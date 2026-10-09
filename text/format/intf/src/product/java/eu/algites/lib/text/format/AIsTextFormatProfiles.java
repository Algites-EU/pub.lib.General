package eu.algites.lib.text.format;

import java.util.EnumSet;

/** Non-instantiable catalog of stable text format protocol descriptors. */
public final class AIsTextFormatProfiles {
    private static final EnumSet<AInTextOutputObjectReferenceMode> ALL =
            EnumSet.allOf(AInTextOutputObjectReferenceMode.class);
    private static final EnumSet<AInTextOutputObjectReferenceMode> NONE =
            EnumSet.of(AInTextOutputObjectReferenceMode.NO_REFERENCES);

    public static final AIiTextFormatProfile JSON_STANDARD = new AIcdTextFormatProfile(
            "json-standard-1", AInTextFormatType.JSON, AInTextReferenceEncoding.NONE, false, NONE);
    public static final AIiTextFormatProfile JSON_DOTNET_PRESERVE = new AIcdTextFormatProfile(
            "json-dotnet-preserve-1", AInTextFormatType.JSON, AInTextReferenceEncoding.JSON_DOTNET, false,
            EnumSet.of(AInTextOutputObjectReferenceMode.ALL_OBJECTS,
                AInTextOutputObjectReferenceMode.CYCLIC_REFERENCES_ONLY,
                AInTextOutputObjectReferenceMode.REPEATED_OBJECTS));
    public static final AIiTextFormatProfile JSON_DIAGNOSTIC = new AIcdTextFormatProfile(
            "json-diagnostic-1", AInTextFormatType.JSON, AInTextReferenceEncoding.JSON_DIAGNOSTIC, true, ALL);
    public static final AIiTextFormatProfile YAML_STANDARD = new AIcdTextFormatProfile(
            "yaml-standard-1", AInTextFormatType.YAML, AInTextReferenceEncoding.NONE, false, NONE);
    public static final AIiTextFormatProfile YAML_NATIVE_REFERENCES = new AIcdTextFormatProfile(
            "yaml-native-references-1", AInTextFormatType.YAML, AInTextReferenceEncoding.YAML_NATIVE, false, ALL);
    public static final AIiTextFormatProfile YAML_DIAGNOSTIC = new AIcdTextFormatProfile(
            "yaml-diagnostic-1", AInTextFormatType.YAML, AInTextReferenceEncoding.YAML_NATIVE, true, ALL);
    public static final AIiTextFormatProfile XML_XSD = new AIcdTextFormatProfile(
            "xml-xsd-1", AInTextFormatType.XML, AInTextReferenceEncoding.XML_SCHEMA, false, NONE);
    public static final AIiTextFormatProfile XML_DIAGNOSTIC = new AIcdTextFormatProfile(
            "xml-diagnostic-1", AInTextFormatType.XML, AInTextReferenceEncoding.XML_DIAGNOSTIC, true, ALL);

    private AIsTextFormatProfiles() { throw new AssertionError("Utility class"); }
}
