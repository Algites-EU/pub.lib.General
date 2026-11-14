package eu.algites.lib.data.smartdataobject.impl.format.yaml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Safe YAML 1.2 block/flow subset for SDO maps, sequences and scalar fields.
 * Never constructs arbitrary Java objects or interprets YAML tags. Anchors,
 * merge keys, block scalar styles and user-defined tags are deliberately not
 * supported by this built-in no-dependency provider.
 *
 * @author linhart1
 */
public final class AIsYamlSyntax {
    private AIsYamlSyntax() { }

    /** @param aTree scalar/map/list data tree @return valid YAML 1.2 block document */
    public static String write(Object aTree) {
        StringBuilder out = new StringBuilder();
        emit(aTree, out, 0);
        return out.toString();
    }

    private static void emit(Object value, StringBuilder out, int depth) {
        if (depth > 128) throw new IllegalArgumentException("YAML nesting limit exceeded");
        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            for (var entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) throw new IllegalArgumentException("YAML key must be text");
                out.append(" ".repeat(depth * 2)).append(AIsYamlFlowSyntax.write(key)).append(':');
                if (isComposite(entry.getValue())) {
                    out.append('\n'); emit(entry.getValue(), out, depth + 1);
                } else out.append(' ').append(AIsYamlFlowSyntax.write(entry.getValue())).append('\n');
            }
        } else if (value instanceof List<?> list && !list.isEmpty()) {
            for (Object item : list) {
                out.append(" ".repeat(depth * 2)).append('-');
                if (isComposite(item)) { out.append('\n'); emit(item, out, depth + 1); }
                else out.append(' ').append(AIsYamlFlowSyntax.write(item)).append('\n');
            }
        } else out.append(" ".repeat(depth * 2)).append(AIsYamlFlowSyntax.write(value)).append('\n');
    }
    private static boolean isComposite(Object value) {
        return value instanceof Map<?, ?> map && !map.isEmpty() ||
                value instanceof List<?> list && !list.isEmpty();
    }

    /** @param aText YAML 1.2 text @return parsed safe map/list/scalar data tree */
    public static Object parse(String aText) {
        if (aText == null) throw new NullPointerException("aText");
        if (aText.length() > 8_000_000) throw new IllegalArgumentException("YAML input size limit exceeded");
        String trimmed = aText.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("["))
            return AIsYamlFlowSyntax.parse(trimmed);
        if (trimmed.isEmpty()) return null;
        List<Line> lines = new ArrayList<>();
        String[] source = aText.replace("\r\n", "\n").split("\n");
        for (int index = 0; index < source.length; index++) {
            String line = source[index];
            String content = line.stripLeading();
            if (content.isEmpty() || content.startsWith("#") || content.equals("---") || content.equals("...")) continue;
            int indent = line.length() - content.length();
            if (indent % 2 != 0 || line.substring(0, indent).indexOf('\t') >= 0)
                throw new IllegalArgumentException("Unsupported YAML indentation at line " + (index + 1));
            lines.add(new Line(indent / 2, content, index + 1));
        }
        if (lines.isEmpty()) return null;
        Reader reader = new Reader(lines);
        Object value = reader.block(lines.get(0).indent(), 0);
        if (reader.index != lines.size()) throw new IllegalArgumentException("Unexpected YAML content at line " + lines.get(reader.index).line());
        return value;
    }

    private record Line(int indent, String text, int line) { }
    private static final class Reader {
        final List<Line> lines;
        int index;
        Reader(List<Line> input) { lines = input; }
        Object block(int indent, int depth) {
            if (depth > 128) throw new IllegalArgumentException("YAML nesting limit exceeded");
            if (index >= lines.size()) return null;
            Line here = lines.get(index);
            if (here.indent() != indent) throw new IllegalArgumentException("Invalid YAML indent at line " + here.line());
            if (here.text().equals("-") || here.text().startsWith("- ")) {
                List<Object> items = new ArrayList<>();
                while (index < lines.size() && lines.get(index).indent() == indent &&
                        (lines.get(index).text().equals("-") || lines.get(index).text().startsWith("- "))) {
                    String part = lines.get(index++).text().substring(1).trim();
                    if (!part.isEmpty()) items.add(scalar(part));
                    else if (index < lines.size() && lines.get(index).indent() > indent)
                        items.add(block(lines.get(index).indent(), depth + 1));
                    else items.add(null);
                }
                return items;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            while (index < lines.size() && lines.get(index).indent() == indent) {
                Line line = lines.get(index++);
                String entry = line.text();
                int colon = keyColon(entry);
                if (colon < 0) throw new IllegalArgumentException("Unsupported YAML mapping at line " + line.line());
                String rawKey = entry.substring(0, colon).trim();
                String key = rawKey.startsWith("\"") ? (String)AIsYamlFlowSyntax.parse(rawKey) : rawKey;
                if (map.containsKey(key)) throw new IllegalArgumentException("Duplicate YAML key: " + key);
                String rawValue = entry.substring(colon + 1).trim();
                Object value;
                if (!rawValue.isEmpty()) value = scalar(rawValue);
                else if (index < lines.size() && lines.get(index).indent() > indent)
                    value = block(lines.get(index).indent(), depth + 1);
                else value = null;
                map.put(key, value);
            }
            return map;
        }
        static int keyColon(String text) {
            boolean quoted = false, escape = false;
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (quoted && ch == '\\' && !escape) { escape = true; continue; }
                if (ch == '"' && !escape) quoted = !quoted;
                if (ch == ':' && !quoted && (i + 1 == text.length() || Character.isWhitespace(text.charAt(i + 1)))) return i;
                escape = false;
            }
            return -1;
        }
        static Object scalar(String value) {
            if (value.startsWith("\"") || value.startsWith("{") || value.startsWith("["))
                return AIsYamlFlowSyntax.parse(value);
            if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2)
                return value.substring(1, value.length() - 1).replace("''", "'");
            if (value.equals("null") || value.equals("~")) return null;
            if (value.equals("true")) return true;
            if (value.equals("false")) return false;
            if (value.matches("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"))
                return new BigDecimal(value);
            if (value.startsWith("!") || value.startsWith("&") || value.startsWith("*"))
                throw new IllegalArgumentException("YAML tags and aliases are not supported");
            return value;
        }
    }
}
