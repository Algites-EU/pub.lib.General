package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.smartdataobject.AIxSmartDataValidationException;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.WildcardType;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.AIaKeyDefinition;
import eu.algites.lib.common.list.AIcKeyDefinition;
import eu.algites.lib.common.list.AIcMultiMappedList;
import eu.algites.lib.common.list.AIiKeyDefinition;
import eu.algites.lib.common.enums.AIiEnumItem;
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

    /** One matched immutable sealed CHOICE alternative with its XML token count. */
    private record AIcdEmbeddedChoiceMatch(int consumed, Object value) { }

    /**
     * Parse exactly one immutable CHOICE variant through its generated getter
     * annotations. The return type remains the original sealed interface;
     * neither the parent DataObject nor the choice type is duplicated.
     *
     * @param aChoice read-only sealed choice interface
     * @param aChildren XML siblings containing the selected alternative
     * @param aStart start position within those siblings
     * @param aNestedTypes explicit concrete SDO implementations for nested groups
     * @param aPath logical getter path for precise diagnostics
     * @param aDepth recursion depth
     * @return matched record and consumed element count; zero if no alternative matches
     */
    private static AIcdEmbeddedChoiceMatch choiceMatch(Class<?> aChoice,
            List<Element> aChildren, int aStart,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes,
            String aPath, int aDepth) {
        if (aDepth >= 128) throw new IllegalArgumentException("XML CHOICE nesting limit exceeded");
        if (!aChoice.isSealed())
            throw new IllegalArgumentException("Expected sealed XML CHOICE: " + aChoice.getName());
        List<AIcdEmbeddedChoiceMatch> matches = new ArrayList<>();
        List<String> missingBindings = new ArrayList<>();
        for (Class<?> variant : aChoice.getPermittedSubclasses()) {
            if (!variant.isRecord())
                throw new IllegalArgumentException("CHOICE alternative is not a record: " + variant.getName());
            // Defs emits the possible first XML QNames of each variant from
            // the linked structural model. Skip an unselected alternative
            // before checking its nested concrete SDO implementation.
            try {
                var firstMethod = variant.getMethod("xmlFirstQNames");
                if (!java.lang.reflect.Modifier.isStatic(firstMethod.getModifiers()))
                    throw new IllegalArgumentException("CHOICE XML prefix must be static: " + variant.getName());
                Object declared = firstMethod.invoke(null);
                if (!(declared instanceof java.util.Set<?> firstQNames)
                        || firstQNames.isEmpty()
                        || firstQNames.stream().anyMatch(name -> !(name instanceof String)))
                    throw new IllegalArgumentException("Invalid CHOICE XML prefix: " + variant.getName());
                if (aStart >= aChildren.size()
                        || !firstQNames.contains(expanded(aChildren.get(aStart)))) continue;
            } catch (NoSuchMethodException absent) {
                // Handwritten/older variants can still use their getter metadata.
            } catch (ReflectiveOperationException failure) {
                throw new IllegalArgumentException("Cannot resolve generated CHOICE XML prefix: "
                        + variant.getName(), failure);
            }
            var components = variant.getRecordComponents();
            java.util.TreeMap<Integer, java.lang.reflect.Method> getters = new java.util.TreeMap<>();
            for (var method : variant.getDeclaredMethods()) {
                var field = method.getDeclaredAnnotation(
                        eu.algites.lib.data.dataobject.AIaDataObjectField.class);
                if (field != null && method.getParameterCount() == 0
                        && getters.putIfAbsent(field.order(), method) != null)
                    throw new IllegalArgumentException("Duplicate CHOICE getter order: " + variant.getName());
            }
            if (getters.size() != components.length)
                throw new IllegalArgumentException("CHOICE record getter metadata mismatch: "
                        + variant.getName());
            List<Object> values = new ArrayList<>();
            int cursor = aStart;
            int index = 0;
            for (var getter : getters.values()) {
                Class<?> componentType = components[index++].getType();
                var wire = getter.getDeclaredAnnotation(
                        eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField.class);
                Object value;
                if (wire != null) {
                    if (cursor >= aChildren.size() || !expanded(wire.namespace(),
                            wire.namespaceLocalName()).equals(expanded(aChildren.get(cursor)))) break;
                    var child = aChildren.get(cursor);
                    if (wire.valueKind()
                            != eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
                        throw new IllegalArgumentException("CHOICE only supports XML element variants: " + getter);
                    String semantic = getter.getName().startsWith("get")
                            ? getter.getName().substring(3) : getter.getName();
                    String nestedPath = aPath + "." + semantic;
                    var nested = aNestedTypes.get(nestedPath);
                    if (nested == null) nested = aNestedTypes.get(semantic);
                    if (nested == null && AIcXmlSmartDataObject.class.isAssignableFrom(componentType))
                        nested = componentType.asSubclass(AIcXmlSmartDataObject.class);
                    if (nested != null) {
                        value = parse(child, nested, aNestedTypes,
                                nestedPath, expanded(child), aDepth + 1);
                    } else if (componentType.isInterface()
                            && AIiXmlDataObject.class.isAssignableFrom(componentType)) {
                        missingBindings.add(nestedPath);
                        break;
                    } else {
                        if (hasChildren(child) || otherAttributes(child))
                            throw new IllegalArgumentException("CHOICE scalar has nested XML: " + nestedPath);
                        value = scalar(child.getTextContent(), componentType);
                    }
                    cursor++;
                } else {
                    var field = getter.getDeclaredAnnotation(
                            eu.algites.lib.data.dataobject.AIaDataObjectField.class);
                    if (field == null || !field.embedded())
                        throw new IllegalArgumentException("Unmapped CHOICE getter: " + getter);
                    String semantic = getter.getName().startsWith("get")
                            ? getter.getName().substring(3) : getter.getName();
                    String nestedPath = aPath + "." + semantic;
                    var nested = aNestedTypes.get(nestedPath);
                    if (nested == null) nested = aNestedTypes.get(semantic);
                    if (nested == null && AIcXmlSmartDataObject.class.isAssignableFrom(componentType))
                        nested = componentType.asSubclass(AIcXmlSmartDataObject.class);
                    if (nested == null) {
                        missingBindings.add(nestedPath);
                        break;
                    }
                    int count = embeddedPrefix(nested, aChildren, cursor,
                            aNestedTypes, nestedPath, aDepth + 1);
                    if (count <= 0) break;
                    Element virtual = aChildren.get(cursor).getOwnerDocument().createElement(
                            "__embedded_choice_content__");
                    for (int i = cursor; i < cursor + count; i++)
                        virtual.appendChild(aChildren.get(i).cloneNode(true));
                    value = parse(virtual, nested, aNestedTypes,
                            nestedPath, expanded(virtual), aDepth + 1);
                    cursor += count;
                }
                values.add(value);
            }
            if (values.size() == components.length && cursor > aStart) {
                try {
                    Class<?>[] types = java.util.Arrays.stream(components)
                            .map(java.lang.reflect.RecordComponent::getType).toArray(Class<?>[]::new);
                    var constructor = variant.getDeclaredConstructor(types);
                    Object value = constructor.newInstance(values.toArray());
                    matches.add(new AIcdEmbeddedChoiceMatch(cursor - aStart, value));
                } catch (ReflectiveOperationException failure) {
                    throw new IllegalArgumentException("Cannot instantiate sealed CHOICE: "
                            + variant.getName(), failure);
                }
            }
        }
        if (matches.size() > 1)
            throw new IllegalArgumentException("Ambiguous embedded XML CHOICE: " + aPath);
        if (!matches.isEmpty()) return matches.get(0);
        if (aStart < aChildren.size() && !missingBindings.isEmpty())
            throw new IllegalArgumentException("CHOICE needs concrete nested SDO for "
                    + String.join(", ", missingBindings));
        return new AIcdEmbeddedChoiceMatch(0, null);
    }

    /**
     * Match one embedded sequence occurrence without a synthetic XML wrapper.
     * A group is a typed object in memory but a contiguous particle sequence
     * on the wire. The result is the number of direct child elements consumed;
     * zero means no occurrence starts at the current position.
     *
     * @param aType concrete embedded SDO type
     * @param aChildren ordered children of the containing XML element
     * @param aStart first unconsumed child
     * @param aNestedTypes explicit nested SDO registrations
     * @param aPath semantic object path for nested type lookup
     * @param aDepth recursion depth
     * @return consumed child count, or zero if no match
     */
    private static int embeddedPrefix(Class<? extends AIcXmlSmartDataObject> aType,
            List<Element> aChildren, int aStart,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes,
            String aPath, int aDepth) {
        if (aDepth >= 128) throw new IllegalArgumentException("Embedded XML nesting limit exceeded");
        int cursor = aStart;
        for (var field : AIcSmartDataMetadata.forType(aType).fields()) {
            if (field.isXmlAttribute())
                throw new IllegalArgumentException("Embedded XML group cannot contain attributes: " + field.name());
            int count = 0;
            if (field.annotation().embedded()) {
                String nestedPath = aPath.isEmpty() ? field.name() : aPath + "." + field.name();
                Class<?> target = field.getter().getReturnType();
                if (List.class.isAssignableFrom(target))
                    target = elementClass(field.getter().getGenericReturnType());
                var nested = target.isSealed() ? null : embeddedType(field, aNestedTypes, nestedPath);
                while (cursor < aChildren.size()) {
                    int matched = target.isSealed()
                            ? choiceMatch(target, aChildren, cursor, aNestedTypes,
                                    nestedPath, aDepth + 1).consumed()
                            : embeddedPrefix(nested, aChildren, cursor, aNestedTypes,
                                    nestedPath, aDepth + 1);
                    if (matched <= 0) break;
                    cursor += matched;
                    count++;
                    if (!List.class.isAssignableFrom(field.getter().getReturnType())
                            && field.annotation().maxItemCount() <= 1) break;
                    if (field.annotation().maxItemCount() >= 0
                            && count >= field.annotation().maxItemCount()) break;
                }
            } else {
                if (field.xml() != null && !field.xml().wildcardNamespace().isBlank())
                    throw new IllegalArgumentException("Embedded XML wildcard needs an explicit particle matcher");
                String qname = expanded(field.xmlNamespace(), field.xmlLocalName());
                while (cursor < aChildren.size() && qname.equals(expanded(aChildren.get(cursor)))) {
                    cursor++;
                    count++;
                    if (!List.class.isAssignableFrom(field.getter().getReturnType())
                            && field.annotation().maxItemCount() <= 1) break;
                    if (field.annotation().maxItemCount() >= 0
                            && count >= field.annotation().maxItemCount()) break;
                }
            }
            if (field.annotation().minItemCount() > count) return 0;
        }
        return cursor - aStart;
    }

    /** Resolve one embedded structural member to a concrete generated implementation. */
    private static Class<? extends AIcXmlSmartDataObject> embeddedType(
            AIcSmartDataMetadata.AIcdField aField,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes, String aPath) {
        Class<? extends AIcXmlSmartDataObject> registered = aNestedTypes.get(aPath);
        if (registered != null) return registered;
        Class<?> expected = aField.getter().getReturnType();
        if (List.class.isAssignableFrom(expected))
            expected = elementClass(aField.getter().getGenericReturnType());
        if (AIcXmlSmartDataObject.class.isAssignableFrom(expected))
            return expected.asSubclass(AIcXmlSmartDataObject.class);
        throw new IllegalArgumentException("Embedded XML group needs a concrete nested SDO: " + aPath);
    }

    private static <T extends AIcXmlSmartDataObject> T parse(
            Element aRoot, Class<T> aType,
            Map<String, Class<? extends AIcXmlSmartDataObject>> aNestedTypes,
            String aPath, String aExpectedQName, int aDepth) {
        if (aDepth >= 128) throw new IllegalArgumentException("XML nesting limit exceeded");
        var locMetadata = AIcSmartDataMetadata.forType(aType);
        Map<String, Class<? extends AIcXmlSmartDataObject>> locEffectiveNested = new HashMap<>(aNestedTypes);
        try {
            var locMethod = aType.getMethod("xmlNestedTypes");
            Object locDeclared = locMethod.invoke(null);
            if (!(locDeclared instanceof Map<?, ?> locMap))
                throw new IllegalArgumentException("Invalid generated XML nested type registry");
            for (var locEntry : locMap.entrySet()) {
                if (!(locEntry.getKey() instanceof String locName)
                        || !(locEntry.getValue() instanceof Class<?> locClass)
                        || !AIcXmlSmartDataObject.class.isAssignableFrom(locClass))
                    throw new IllegalArgumentException("Invalid generated XML nested type entry");
                @SuppressWarnings("unchecked")
                Class<? extends AIcXmlSmartDataObject> locTarget =
                        (Class<? extends AIcXmlSmartDataObject>) locClass;
                locEffectiveNested.putIfAbsent(aPath.isEmpty() ? locName : aPath + "." + locName,
                        locTarget);
            }
        } catch (NoSuchMethodException locAbsent) {
            /* Handwritten SmartDataObjects may supply only the caller's nested registry. */
        } catch (ReflectiveOperationException locFailure) {
            throw new IllegalArgumentException("Cannot resolve generated XML nested classes", locFailure);
        }
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
            if (locField.annotation().embedded()) {
                if (!locField.annotation().fieldName().isEmpty())
                    throw new IllegalArgumentException("Embedded XML field has a wire name: " + locField.name());
                continue;
            }
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
        // A named or synthetic group has no XML QName. Consume its occurrence
        // directly from the ordered element stream before ordinary QName lookup.
        // The two scopes are deliberately independent: GROUP reference identity
        // is a semantic getter, not an invented serialized field.
        List<Element> locChildren = new ArrayList<>();
        for (Node child = aRoot.getFirstChild(); child != null; child = child.getNextSibling())
            if (child instanceof Element element) locChildren.add(element);
        java.util.Set<Element> locEmbeddedChildren = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        if (locMetadata.fields().stream().anyMatch(field -> field.annotation().embedded())) {
            int cursor = 0;
            for (var field : locMetadata.fields()) {
                if (!field.annotation().embedded()) {
                    if (field.isXmlAttribute()) continue;
                    String qname = expanded(field.xmlNamespace(), field.xmlLocalName());
                    while (cursor < locChildren.size() && qname.equals(expanded(locChildren.get(cursor)))) {
                        cursor++;
                        if (!List.class.isAssignableFrom(field.getter().getReturnType())) break;
                    }
                    continue;
                }
                String fieldPath = aPath.isEmpty() ? field.name() : aPath + "." + field.name();
                Class<?> target = field.getter().getReturnType();
                if (List.class.isAssignableFrom(target))
                    target = elementClass(field.getter().getGenericReturnType());
                boolean choice = target.isSealed();
                var nested = choice ? null : embeddedType(field, locEffectiveNested, fieldPath);
                List<Object> values = new ArrayList<>();
                while (cursor < locChildren.size()) {
                    AIcdEmbeddedChoiceMatch choiceValue = choice
                            ? choiceMatch(target, locChildren, cursor, locEffectiveNested,
                                    fieldPath, aDepth + 1) : null;
                    int consumed = choice ? choiceValue.consumed()
                            : embeddedPrefix(nested, locChildren, cursor,
                                    locEffectiveNested, fieldPath, aDepth + 1);
                    if (consumed <= 0) break;
                    Element virtual = choice ? null : aRoot.getOwnerDocument().createElementNS(
                            aRoot.getNamespaceURI(), aRoot.getTagName());
                    for (int i = cursor; i < cursor + consumed; i++) {
                        locEmbeddedChildren.add(locChildren.get(i));
                        if (!choice) virtual.appendChild(locChildren.get(i).cloneNode(true));
                    }
                    values.add(choice ? choiceValue.value()
                            : parse(virtual, nested, locEffectiveNested,
                                    fieldPath, expanded(aRoot), aDepth + 1));
                    cursor += consumed;
                    if (!List.class.isAssignableFrom(field.getter().getReturnType())
                            && field.annotation().maxItemCount() <= 1) break;
                    if (field.annotation().maxItemCount() >= 0
                            && values.size() >= field.annotation().maxItemCount()) break;
                }
                if (!values.isEmpty()) locValues.put(field.name(),
                        List.class.isAssignableFrom(field.getter().getReturnType()) ? values : values.get(0));
            }
        }
        for (Node locChild = aRoot.getFirstChild(); locChild != null; locChild = locChild.getNextSibling()) {
            if (!(locChild instanceof Element locElement)) continue;
            if (locEmbeddedChildren.contains(locElement)) continue;
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
                Class<? extends AIcXmlSmartDataObject> locNested = locEffectiveNested.get(locFieldPath);
                if (locNested != null) {
                    locValue = parse(locElement, locNested, locEffectiveNested, locFieldPath, locQName, aDepth + 1);
                } else if (AIcXmlSmartDataObject.class.isAssignableFrom(locValueType)) {
                    Class<? extends AIcXmlSmartDataObject> locTarget = locValueType.asSubclass(AIcXmlSmartDataObject.class);
                    locValue = parse(locElement, locTarget, locEffectiveNested, locFieldPath, locQName, aDepth + 1);
                } else {
                    if (hasChildren(locElement) || otherAttributes(locElement))
                        throw new IllegalArgumentException("Unexpected nested XML content: " + locFieldPath);
                    String locLexical = locElement.getTextContent();
                    // A schema-permitted empty XML element is a distinct raw
                    // state. A direct setXxxx("") remains an ordinary String.
                    // The two states serialize identically, by design.
                    locValue = !locRepeated && locValueType == String.class
                            && locField.annotation().isEmptyAllowed() && locLexical.isEmpty()
                            ? AIcSmartDataObject.EMPTY_VALUE : scalar(locLexical, locValueType);
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
            var locBounds = locField.annotation();
            if (locBounds.minItemCount() >= 0 || locBounds.maxItemCount() >= 0) {
                Object locValue = locValues.get(locField.name());
                int locCount = locValue instanceof List<?> locList ? locList.size()
                        : locValues.containsKey(locField.name()) ? 1 : 0;
                if (locBounds.minItemCount() >= 0 && locCount < locBounds.minItemCount())
                    throw new IllegalArgumentException("XML field '" + locField.name() + "' has "
                            + locCount + " occurrences; minimum is " + locBounds.minItemCount());
                if (locBounds.maxItemCount() >= 0 && locCount > locBounds.maxItemCount())
                    throw new IllegalArgumentException("XML field '" + locField.name() + "' has "
                            + locCount + " occurrences; maximum is " + locBounds.maxItemCount());
            }
            if (List.class.isAssignableFrom(locField.getter().getReturnType()))
                locValues.putIfAbsent(locField.name(), new ArrayList<>());
        }
        for (var locField : locMetadata.fields()) {
            AIaKeyDefinition[] locKeys = java.util.Arrays.stream(locField.getter()
                    .getAnnotationsByType(AIaKeyDefinition.class))
                    .filter(key -> key.selector().isBlank())
                    .toArray(AIaKeyDefinition[]::new);
            if (locKeys.length == 0) continue;
            if (!List.class.isAssignableFrom(locField.getter().getReturnType()))
                throw new IllegalArgumentException("xs:key requires a repeating XML field: " + locField.name());
            Object locRaw = locValues.get(locField.name());
            if (!(locRaw instanceof List<?> locList))
                throw new IllegalArgumentException("Expected XML list for indexed field: " + locField.name());
            List<AIiKeyDefinition<Object, ?>> locDefinitions = new ArrayList<>();
            for (AIaKeyDefinition locKey : locKeys) {
                String[] locPaths = locKey.fields().clone();
                if (locPaths.length == 0)
                    throw new IllegalArgumentException("Empty xs:key: " + locKey.name());
                locDefinitions.add(new AIcKeyDefinition<>(locKey.name(),
                        aItem -> xmlKey(aItem, locKey.name(), locPaths, locKey.required()),
                        true, locKey.required()));
            }
            AIcMultiMappedList<Object> locIndexed = new AIcMultiMappedList<>(locDefinitions);
            locIndexed.addAll(locList);
            locValues.put(locField.name(), locIndexed);
        }
        // Scoped identity rules are declared on the reference getter and
        // applied to its child list after the original nominal TYPE was parsed.
        // They are not inherited globally by other uses of that shared TYPE.
        for (var locField : locMetadata.fields()) {
            AIaKeyDefinition[] locScoped = java.util.Arrays.stream(locField.getter()
                    .getAnnotationsByType(AIaKeyDefinition.class))
                    .filter(key -> !key.selector().isBlank())
                    .toArray(AIaKeyDefinition[]::new);
            if (locScoped.length == 0) continue;
            Object locNested = locValues.get(locField.name());
            if (locNested == null) continue;
            if (!(locNested instanceof AIcXmlSmartDataObject nested)) {
                throw new IllegalArgumentException("Scoped xs:key needs a nested XML DataObject: "
                        + locField.name());
            }
            applyScopedKeys(nested, locField.name(), locScoped);
        }
        // Some shared TYPEs have a multi-map-capable LIST getter, even when
        // this particular occurrence has no local identity constraint.
        for (var locField : locMetadata.fields()) {
            if (!eu.algites.lib.common.list.AIiMultiMappedList.class.isAssignableFrom(
                    locField.getter().getReturnType())) continue;
            Object locRaw = locValues.get(locField.name());
            if (locRaw instanceof List<?> list
                    && !(locRaw instanceof eu.algites.lib.common.list.AIiMultiMappedList<?>)) {
                AIcMultiMappedList<Object> capable = new AIcMultiMappedList<>();
                capable.addAll(list);
                locValues.put(locField.name(), capable);
            }
        }
        T locObject = instantiate(aType);
        for (var locValue : locValues.entrySet()) {
            if (locValue.getValue() == AIcSmartDataObject.EMPTY_VALUE)
                locObject.set_EmptyField(locValue.getKey());
            else
                locObject.set_RawField(locValue.getKey(), locValue.getValue());
        }
        var locIssues = locObject._getValidationIssues(eu.algites.lib.data.smartdataobject.AInSdoFieldValueLoadingMode.RAW_VALUES);
        if (!locIssues.isEmpty()) throw new AIxSmartDataValidationException(locIssues);
        return locObject;
    }

    /**
     * Attach occurrence-local indexes to an existing nested object's selected
     * repeated member. Neither the nested TYPE nor its generated class changes.
     *
     * @param owner the parsed object reached through the parent element
     * @param ownerField enclosing element name used in errors
     * @param rules identity constraints declared on that enclosing element
     * @throws IllegalArgumentException if a selector is missing, ambiguous or
     *         does not select a repeating field, or if a uniqueness rule fails
     */
    private static void applyScopedKeys(AIcXmlSmartDataObject owner, String ownerField,
            AIaKeyDefinition[] rules) {
        var metadata = AIcSmartDataMetadata.forType(owner.getClass());
        Map<String, List<AIaKeyDefinition>> grouped = new java.util.LinkedHashMap<>();
        for (AIaKeyDefinition rule : rules) {
            AIcSmartDataMetadata.AIcdField selected = null;
            for (var candidate : metadata.fields()) {
                if (rule.selector().equals(candidate.name())
                        || rule.selector().equals(candidate.xmlLocalName())) {
                    if (selected != null)
                        throw new IllegalArgumentException("Ambiguous scoped xs:key selector "
                                + rule.selector() + " on " + ownerField);
                    selected = candidate;
                }
            }
            if (selected == null || !List.class.isAssignableFrom(selected.getter().getReturnType()))
                throw new IllegalArgumentException("Scoped xs:key selector must resolve to a LIST: "
                        + ownerField + "/" + rule.selector());
            grouped.computeIfAbsent(selected.name(), ignored -> new ArrayList<>()).add(rule);
        }
        for (var group : grouped.entrySet()) {
            Object value = owner.get_RawField(group.getKey());
            if (!(value instanceof List<?> items))
                throw new IllegalArgumentException("Scoped xs:key member is not a list: "
                        + ownerField + "/" + group.getKey());
            List<AIiKeyDefinition<Object, ?>> definitions = new ArrayList<>();
            for (AIaKeyDefinition rule : group.getValue()) {
                String[] paths = rule.fields().clone();
                if (paths.length == 0)
                    throw new IllegalArgumentException("Empty scoped xs:key: " + rule.name());
                definitions.add(new AIcKeyDefinition<>(rule.name(),
                        item -> xmlKey(item, rule.name(), paths, rule.required()),
                        true, rule.required()));
            }
            AIcMultiMappedList<Object> indexed = new AIcMultiMappedList<>(false, definitions);
            indexed.addAll(items);
            owner.set_RawField(group.getKey(), indexed);
        }
    }

    /**
     * Extract an XML identity tuple from already parsed typed DataObject values.
     * This does not use string XPath evaluation: each component is returned by
     * its generated getter, preserving numeric equality, enum identity and the
     * distinction between a missing field and a present empty string.
     *
     * @param aItem typed XML value object owning all selected fields
     * @param aKeyName diagnostic name of the {@code xs:key}
     * @param aPaths ordered schema field paths, each one or more named steps
     * @param aRequired whether a missing component is an invalid key rather than an absent index
     * @return one typed scalar or an immutable ordered tuple of typed components
     * @throws IllegalArgumentException if a step is absent, ambiguous or inaccessible
     */
    private static Object xmlKey(Object aItem, String aKeyName, String[] aPaths, boolean aRequired) {
        List<Object> locValues = new ArrayList<>();
        for (String locPath : aPaths) {
            Object locCurrent = aItem;
            for (String locSegment : locPath.split("\\.", -1)) {
                if (locSegment.isBlank())
                    throw new IllegalArgumentException("Invalid XML identity path " + locPath);
                if (locCurrent == null) {
                    if (!aRequired) return null;
                    throw new IllegalArgumentException("Missing xs:key " + aKeyName + " component " + locPath);
                }
                var locDescriptor = AIcSmartDataMetadata.forType(locCurrent.getClass());
                var locMatches = locDescriptor.fields().stream()
                        .filter(aField -> aField.name().equals(locSegment)
                                || aField.xmlLocalName().equals(locSegment)).toList();
                if (locMatches.size() != 1)
                    throw new IllegalArgumentException("Unresolved xs:key " + aKeyName
                            + " component " + locPath + ": " + locSegment);
                try {
                    locCurrent = locMatches.get(0).getter().invoke(locCurrent);
                } catch (ReflectiveOperationException locFailure) {
                    throw new IllegalArgumentException("Cannot read xs:key " + aKeyName
                            + " component " + locPath, locFailure);
                }
            }
            if (locCurrent == null && !aRequired) return null;
            if (locCurrent == null || locCurrent instanceof List<?> || locCurrent instanceof Map<?, ?>)
                throw new IllegalArgumentException("Missing or non-scalar XML key " + aKeyName
                        + " component " + locPath);
            locValues.add(locCurrent);
        }
        return locValues.size() == 1 ? locValues.get(0) : List.copyOf(locValues);
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
                Object locCode = locEnum instanceof AIiEnumItem locItem
                        ? Objects.requireNonNull(locItem.code(), "Enum item code must not be null")
                        : ((Enum<?>) locEnum).name();
                if (String.valueOf(locCode).equals(aText)) return locEnum;
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
