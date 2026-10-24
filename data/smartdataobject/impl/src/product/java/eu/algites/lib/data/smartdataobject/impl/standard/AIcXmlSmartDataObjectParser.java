package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.smartdataobject.AIxSmartDataValidationException;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.WildcardType;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlWildcardFragment;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Internal, namespace-aware XML materializer for concrete mutable SmartDataObjects.
 *
 * <p>This parser uses only declared schema metadata, never the position of child
 * elements. It rejects unknown or ambiguous QNames and does not attempt to
 * interpret mixed content or unconstrained XSD wildcards.
 */
public final class AIcXmlSmartDataObjectParser {
    private static final String XSI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
    private static final String XMLNS = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

    private AIcXmlSmartDataObjectParser() { }

    /**
     * Parses one canonical XML document into a generated, concrete SDO subtype.
     *
     * <p>The nested type mapping uses dot-separated property paths relative to
     * the document root, for example {@code Address} or {@code Address.City}.
     * Each provided class must extend the technology-specific SDO base and have
     * an accessible zero-argument constructor. This method performs no schema
     * grammar validation beyond the available DataObject metadata.
     *
     * @param aXml XML input, without DTD or external entity declarations
     * @param aType root generated implementation class
     * @param aNestedTypes concrete classes for referenced nested SDO fields
     * @param <T> generated root SmartDataObject implementation type
     * @return a fully populated, typed root object
     * @throws IllegalArgumentException if the XML is invalid, ambiguous, or
     *                                  cannot be represented by the contract
     * @throws AIxSmartDataValidationException if populated field values violate
     *                                         basic SmartDataObject requirements
     */
    public static <T extends AIcXmlSmartDataObject> T fromXml(
            String aXml, Class<T> aType,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes) {
        Objects.requireNonNull(aXml, "XML input must not be null");
        Objects.requireNonNull(aType, "XML root type must not be null");
        if (aXml.length() > 8_000_000) throw new IllegalArgumentException("XML input size limit exceeded");
        if (aXml.toUpperCase(java.util.Locale.ROOT).contains("<!DOCTYPE") ||
                aXml.toUpperCase(java.util.Locale.ROOT).contains("<!ENTITY"))
            throw new IllegalArgumentException("DTD and XML entities are forbidden");
        try {
            DocumentBuilderFactory locFactory = DocumentBuilderFactory.newInstance();
            locFactory.setNamespaceAware(true);
            locFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            locFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            locFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            locFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            locFactory.setXIncludeAware(false);
            locFactory.setExpandEntityReferences(false);
            Element locRoot = locFactory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(aXml))).getDocumentElement();
            return parse(locRoot, aType,
                    aNestedTypes == null ? Map.of() : Map.copyOf(aNestedTypes), "", null, 0);
        } catch (javax.xml.parsers.ParserConfigurationException |
                org.xml.sax.SAXException | java.io.IOException aFailure) {
            throw new IllegalArgumentException("Invalid or unsafe XML document", aFailure);
        }
    }

    /** Match xs:any namespace tokens against an expanded XML QName. */
    private static boolean wildcardMatches(Element aElement, String aRule, String aTargetNamespace) {
        String locNamespace = aElement.getNamespaceURI() == null ? "" : aElement.getNamespaceURI();
        for (String locToken : aRule.trim().split("\\s+")) {
            if (locToken.equals("##any")
                    || locToken.equals("##local") && locNamespace.isEmpty()
                    || locToken.equals("##targetNamespace") && locNamespace.equals(aTargetNamespace)
                    || locToken.equals("##other") && !locNamespace.isEmpty()
                            && !locNamespace.equals(aTargetNamespace)
                    || locToken.equals(locNamespace)) return true;
        }
        return false;
    }

    /** Retain a complete, standalone XML subtree using secure JAXP transformation. */
    private static String xmlFragment(Element aElement) {
        try {
            var locFactory = TransformerFactory.newInstance();
            locFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            var locTransformer = locFactory.newTransformer();
            locTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter locWriter = new StringWriter();
            locTransformer.transform(new DOMSource(aElement), new StreamResult(locWriter));
            return locWriter.toString();
        } catch (javax.xml.transform.TransformerException aFailure) {
            throw new IllegalArgumentException("Cannot serialize XML wildcard subtree", aFailure);
        }
    }

    /** Safely normalize a manually supplied XML fragment before emitting it unescaped. */
    static String verifiedWildcardFragment(String aFragment, String aConstraint, String aTargetNamespace) {
        Objects.requireNonNull(aFragment, "XML wildcard fragment must not be null");
        if (aFragment.length() > 8_000_000 || aFragment.toUpperCase(java.util.Locale.ROOT).contains("<!DOCTYPE")
                || aFragment.toUpperCase(java.util.Locale.ROOT).contains("<!ENTITY"))
            throw new IllegalArgumentException("Unsafe XML wildcard fragment");
        try {
            var locFactory = DocumentBuilderFactory.newInstance();
            locFactory.setNamespaceAware(true);
            locFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            locFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            locFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            locFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            locFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            locFactory.setXIncludeAware(false);
            locFactory.setExpandEntityReferences(false);
            Element locRoot = locFactory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(aFragment))).getDocumentElement();
            if (!wildcardMatches(locRoot, aConstraint, aTargetNamespace))
                throw new IllegalArgumentException("XML wildcard namespace not permitted");
            return xmlFragment(locRoot);
        } catch (javax.xml.parsers.ParserConfigurationException |
                org.xml.sax.SAXException | java.io.IOException aFailure) {
            throw new IllegalArgumentException("Invalid or unsafe XML wildcard fragment", aFailure);
        }
    }

    private static <T extends AIcXmlSmartDataObject> T parse(
            Element aRoot, Class<T> aType,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes,
            String aPath, String aExpectedQName, int aDepth) {
        if (aDepth >= 128) throw new IllegalArgumentException("XML nesting limit exceeded");
        var locMetadata = AIcSmartDataMetadata.forType(aType);
        String locExpected = aExpectedQName;
        if (locExpected == null && locMetadata.xmlSchema() != null &&
                !locMetadata.xmlSchema().namespaceLocalName().isBlank())
            locExpected = expanded(locMetadata.xmlSchema().namespace(),
                    locMetadata.xmlSchema().namespaceLocalName());
        if (locExpected != null && !locExpected.equals(expanded(aRoot)))
            throw new IllegalArgumentException("Unexpected XML QName: " + expanded(aRoot));
        if (hasText(aRoot)) throw new IllegalArgumentException("Mixed XML content is not supported");

        Map<String, AIcSmartDataMetadata.AIcdField> locElements = new HashMap<>();
        Map<String, AIcSmartDataMetadata.AIcdField> locAttributes = new HashMap<>();
        List<AIcSmartDataMetadata.AIcdField> locWildcards = new ArrayList<>();
        for (var locField : locMetadata.fields()) {
            if (locField.xml() != null && !locField.xml().wildcardNamespace().isBlank()) {
                locWildcards.add(locField);
                continue;
            }
            String locQName = expanded(locField.xmlNamespace(), locField.xmlLocalName());
            var locMap = locField.isXmlAttribute() ? locAttributes : locElements;
            if (locMap.putIfAbsent(locQName, locField) != null)
                throw new IllegalArgumentException("Ambiguous schema XML QName: " + locQName);
        }
        Map<String, Object> locValues = new java.util.LinkedHashMap<>();
        var locAttrs = aRoot.getAttributes();
        for (int i = 0; i < locAttrs.getLength(); i++) {
            Attr locAttr = (Attr) locAttrs.item(i);
            if (XMLNS.equals(locAttr.getNamespaceURI())) continue;
            String locQName = expanded(locAttr);
            if (expanded(XSI, "nil").equals(locQName)) {
                if (locAttr.getValue().equals("false") || locAttr.getValue().equals("0")) continue;
                throw new IllegalArgumentException("Root or nested SDO cannot be xsi:nil");
            }
            var locField = locAttributes.get(locQName);
            if (locField == null) throw new IllegalArgumentException("Unknown XML attribute: " + locQName);
            locValues.put(locField.name(), scalar(locAttr.getValue(), locField.getter().getReturnType()));
        }
        for (Node locChild = aRoot.getFirstChild(); locChild != null; locChild = locChild.getNextSibling()) {
            if (!(locChild instanceof Element locElement)) continue;
            String locQName = expanded(locElement);
            var locField = locElements.get(locQName);
            if (locField == null) {
                for (var locCandidate : locWildcards) {
                    if (wildcardMatches(locElement, locCandidate.xml().wildcardNamespace(),
                            locCandidate.xml().namespace())) {
                        if (locField != null)
                            throw new IllegalArgumentException("Ambiguous XML wildcard: " + locQName);
                        locField = locCandidate;
                    }
                }
            }
            if (locField == null) throw new IllegalArgumentException("Unknown XML element: " + locQName);
            String locFieldPath = aPath.isEmpty() ? locField.name() : aPath + "." + locField.name();
            Class<?> locValueType = locField.getter().getReturnType();
            boolean locRepeated = List.class.isAssignableFrom(locValueType);
            if (locRepeated) locValueType = elementClass(locField.getter().getGenericReturnType());
            Object locValue;
            if (locField.xml() != null && !locField.xml().wildcardNamespace().isBlank()) {
                locValue = wildcardObject(xmlFragment(locElement), locValueType);
                if (locRepeated) {
                    @SuppressWarnings("unchecked")
                    List<Object> locList = (List<Object>) locValues.computeIfAbsent(
                            locField.name(), k -> new ArrayList<>());
                    locList.add(locValue);
                } else {
                    if (locValues.containsKey(locField.name()))
                        throw new IllegalArgumentException("Duplicate non-repeating XML wildcard");
                    locValues.put(locField.name(), locValue);
                }
                continue;
            }
            var locNil = locElement.getAttributeNodeNS(XSI, "nil");
            if (locNil != null && !locNil.getValue().equals("false") && !locNil.getValue().equals("0") &&
                    !locNil.getValue().equals("true") && !locNil.getValue().equals("1"))
                throw new IllegalArgumentException("Invalid xsi:nil lexical value");
            if (locNil != null && (locNil.getValue().equals("true") || locNil.getValue().equals("1"))) {
                if (!locField.annotation().allowsNull())
                    throw new IllegalArgumentException("Non-nillable XML element: " + locFieldPath);
                if (hasChildren(locElement) || hasText(locElement) || otherAttributes(locElement))
                    throw new IllegalArgumentException("Nil XML element has content or other attributes");
                locValue = null;
            } else {
                Class<? extends AIcXmlSmartDataObject> locNested = aNestedTypes.get(locFieldPath);
                if (locNested != null) {
                    locValue = parse(locElement, locNested, aNestedTypes, locFieldPath, locQName, aDepth + 1);
                } else if (AIcXmlSmartDataObject.class.isAssignableFrom(locValueType)) {
                    Class<? extends AIcXmlSmartDataObject> locTarget = locValueType.asSubclass(AIcXmlSmartDataObject.class);
                    locValue = parse(locElement, locTarget, aNestedTypes, locFieldPath, locQName, aDepth + 1);
                } else {
                    if (hasChildren(locElement) || otherAttributes(locElement))
                        throw new IllegalArgumentException("Unexpected nested XML content: " + locFieldPath);
                    locValue = scalar(locElement.getTextContent(), locValueType);
                }
            }
            if (locRepeated) {
                @SuppressWarnings("unchecked")
                List<Object> locList = (List<Object>) locValues.computeIfAbsent(locField.name(), k -> new ArrayList<>());
                locList.add(locValue);
            } else {
                if (locValues.containsKey(locField.name()))
                    throw new IllegalArgumentException("Duplicate non-repeating XML element: " + locFieldPath);
                locValues.put(locField.name(), locValue);
            }
        }
        for (var locField : locMetadata.fields()) {
            var locXml = locField.xml();
            if (locXml != null && (locXml.minOccurs() >= 0 || locXml.maxOccurs() >= 0)) {
                Object locValue = locValues.get(locField.name());
                int locCount = locValue instanceof List<?> locList ? locList.size()
                        : locValues.containsKey(locField.name()) ? 1 : 0;
                if (locXml.minOccurs() >= 0 && locCount < locXml.minOccurs())
                    throw new IllegalArgumentException("XML field '" + locField.name() + "' has "
                            + locCount + " occurrences; minimum is " + locXml.minOccurs());
                if (locXml.maxOccurs() >= 0 && locCount > locXml.maxOccurs())
                    throw new IllegalArgumentException("XML field '" + locField.name() + "' has "
                            + locCount + " occurrences; maximum is " + locXml.maxOccurs());
            }
            if (List.class.isAssignableFrom(locField.getter().getReturnType()))
                locValues.putIfAbsent(locField.name(), new ArrayList<>());
        }
        T locObject = instantiate(aType);
        for (var locValue : locValues.entrySet()) locObject.set_RawField(locValue.getKey(), locValue.getValue());
        var locIssues = locObject._getValidationIssues(eu.algites.lib.data.smartdataobject.AInSdoFieldValueLoadingMode.RAW_VALUES);
        if (!locIssues.isEmpty()) throw new AIxSmartDataValidationException(locIssues);
        return locObject;
    }

    private static <T extends AIcXmlSmartDataObject> T instantiate(Class<T> aType) {
        try {
            Constructor<T> locConstructor = aType.getDeclaredConstructor();
            return locConstructor.newInstance();
        } catch (ReflectiveOperationException aFailure) {
            throw new IllegalArgumentException("XML type requires an accessible empty constructor: " + aType, aFailure);
        }
    }

    private static boolean hasChildren(Element aElement) {
        for (Node locChild = aElement.getFirstChild(); locChild != null; locChild = locChild.getNextSibling())
            if (locChild instanceof Element) return true;
        return false;
    }

    private static boolean hasText(Element aElement) {
        for (Node locChild = aElement.getFirstChild(); locChild != null; locChild = locChild.getNextSibling())
            if ((locChild.getNodeType() == Node.TEXT_NODE || locChild.getNodeType() == Node.CDATA_SECTION_NODE)
                    && !locChild.getNodeValue().isBlank()) return true;
        return false;
    }

    private static boolean otherAttributes(Element aElement) {
        var locAttributes = aElement.getAttributes();
        for (int i = 0; i < locAttributes.getLength(); i++) {
            Attr locAttribute = (Attr) locAttributes.item(i);
            if (!XMLNS.equals(locAttribute.getNamespaceURI()) &&
                    !expanded(XSI, "nil").equals(expanded(locAttribute))) return true;
        }
        return false;
    }

    /** Build a read-only XML object also implementing the namespace-specific generated contract. */
    private static AIiXmlWildcardFragment wildcardObject(String fragment, Class<?> requestedType) {
        if (!requestedType.isInterface() || !AIiXmlDataObject.class.isAssignableFrom(requestedType)) {
            if (requestedType == String.class || requestedType == Object.class)
                return wildcardProxy(fragment, AIiXmlDataObject.class);
            throw new IllegalArgumentException("XML wildcard getter must declare an XML data object interface: "
                    + requestedType.getName());
        }
        return wildcardProxy(fragment, requestedType);
    }

    /** Preserve XML subtrees behind an interface proxy without inventing schema fields. */
    private static AIiXmlWildcardFragment wildcardProxy(String fragment, Class<?> requestedType) {
        Class<?>[] interfaces = requestedType.isAssignableFrom(AIiXmlWildcardFragment.class)
                ? new Class<?>[] {AIiXmlWildcardFragment.class}
                : new Class<?>[] {requestedType, AIiXmlWildcardFragment.class};
        return (AIiXmlWildcardFragment) Proxy.newProxyInstance(
                requestedType.getClassLoader(), interfaces, (proxy, method, args) -> {
                    if (method.getName().equals("xmlFragment") && method.getParameterCount() == 0)
                        return fragment;
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> fragment;
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> throw new UnsupportedOperationException(method.toString());
                        };
                    }
                    throw new UnsupportedOperationException(
                            "Unbound XML wildcard has no generated field getter: " + method.getName());
                });
    }

    private static Class<?> elementClass(Type aType) {
        if (aType instanceof ParameterizedType locType && locType.getActualTypeArguments().length == 1) {
            Type member = locType.getActualTypeArguments()[0];
            if (member instanceof Class<?> locClass) return locClass;
            if (member instanceof WildcardType locWildcard && locWildcard.getUpperBounds().length == 1
                    && locWildcard.getUpperBounds()[0] instanceof Class<?> locClass) return locClass;
        }
        return String.class;
    }

    private static Object scalar(String aText, Class<?> aType) {
        if (aType == String.class || aType == Object.class) return aText;
        String locTrimmed = aText.strip();
        if (aType == Boolean.class || aType == boolean.class) {
            if (locTrimmed.equals("true") || locTrimmed.equals("1")) return true;
            if (locTrimmed.equals("false") || locTrimmed.equals("0")) return false;
            throw new IllegalArgumentException("Invalid XML boolean value: " + aText);
        }
        if (aType == Integer.class || aType == int.class) return Integer.valueOf(locTrimmed);
        if (aType == Long.class || aType == long.class) return Long.valueOf(locTrimmed);
        if (aType == Short.class || aType == short.class) return Short.valueOf(locTrimmed);
        if (aType == Double.class || aType == double.class) return Double.valueOf(locTrimmed);
        if (aType == Float.class || aType == float.class) return Float.valueOf(locTrimmed);
        if (aType == BigInteger.class) return new BigInteger(locTrimmed);
        if (aType == BigDecimal.class) return new BigDecimal(locTrimmed);
        if (aType.isEnum()) {
            for (Object locEnum : aType.getEnumConstants()) {
                String locWire;
                try {
                    locWire = String.valueOf(aType.getMethod("wireValue").invoke(locEnum));
                } catch (NoSuchMethodException aFailure) {
                    locWire = ((Enum<?>) locEnum).name();
                } catch (IllegalAccessException | InvocationTargetException aFailure) {
                    throw new IllegalArgumentException("Cannot read XML enum wire value", aFailure);
                }
                if (locWire.equals(aText)) return locEnum;
            }
            throw new IllegalArgumentException("Unrecognized XML enumeration value: " + aText);
        }
        throw new IllegalArgumentException("XML field has unsupported scalar type: " + aType);
    }

    private static String expanded(Element aElement) {
        return expanded(aElement.getNamespaceURI(), aElement.getLocalName() == null
                ? aElement.getTagName() : aElement.getLocalName());
    }

    private static String expanded(Attr aAttr) {
        return expanded(aAttr.getNamespaceURI(), aAttr.getLocalName() == null
                ? aAttr.getName() : aAttr.getLocalName());
    }

    private static String expanded(String aNamespace, String aLocalName) {
        return aNamespace == null || aNamespace.isEmpty() ? aLocalName : "{" + aNamespace + "}" + aLocalName;
    }
}
