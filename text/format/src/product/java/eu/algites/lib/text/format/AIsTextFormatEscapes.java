package eu.algites.lib.text.format;

/** Format-specific scalar escaping independent of any SmartDataObject implementation. */
public final class AIsTextFormatEscapes {
    /** Encodes a Java string as a JSON string literal. */
    public static String jsonString(String aValue) {
        StringBuilder locOut = new StringBuilder("\"");
        for (int locIndex = 0; locIndex < aValue.length(); locIndex++) {
            char locChar = aValue.charAt(locIndex);
            switch (locChar) {
                case '"' -> locOut.append("\\\"");
                case '\\' -> locOut.append("\\\\");
                case '\n' -> locOut.append("\\n");
                case '\r' -> locOut.append("\\r");
                case '\t' -> locOut.append("\\t");
                default -> {
                    if (locChar < 32) locOut.append(String.format("\\u%04x", (int) locChar));
                    else locOut.append(locChar);
                }
            }
        }
        return locOut.append('"').toString();
    }
    /** Escapes character content and quoted attribute content in XML 1.0. */
    public static String xmlText(String aValue) {
        return aValue.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
    private AIsTextFormatEscapes() { throw new AssertionError("Utility class"); }
}
