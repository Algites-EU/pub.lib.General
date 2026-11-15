package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentValueContentVariantValue1.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentValueContent/choice/choice/0#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field Value
 */
public record AIigNameFragmentsSegmentValueContentVariantValue1_1(String field1) implements AIigNameFragmentsSegmentValueContent_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("Value");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentValueContentVariantValue1_1 {
        java.util.Objects.requireNonNull(field1, "Value");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Value", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Value", order = 0)
    /** @return Value of Value (required) */
    public String getValue() { return field1; }
}
