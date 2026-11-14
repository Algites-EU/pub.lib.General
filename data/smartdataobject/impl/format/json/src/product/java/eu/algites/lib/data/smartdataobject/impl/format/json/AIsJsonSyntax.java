package eu.algites.lib.data.smartdataobject.impl.format.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Independent UTF-16 JSON grammar parser and serializer for format providers.
 * Rejects duplicate object member names, depth-limit overruns and non-finite
 * numeric values. No dependency on the legacy graph renderer.
 *
 * @author linhart1
 */
public final class AIsJsonSyntax {
    private AIsJsonSyntax() { }

    /**
     * Serializes a tree of maps, lists and JSON scalar values.
     * @param aTree input data tree
     * @return valid compact JSON text
     */
    public static String write(Object aTree) {
        StringBuilder result = new StringBuilder();
        writeValue(aTree, result, 0);
        return result.toString();
    }

    private static void writeValue(Object value, StringBuilder out, int depth) {
        if (depth > 128) throw new IllegalArgumentException("JSON nesting limit exceeded");
        if (value == null) { out.append("null"); return; }
        if (value instanceof String text) { quote(text, out); return; }
        if (value instanceof Boolean flag) { out.append(flag); return; }
        if (value instanceof Number number) {
            if (number instanceof Double d && !Double.isFinite(d) ||
                    number instanceof Float f && !Float.isFinite(f))
                throw new IllegalArgumentException("Non-finite JSON number");
            out.append(number);
            return;
        }
        if (value instanceof Map<?, ?> map) {
            out.append('{'); boolean first = true;
            for (var item : map.entrySet()) {
                if (!(item.getKey() instanceof String key)) throw new IllegalArgumentException("JSON object key is not a string");
                if (!first) out.append(','); first = false;
                quote(key, out); out.append(':'); writeValue(item.getValue(), out, depth + 1);
            }
            out.append('}'); return;
        }
        if (value instanceof Iterable<?> collection) {
            out.append('['); boolean first = true;
            for (Object item : collection) {
                if (!first) out.append(','); first = false;
                writeValue(item, out, depth + 1);
            }
            out.append(']'); return;
        }
        throw new IllegalArgumentException("Not a JSON tree value: " + value.getClass());
    }

    private static void quote(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (ch < 0x20 || Character.isSurrogate(ch)) {
                        out.append("\\u").append(String.format("%04x", (int) ch));
                    } else out.append(ch);
                }
            }
        }
        out.append('"');
    }

    /**
     * Parses a complete JSON value into immutable scalar / mutable ordered tree types.
     * @param aText JSON text
     * @return decoded JSON data tree
     */
    public static Object parse(String aText) {
        if (aText == null) throw new NullPointerException("aText");
        if (aText.length() > 8_000_000) throw new IllegalArgumentException("JSON input size limit exceeded");
        Parser parser = new Parser(aText);
        Object result = parser.value(0);
        parser.white();
        if (parser.pos != parser.text.length()) throw parser.bad("Unexpected trailing input");
        return result;
    }

    private static final class Parser {
        final String text;
        int pos;
        Parser(String t) { text = t; }
        IllegalArgumentException bad(String reason) {
            return new IllegalArgumentException("Invalid JSON at character " + pos + ": " + reason);
        }
        void white() {
            while (pos < text.length() && (text.charAt(pos) == ' ' || text.charAt(pos) == '\t' ||
                    text.charAt(pos) == '\r' || text.charAt(pos) == '\n')) pos++;
        }
        boolean take(char ch) {
            white();
            if (pos < text.length() && text.charAt(pos) == ch) { pos++; return true; }
            return false;
        }
        Object value(int depth) {
            if (depth > 128) throw bad("Nesting limit exceeded");
            white();
            if (pos >= text.length()) throw bad("Unexpected end of input");
            char ch = text.charAt(pos);
            if (ch == '"') return string();
            if (ch == '{') {
                pos++; Map<String, Object> map = new LinkedHashMap<>();
                if (take('}')) return map;
                do {
                    white(); if (pos >= text.length() || text.charAt(pos) != '"') throw bad("Object key must be a string");
                    String key = string();
                    if (!take(':')) throw bad("Expected colon");
                    Object item = value(depth + 1);
                    if (map.containsKey(key)) throw bad("Duplicate object property: " + key);
                    map.put(key, item);
                    if (take('}')) return map;
                } while (take(','));
                throw bad("Expected comma or closing brace");
            }
            if (ch == '[') {
                pos++; List<Object> array = new ArrayList<>();
                if (take(']')) return array;
                do {
                    array.add(value(depth + 1));
                    if (take(']')) return array;
                } while (take(','));
                throw bad("Expected comma or closing bracket");
            }
            if (text.startsWith("true", pos)) { pos += 4; return true; }
            if (text.startsWith("false", pos)) { pos += 5; return false; }
            if (text.startsWith("null", pos)) { pos += 4; return null; }
            if (ch == '-' || ch >= '0' && ch <= '9') return number();
            throw bad("Unexpected token");
        }
        String string() {
            pos++; StringBuilder out = new StringBuilder();
            while (pos < text.length()) {
                char ch = text.charAt(pos++);
                if (ch == '"') return out.toString();
                if (ch < 0x20) throw bad("Control character in string");
                if (ch != '\\') { out.append(ch); continue; }
                if (pos >= text.length()) throw bad("Incomplete escape");
                switch (text.charAt(pos++)) {
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    case '/' -> out.append('/');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (pos + 4 > text.length()) throw bad("Incomplete unicode escape");
                        try { out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16)); }
                        catch (NumberFormatException error) { throw bad("Invalid unicode escape"); }
                        pos += 4;
                    }
                    default -> throw bad("Unknown string escape");
                }
            }
            throw bad("Unterminated string");
        }
        BigDecimal number() {
            int start = pos;
            if (text.charAt(pos) == '-') pos++;
            if (pos >= text.length()) throw bad("Incomplete number");
            if (text.charAt(pos) == '0') pos++;
            else {
                if (text.charAt(pos) < '1' || text.charAt(pos) > '9') throw bad("Invalid number");
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
            }
            if (pos < text.length() && text.charAt(pos) == '.') {
                pos++;
                int startFraction = pos;
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
                if (pos == startFraction) throw bad("Empty fraction");
            }
            if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
                pos++;
                if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) pos++;
                int startExponent = pos;
                while (pos < text.length() && Character.isDigit(text.charAt(pos))) pos++;
                if (pos == startExponent) throw bad("Empty exponent");
            }
            try { return new BigDecimal(text.substring(start, pos)); }
            catch (NumberFormatException error) { throw bad("Invalid decimal number"); }
        }
    }
}
