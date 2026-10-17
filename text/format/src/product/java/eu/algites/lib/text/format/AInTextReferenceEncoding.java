package eu.algites.lib.text.format;

/** Shape of object identity metadata in a text document. */
public enum AInTextReferenceEncoding {
    /** No special reference encoding is applied. */
    NONE,
    /** JSON reference encoding compatible with .NET object identity conventions. */
    JSON_DOTNET,
    /** JSON encoding that exposes references for diagnostics. */
    JSON_DIAGNOSTIC,
    /** Native YAML anchor and alias reference encoding. */
    YAML_NATIVE,
    /** XML encoding that exposes object references for diagnostics. */
    XML_DIAGNOSTIC,
    /** XML reference representation based on schema-compatible structures. */
    XML_SCHEMA
}
