package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentValueContentVariantConvention2.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentValueContent/choice/choice/1#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field Convention
 */
public record AIigNameFragmentsSegmentValueContentVariantConvention2_1(String field1) implements AIigNameFragmentsSegmentValueContent_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("Convention");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentValueContentVariantConvention2_1 {
        java.util.Objects.requireNonNull(field1, "Convention");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Convention", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Convention", order = 0)
    /** @return Value of Convention (required) */
    public String getConvention() { return field1; }
}
