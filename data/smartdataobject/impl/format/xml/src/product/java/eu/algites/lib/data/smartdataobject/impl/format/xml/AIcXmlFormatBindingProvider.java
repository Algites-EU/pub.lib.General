package eu.algites.lib.data.smartdataobject.impl.format.xml;

import eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectFieldXmlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.impl.common.*;
import eu.algites.lib.text.format.AInTextFormatType;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * XML format binding provider for the independent common SmartDataObject runtime.
 * Namespaced attributes and elements are resolved exclusively from XMLDEFS
 * annotations; XML-specific markers and static nested implementation maps are
 * not required.
 *
 * @author linhart1
 */
public final class AIcXmlFormatBindingProvider implements AIiSmartDataObjectFormatBindingProvider {
    /** Stable built-in provider identifier. */
    public static final String PROVIDER_ID = "algites-xml";

    /** {@inheritDoc} */
    @Override public String getFormatId() { return AInTextFormatType.XML.code(); }
    /** {@inheritDoc} */
    @Override public String getProviderId() { return PROVIDER_ID; }

    /** {@inheritDoc} */
    @Override public Optional<? extends AIiSmartDataObjectFormatBindingMetadata> resolveMetadata(
            AIiSmartDataObjectMetadata aMetadata) {
        AIaDataObjectXmlSchema schema = null;
        Map<String, XmlField> fields = new LinkedHashMap<>();
        for (Class<?> type : AIsSmartDataObjectFormatMetadataUtils.hierarchy(aMetadata.getImplementationClass())) {
            AIaDataObjectXmlSchema declared = type.getDeclaredAnnotation(AIaDataObjectXmlSchema.class);
            if (declared != null) schema = declared;
            for (Method getter : type.getDeclaredMethods()) {
                AIaDataObjectFieldXmlSchema field = getter.getDeclaredAnnotation(AIaDataObjectFieldXmlSchema.class);
                if (field != null && getter.getParameterCount() == 0 && getter.getName().startsWith("get") && getter.getName().length() > 3)
                    fields.put(getter.getName().substring(3), new XmlField(field.namespace(),
                            field.namespaceLocalName(), field.valueKind() == AInXmlValueKind.ATTRIBUTE));
            }
        }
        if (schema == null && fields.isEmpty()) return Optional.empty();
        String rootName = schema == null || schema.namespaceLocalName().isEmpty()
                ? aMetadata.getImplementationClass().getSimpleName() : schema.namespaceLocalName();
        String namespace = schema == null ? "" : schema.namespace();
        return Optional.of(new XmlMetadata(namespace, rootName, Map.copyOf(fields)));
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectMarshaller createMarshaller() {
        return (object, registry) -> {
            try {
                if (!(object instanceof AIcSmartDataObject sdo))
                    throw new IllegalArgumentException("Not an independent common SDO instance");
                var binding = registry.getBinding(sdo.getClass());
                if (binding == null) throw new IllegalArgumentException("Unregistered root SDO: " + sdo.getClass());
                Document document = builder().newDocumentBuilder().newDocument();
                XmlMetadata xml = metadata(binding);
                Element root = document.createElementNS(emptyAsNull(xml.namespace()), xml.rootName());
                document.appendChild(root);
                encode(sdo, root, registry, new java.util.IdentityHashMap<>(), 0);
                TransformerFactory factory = TransformerFactory.newInstance();
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                var transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                StringWriter out = new StringWriter();
                transformer.transform(new DOMSource(document), new StreamResult(out));
                return out.toString();
            } catch (IllegalArgumentException error) { throw error; }
            catch (Exception error) { throw new IllegalArgumentException("Cannot marshal XML SmartDataObject", error); }
        };
    }

    private static void encode(AIcSmartDataObject sdo, Element element,
            AIiSmartDataObjectBindingRegistry registry, java.util.IdentityHashMap<Object, Boolean> active, int depth) {
        if (depth > 128 || active.put(sdo, Boolean.TRUE) != null)
            throw new IllegalArgumentException("XML object cycle or nesting limit exceeded");
        try {
            var binding = registry.getBinding(sdo.getClass());
            if (binding == null) throw new IllegalArgumentException("Unregistered nested SDO: " + sdo.getClass());
            var xml = metadata(binding);
            Map<String,Object> raw = sdo.getRawValues();
            for (var field : binding.getMetadata().getFields()) {
                if (!raw.containsKey(field.getSemanticFieldName())) continue;
                XmlField descriptor = xml.fields().get(field.getSemanticFieldName());
                String namespace = descriptor == null ? "" : descriptor.namespace();
                String localName = descriptor == null || descriptor.localName().isEmpty()
                        ? field.getDataFieldName() : descriptor.localName();
                Object value = raw.get(field.getSemanticFieldName());
                if (descriptor != null && descriptor.attribute()) {
                    if (value != null) element.setAttributeNS(emptyAsNull(namespace), localName, lexicalValue(value));
                    continue;
                }
                if (value instanceof Iterable<?> items) {
                    for (Object child : items) append(element, namespace, localName, child, registry, active, depth + 1);
                } else append(element, namespace, localName, value, registry, active, depth + 1);
            }
        } finally { active.remove(sdo); }
    }

    private static void append(Element owner, String namespace, String localName, Object value,
            AIiSmartDataObjectBindingRegistry registry, java.util.IdentityHashMap<Object, Boolean> active, int depth) {
        Document document = owner.getOwnerDocument();
        Element element = document.createElementNS(emptyAsNull(namespace), localName);
        owner.appendChild(element);
        if (value instanceof AIcSmartDataObject sdo) encode(sdo, element, registry, active, depth);
        else if (value != null) element.setTextContent(lexicalValue(value));
        else element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:nil", "true");
    }

    /**
     * Converts one XML scalar to its stable external lexical representation.
     * Serializable enums must provide an explicit code, never the mutable Java constant name.
     *
     * @param aValue scalar field value
     * @return XML lexical representation
     * @throws IllegalArgumentException for non-serializable enum implementations
     */
    private static String lexicalValue(Object aValue) {
        if (aValue instanceof eu.algites.lib.common.enums.AIiEnumItem<?> locItem)
            return String.valueOf(locItem.code());
        if (aValue instanceof Enum<?> locEnum)
            throw new IllegalArgumentException("XML enum must implement AIiEnumItem: "
                    + locEnum.getDeclaringClass().getName());
        return String.valueOf(aValue);
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectUnmarshaller createUnmarshaller() {
        return (text, contract, registry) -> {
            if (text == null || text.length() > 8_000_000 || text.contains("<!DOCTYPE") || text.contains("<!ENTITY"))
                throw new IllegalArgumentException("Invalid or unsafe XML input");
            try {
                var document = builder().newDocumentBuilder().parse(new InputSource(new StringReader(text)));
                var binding = registry.resolveBinding(contract, null, null);
                XmlMetadata xml = metadata(binding);
                Element element = document.getDocumentElement();
                if (!sameName(element, xml.namespace(), xml.rootName()))
                    throw new IllegalArgumentException("Unexpected XML root QName: " + element.getTagName());
                Object sdo = AIsSmartDataObjectFactory.fromData(contract,
                        decode(element, binding, registry, 0), registry);
                return (AIiSmartDataObject) sdo;
            } catch (IllegalArgumentException error) { throw error; }
            catch (Exception error) { throw new IllegalArgumentException("Cannot unmarshal XML SmartDataObject", error); }
        };
    }

    private static Map<String,Object> decode(Element element, AIiSmartDataObjectBinding binding,
            AIiSmartDataObjectBindingRegistry registry, int depth) {
        if (depth > 128) throw new IllegalArgumentException("XML nesting limit exceeded");
        XmlMetadata xml = metadata(binding);
        Map<String,Object> result = new LinkedHashMap<>();
        for (var field : binding.getMetadata().getFields()) {
            XmlField descriptor = xml.fields().get(field.getSemanticFieldName());
            String namespace = descriptor == null ? "" : descriptor.namespace();
            String name = descriptor == null || descriptor.localName().isEmpty()
                    ? field.getDataFieldName() : descriptor.localName();
            if (descriptor != null && descriptor.attribute()) {
                if (element.hasAttributeNS(emptyAsNull(namespace), name))
                    result.put(field.getDataFieldName(), element.getAttributeNS(emptyAsNull(namespace), name));
                continue;
            }
            List<Object> values = new ArrayList<>();
            NodeList nodes = element.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node child = nodes.item(i);
                if (!(child instanceof Element childElement) || !sameName(childElement, namespace, name)) continue;
                String nil = childElement.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
                if ("true".equals(nil) || "1".equals(nil)) { values.add(null); continue; }
                Class<?> childContract = nestedContract(field.getEffectiveReturnType());
                if (childContract != null) {
                    @SuppressWarnings("unchecked")
                    Class<? extends eu.algites.lib.data.dataobject.AIiDataObject> cast =
                            (Class<? extends eu.algites.lib.data.dataobject.AIiDataObject>)childContract;
                    var nestedBinding = registry.resolveBinding(cast, binding, field.getSemanticFieldName());
                    values.add(decode(childElement, nestedBinding, registry, depth + 1));
                } else values.add(childElement.getTextContent());
            }
            if (values.isEmpty()) continue;
            if (java.util.Collection.class.isAssignableFrom(field.getEffectiveGetter().getReturnType()))
                result.put(field.getDataFieldName(), values);
            else if (values.size() == 1) result.put(field.getDataFieldName(), values.get(0));
            else throw new IllegalArgumentException("Repeated XML element for scalar field: " + name);
        }
        return result;
    }

    private static Class<?> nestedContract(java.lang.reflect.Type type) {
        if (type instanceof Class<?> cls)
            return eu.algites.lib.data.dataobject.AIiDataObject.class.isAssignableFrom(cls) ? cls : null;
        if (type instanceof java.lang.reflect.ParameterizedType parameterized) {
            for (var arg : parameterized.getActualTypeArguments()) {
                Class<?> result = nestedContract(arg);
                if (result != null) return result;
            }
        }
        if (type instanceof java.lang.reflect.WildcardType wildcard) {
            for (var bound : wildcard.getUpperBounds()) {
                Class<?> result = nestedContract(bound);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static boolean sameName(Element element, String namespace, String localName) {
        return (element.getNamespaceURI() == null ? "" : element.getNamespaceURI()).equals(namespace)
                && (element.getLocalName() == null ? element.getNodeName() : element.getLocalName()).equals(localName);
    }
    private static String emptyAsNull(String namespace) { return namespace.isEmpty() ? null : namespace; }

    private static DocumentBuilderFactory builder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static XmlMetadata metadata(AIiSmartDataObjectBinding binding) {
        return binding.getMetadata().getFormatBinding(AInTextFormatType.XML.code(), PROVIDER_ID)
                .map(XmlMetadata.class::cast).orElseThrow(() ->
                    new IllegalArgumentException("Missing XML binding metadata for " + binding.getImplementationClass()));
    }

    /** Immutable XML field QName and node-kind metadata. */
    public record XmlField(String namespace, String localName, boolean attribute) { }

    /** Immutable XML class and field binding metadata. */
    public record XmlMetadata(String namespace, String rootName, Map<String, XmlField> fields)
            implements AIiSmartDataObjectFormatBindingMetadata {
        /** {@inheritDoc} */
        @Override public String getFormatId() { return AInTextFormatType.XML.code(); }
        /** {@inheritDoc} */
        @Override public String getProviderId() { return PROVIDER_ID; }
    }
}
