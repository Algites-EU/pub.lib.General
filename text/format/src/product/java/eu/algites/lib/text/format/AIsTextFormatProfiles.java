package eu.algites.lib.text.format;

import java.util.EnumSet;

/** Non-instantiable catalog of stable text format protocol descriptors. */
public final class AIsTextFormatProfiles {
    private static final EnumSet<AInTextOutputObjectReferenceMode> ALL =
            EnumSet.allOf(AInTextOutputObjectReferenceMode.class);
    private static final EnumSet<AInTextOutputObjectReferenceMode> NONE =
            EnumSet.of(AInTextOutputObjectReferenceMode.NO_REFERENCES);

    /**
     * Standard JSON formatting without nonstandard reference markers.
     */
    public static final AIiTextFormatProfile JSON_STANDARD = new AIcdTextFormatProfile(
            "json-standard-1", AInTextFormatType.JSON, AInTextReferenceEncoding.NONE, false, NONE);
    /**
     * JSON profile retaining .NET-compatible object identity.
     */
    public static final AIiTextFormatProfile JSON_DOTNET_PRESERVE = new AIcdTextFormatProfile(
            "json-dotnet-preserve-1", AInTextFormatType.JSON, AInTextReferenceEncoding.JSON_DOTNET, false,
            EnumSet.of(AInTextOutputObjectReferenceMode.ALL_OBJECTS,
                AInTextOutputObjectReferenceMode.CYCLIC_REFERENCES_ONLY,
                AInTextOutputObjectReferenceMode.REPEATED_OBJECTS));
    /**
     * JSON encoding that exposes references for diagnostics.
     */
    public static final AIiTextFormatProfile JSON_DIAGNOSTIC = new AIcdTextFormatProfile(
            "json-diagnostic-1", AInTextFormatType.JSON, AInTextReferenceEncoding.JSON_DIAGNOSTIC, true, ALL);
    /**
     * Standard YAML formatting profile.
     */
    public static final AIiTextFormatProfile YAML_STANDARD = new AIcdTextFormatProfile(
            "yaml-standard-1", AInTextFormatType.YAML, AInTextReferenceEncoding.NONE, false, NONE);
    /**
     * YAML profile preserving shared references with anchors and aliases.
     */
    public static final AIiTextFormatProfile YAML_NATIVE_REFERENCES = new AIcdTextFormatProfile(
            "yaml-native-references-1", AInTextFormatType.YAML, AInTextReferenceEncoding.YAML_NATIVE, false, ALL);
    /**
     * YAML profile exposing references for diagnostic purposes.
     */
    public static final AIiTextFormatProfile YAML_DIAGNOSTIC = new AIcdTextFormatProfile(
            "yaml-diagnostic-1", AInTextFormatType.YAML, AInTextReferenceEncoding.YAML_NATIVE, true, ALL);
    /**
     * XML formatting profile designed for XSD-constrained documents.
     */
    public static final AIiTextFormatProfile XML_XSD = new AIcdTextFormatProfile(
            "xml-xsd-1", AInTextFormatType.XML, AInTextReferenceEncoding.XML_SCHEMA, false, NONE);
    /**
     * XML encoding that exposes object references for diagnostics.
     */
    public static final AIiTextFormatProfile XML_DIAGNOSTIC = new AIcdTextFormatProfile(
            "xml-diagnostic-1", AInTextFormatType.XML, AInTextReferenceEncoding.XML_DIAGNOSTIC, true, ALL);

    private AIsTextFormatProfiles() { throw new AssertionError("Utility class"); }
}
