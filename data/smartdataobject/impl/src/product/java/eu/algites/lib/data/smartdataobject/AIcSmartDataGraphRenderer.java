package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AIsTextFormatEscapes;
import eu.algites.lib.text.format.AInTextFormatType;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AInTextReferenceEncoding;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Internal profile-driven graph renderer. No generated type depends on this class. */
final class AIcSmartDataGraphRenderer {
    private final AIcSmartDataGraphSnapshot snapshot;
    private final AIiTextFormatProfile profile;
    private final AInTextOutputObjectReferenceMode mode;
    private final boolean effective;
    private final IdentityHashMap<AIcSmartDataGraphSnapshot.AIcNode, Integer> visits = new IdentityHashMap<>();
    private final Set<AIcSmartDataGraphSnapshot.AIcNode> cycleTargets = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<AIcSmartDataGraphSnapshot.AIcNode> expanded = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<AIcSmartDataGraphSnapshot.AIcNode> active = Collections.newSetFromMap(new IdentityHashMap<>());
    private final IdentityHashMap<AIcSmartDataGraphSnapshot.AIcNode, Integer> ids = new IdentityHashMap<>();
    private final IdentityHashMap<AIcSmartDataGraphSnapshot.AIcNode, Integer> approximateSizes = new IdentityHashMap<>();
    private int nextId = 1;
    private final Map<String, String> namespacePrefixes = new LinkedHashMap<>();

    static String render(AIcSmartDataGraphSnapshot aSnapshot, AIiTextFormatProfile aProfile,
            AInTextOutputObjectReferenceMode aMode, boolean aEffective) {
        if (aProfile == null || aMode == null || !aProfile.isReferenceModeSupported(aMode))
            throw new IllegalArgumentException("Reference mode is not supported by this text format profile");
        var locRenderer = new AIcSmartDataGraphRenderer(aSnapshot, aProfile, aMode, aEffective);
        locRenderer.scan(aSnapshot.root(), Collections.newSetFromMap(new IdentityHashMap<>()));
        if (aProfile.getFormatType() == AInTextFormatType.XML && !aProfile.isDiagnostic())
            locRenderer.collectNamespaces();
        if (aProfile.getFormatType() == AInTextFormatType.XML) {
            StringBuilder locOut = new StringBuilder();
            locRenderer.xml(aSnapshot.root(), locOut, 0, null, false);
            return locOut.toString();
        }
        if (aProfile.getFormatType() == AInTextFormatType.YAML &&
                aProfile.getReferenceEncoding() == AInTextReferenceEncoding.YAML_NATIVE)
            return locRenderer.yaml(aSnapshot.root());
        Object locTree = locRenderer.jsonValue(aSnapshot.root());
        String locJson = jsonText(locTree);
        /* JSON is a YAML 1.2 flow-style document, preserving scalars and ordering. */
        return locJson + "\n";
    }

    private AIcSmartDataGraphRenderer(AIcSmartDataGraphSnapshot aSnapshot, AIiTextFormatProfile aProfile,
            AInTextOutputObjectReferenceMode aMode, boolean aEffective) {
        snapshot = aSnapshot;
        profile = aProfile;
        mode = aMode;
        effective = aEffective;
    }

    private List<AIcSmartDataGraphSnapshot.AIcNode> children(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        List<AIcSmartDataGraphSnapshot.AIcNode> locChildren = new ArrayList<>();
        switch (aNode.kind) {
            case SMART -> {
                for (var locField : aNode.fields) {
                    if (locField.present || (effective && locField.descriptor.hasDefault()))
                        locChildren.add(snapshot.fieldValue(locField, effective));
                }
            }
            case SEQUENCE -> locChildren.addAll(aNode.elements);
            case MAPPING -> {
                for (var locEntry : aNode.entries.entrySet()) {
                    locChildren.add(locEntry.getKey());
                    locChildren.add(locEntry.getValue());
                }
            }
            default -> { }
        }
        return locChildren;
    }

    private void scan(AIcSmartDataGraphSnapshot.AIcNode aNode,
            Set<AIcSmartDataGraphSnapshot.AIcNode> aActive) {
        visits.merge(aNode, 1, Integer::sum);
        if (aActive.contains(aNode)) {
            cycleTargets.add(aNode);
            return;
        }
        if (expanded.contains(aNode)) return;
        expanded.add(aNode);
        aActive.add(aNode);
        for (var locChild : children(aNode)) scan(locChild, aActive);
        aActive.remove(aNode);
    }

    private boolean referenceCandidate(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.NULL) return false;
        if (profile.getReferenceEncoding() == AInTextReferenceEncoding.JSON_DOTNET && !aNode.isComposite()) return false;
        if (profile.getReferenceEncoding() == AInTextReferenceEncoding.XML_SCHEMA ||
                profile.getReferenceEncoding() == AInTextReferenceEncoding.NONE) return false;
        return true;
    }

    private int approximateSize(AIcSmartDataGraphSnapshot.AIcNode aNode,
            Set<AIcSmartDataGraphSnapshot.AIcNode> aPath) {
        Integer locCached = approximateSizes.get(aNode);
        if (locCached != null) return locCached;
        if (!aPath.add(aNode)) return 16;
        int locSize = 0;
        if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.NULL) locSize = 4;
        else if (!aNode.isComposite()) {
            locSize = (aNode.scalar instanceof Number || aNode.scalar instanceof Boolean)
                    ? String.valueOf(aNode.scalar).length() : quote(String.valueOf(aNode.scalar)).length();
        } else {
            locSize = 2;
            if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART) {
                for (var locField : aNode.fields) {
                    if (!locField.present && !profile.isDiagnostic() &&
                            (!effective || !locField.descriptor.hasDefault())) continue;
                    locSize += quote(locField.name()).length() + 2;
                    if (locField.present || (effective && locField.descriptor.hasDefault()))
                        locSize += approximateSize(snapshot.fieldValue(locField, effective), aPath);
                    if (profile.isDiagnostic()) locSize += 20;
                }
            } else {
                for (var locChild : children(aNode)) locSize += approximateSize(locChild, aPath) + 2;
            }
        }
        aPath.remove(aNode);
        approximateSizes.put(aNode, locSize);
        return locSize;
    }
    private boolean sizeOptimized(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        if (cycleTargets.contains(aNode)) return true;
        int locOccurrences = visits.getOrDefault(aNode, 0);
        if (locOccurrences <= 1) return false;
        int locOriginal = approximateSize(aNode, Collections.newSetFromMap(new IdentityHashMap<>()));
        int locReference = profile.getFormatType() == AInTextFormatType.YAML ? 5 :
                profile.getFormatType() == AInTextFormatType.XML ? 23 : 18;
        int locIdCost = profile.getFormatType() == AInTextFormatType.YAML ? 5 :
                profile.getFormatType() == AInTextFormatType.XML ? 12 : 20;
        return (long) (locOccurrences - 1) * (locOriginal - locReference) > locIdCost;
    }

    private boolean shouldNumber(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        if (!referenceCandidate(aNode)) return false;
        return switch (mode) {
            case NO_REFERENCES -> false;
            case ALL_OBJECTS -> true;
            case CYCLIC_REFERENCES_ONLY -> cycleTargets.contains(aNode);
            case REPEATED_OBJECTS -> visits.getOrDefault(aNode, 0) > 1;
            case OPTIMIZED_TEXT_OUTPUT_SIZE -> sizeOptimized(aNode);
        };
    }

    private Integer referenceOrRegister(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        if (active.contains(aNode)) {
            if (mode == AInTextOutputObjectReferenceMode.NO_REFERENCES || !referenceCandidate(aNode))
                throw new IllegalStateException("Cyclic graph cannot be represented by this text profile");
            return ids.computeIfAbsent(aNode, aUnused -> nextId++);
        }
        if (mode != AInTextOutputObjectReferenceMode.CYCLIC_REFERENCES_ONLY &&
                shouldNumber(aNode) && ids.containsKey(aNode)) return ids.get(aNode);
        if (shouldNumber(aNode)) ids.computeIfAbsent(aNode, aUnused -> nextId++);
        return null;
    }

    private Object jsonValue(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        Integer locRef = referenceOrRegister(aNode);
        if (locRef != null) {
            return Map.of(profile.isDiagnostic() ? "dNrRef" : "$ref", String.valueOf(locRef));
        }
        Integer locId = shouldNumber(aNode) ? ids.get(aNode) : null;
        active.add(aNode);
        try {
            Object locValue;
            switch (aNode.kind) {
                case NULL -> locValue = null;
                case SCALAR -> locValue = aNode.scalar instanceof Character || aNode.scalar instanceof Enum<?>
                        ? String.valueOf(aNode.scalar) : aNode.scalar;
                case OPAQUE -> {
                    if (!profile.isDiagnostic())
                        throw new IllegalArgumentException("Canonical text output requires a supported scalar or structured data type: " + aNode.original.getClass());
                    locValue = aNode.scalar;
                }
                case SEQUENCE -> {
                    List<Object> locList = new ArrayList<>();
                    for (var locChild : aNode.elements) locList.add(jsonValue(locChild));
                    locValue = locList;
                }
                case MAPPING -> {
                    Map<String, Object> locMap = new LinkedHashMap<>();
                    for (var locEntry : aNode.entries.entrySet()) {
                        Object locKey = locEntry.getKey().scalar;
                        if (!(locKey instanceof String)) throw new IllegalArgumentException("JSON object keys must be strings");
                        locMap.put((String) locKey, jsonValue(locEntry.getValue()));
                    }
                    locValue = locMap;
                }
                case SMART -> {
                    Map<String, Object> locFields = new LinkedHashMap<>();
                    for (var locField : aNode.fields) {
                        if (!locField.present && !profile.isDiagnostic() &&
                                (!effective || !locField.descriptor.hasDefault())) continue;
                        Object locFieldValue = locField.present || (effective && locField.descriptor.hasDefault())
                                ? jsonValue(snapshot.fieldValue(locField, effective)) : null;
                        if (profile.isDiagnostic()) {
                            Map<String, Object> locWrapper = new LinkedHashMap<>();
                            locWrapper.put("present", locField.present);
                            if (locField.present) locWrapper.put("value", locFieldValue);
                            locFields.put(locField.name(), locWrapper);
                        } else locFields.put(locField.name(), locFieldValue);
                    }
                    if (profile.isDiagnostic()) {
                        Map<String, Object> locMeta = new LinkedHashMap<>();
                        locMeta.put("type", aNode.original.getClass().getSimpleName());
                        String locHash = AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema() == null ? "" :
                                AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema().fingerprint();
                        if (!locHash.isBlank()) locMeta.put("schemaHash", locHash);
                        if (locId != null) locMeta.put("dNr", locId);
                        locValue = Map.of("$meta", locMeta, "$fields", locFields);
                        locId = null;
                    } else locValue = locFields;
                }
                default -> throw new IllegalStateException("Unhandled graph node");
            }
            if (locId != null) {
                Map<String, Object> locWrap = new LinkedHashMap<>();
                if (profile.isDiagnostic()) {
                    locWrap.put("$meta", Map.of("dNr", locId));
                    locWrap.put("$value", locValue);
                } else if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.MAPPING ||
                        aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART) {
                    Map<String, Object> locMembers = (Map<String, Object>) locValue;
                    if (locMembers.containsKey("$id") || locMembers.containsKey("$ref") || locMembers.containsKey("$values"))
                        throw new IllegalArgumentException("Reserved JSON reference metadata collides with a data property");
                    locWrap.put("$id", String.valueOf(locId));
                    locWrap.putAll(locMembers);
                } else {
                    locWrap.put("$id", String.valueOf(locId));
                    locWrap.put("$values", locValue);
                }
                return locWrap;
            }
            return locValue;
        } finally { active.remove(aNode); }
    }

    private static String jsonText(Object aValue) {
        if (aValue == null) return "null";
        if (aValue instanceof Boolean || aValue instanceof Number) {
            if (aValue instanceof Double d && !Double.isFinite(d) || aValue instanceof Float f && !Float.isFinite(f))
                throw new IllegalArgumentException("JSON cannot represent NaN or Infinity");
            return String.valueOf(aValue);
        }
        if (aValue instanceof Map<?, ?> locMap) {
            StringBuilder locOut = new StringBuilder("{");
            for (var locEntry : locMap.entrySet()) {
                if (locOut.length() > 1) locOut.append(',');
                locOut.append(quote(String.valueOf(locEntry.getKey()))).append(':').append(jsonText(locEntry.getValue()));
            }
            return locOut.append('}').toString();
        }
        if (aValue instanceof List<?> locList) {
            StringBuilder locOut = new StringBuilder("[");
            for (Object locItem : locList) {
                if (locOut.length() > 1) locOut.append(',');
                locOut.append(jsonText(locItem));
            }
            return locOut.append(']').toString();
        }
        return quote(String.valueOf(aValue));
    }
    private static String quote(String aText) {
        return AIsTextFormatEscapes.jsonString(aText);
    }

    private String yaml(AIcSmartDataGraphSnapshot.AIcNode aRoot) {
        StringBuilder locOut = new StringBuilder();
        yamlNode(aRoot, locOut, 0);
        return locOut.toString();
    }
    private void yamlNode(AIcSmartDataGraphSnapshot.AIcNode aNode, StringBuilder aOut, int aDepth) {
        Integer locRef = referenceOrRegister(aNode);
        if (locRef != null) {
            aOut.append('*').append('o').append(locRef).append('\n');
            return;
        }
        Integer locId = shouldNumber(aNode) ? ids.get(aNode) : null;
        if (locId != null) aOut.append('&').append('o').append(locId).append('\n');
        else if (aDepth > 0 && aNode.isComposite()) aOut.append('\n');
        active.add(aNode);
        try {
            if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART) {
                for (var locField : aNode.fields) {
                    if (!locField.present && !profile.isDiagnostic() && (!effective || !locField.descriptor.hasDefault())) continue;
                    aOut.append(" ".repeat(aDepth)).append(quote(locField.name())).append(':');
                    if (profile.isDiagnostic()) {
                        aOut.append("\n").append(" ".repeat(aDepth + 2)).append("present: ").append(locField.present).append('\n');
                        if (locField.present) {
                            aOut.append(" ".repeat(aDepth + 2)).append("value: ");
                            yamlNode(snapshot.fieldValue(locField, effective), aOut, aDepth + 4);
                        }
                    } else {
                        aOut.append(' ');
                        yamlNode(snapshot.fieldValue(locField, effective), aOut, aDepth + 2);
                    }
                }
                if (aNode.fields.isEmpty()) aOut.append(" ".repeat(aDepth)).append("{}\n");
            } else if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SEQUENCE) {
                if (aNode.elements.isEmpty()) aOut.append(" ".repeat(aDepth)).append("[]\n");
                for (var locValue : aNode.elements) {
                    aOut.append(" ".repeat(aDepth)).append("- ");
                    yamlNode(locValue, aOut, aDepth + 2);
                }
            } else if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.MAPPING) {
                if (aNode.entries.isEmpty()) aOut.append(" ".repeat(aDepth)).append("{}\n");
                for (var locEntry : aNode.entries.entrySet()) {
                    aOut.append(" ".repeat(aDepth)).append(quote(String.valueOf(locEntry.getKey().scalar))).append(": ");
                    yamlNode(locEntry.getValue(), aOut, aDepth + 2);
                }
            } else {
                if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.OPAQUE && !profile.isDiagnostic())
                    throw new IllegalArgumentException("Unsupported opaque canonical YAML value");
                aOut.append(jsonText(aNode.scalar)).append('\n');
            }
        } finally { active.remove(aNode); }
    }

    private void collectNamespaces() {
        Set<AIcSmartDataGraphSnapshot.AIcNode> locSeen = Collections.newSetFromMap(new IdentityHashMap<>());
        collectNamespaces(snapshot.root(), locSeen);
    }
    private void collectNamespaces(AIcSmartDataGraphSnapshot.AIcNode aNode,
            Set<AIcSmartDataGraphSnapshot.AIcNode> aSeen) {
        if (!aSeen.add(aNode)) return;
        if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART) {
            var locSchema = AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema();
            if (locSchema != null) addNamespace(locSchema.xmlNamespace());
            for (var locField : aNode.fields) addNamespace(locField.descriptor.annotation().xmlNamespace());
        }
        for (var locChild : children(aNode)) collectNamespaces(locChild, aSeen);
    }
    private void addNamespace(String aUri) {
        if (aUri != null && !aUri.isEmpty())
            namespacePrefixes.computeIfAbsent(aUri, aUnused -> "ns" + (namespacePrefixes.size() + 1));
    }
    private String qname(String aLocalName, String aNamespace) {
        if (aNamespace == null || aNamespace.isBlank()) return aLocalName;
        String locPrefix = namespacePrefixes.get(aNamespace);
        if (locPrefix == null) throw new IllegalStateException("Undeclared XML namespace: " + aNamespace);
        return locPrefix + ":" + aLocalName;
    }

    private void xml(AIcSmartDataGraphSnapshot.AIcNode aNode, StringBuilder aOut, int aDepth,
            String aName, boolean aInsideField) {
        String locName = aName == null ? aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART
                ? AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema() != null &&
                  !AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema().xmlLocalName().isBlank()
                  ? AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema().xmlLocalName()
                  : aNode.original.getClass().getSimpleName() : "Value" : aName;
        if (!locName.matches("([A-Za-z_][A-Za-z0-9_.-]*:)?[A-Za-z_][A-Za-z0-9_.-]*"))
            throw new IllegalArgumentException("Invalid XML local name: " + locName);
        Integer locRef = referenceOrRegister(aNode);
        if (aName == null && aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART && !profile.isDiagnostic()) {
            var locSchema = AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema();
            if (locSchema != null) locName = qname(locName, locSchema.xmlNamespace());
        }
        aOut.append("  ".repeat(aDepth)).append('<').append(locName);
        if (aDepth == 0 && !profile.isDiagnostic()) {
            for (var locNamespace : namespacePrefixes.entrySet())
                aOut.append(" xmlns:").append(locNamespace.getValue())
                        .append("=\"").append(xmlEscape(locNamespace.getKey())).append('"');
        }
        if (locRef != null) {
            aOut.append(" dNrRef=\"").append(locRef).append("\"/>\n");
            return;
        }
        Integer locId = shouldNumber(aNode) ? ids.get(aNode) : null;
        if (locId != null && profile.isDiagnostic()) aOut.append(" dNr=\"").append(locId).append('"');
        if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.NULL) {
            aOut.append(" xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>\n");
            return;
        }
        active.add(aNode);
        try {
            if (!aNode.isComposite()) {
                if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.OPAQUE && !profile.isDiagnostic())
                    throw new IllegalArgumentException("Unsupported opaque canonical XML value");
                aOut.append('>').append(xmlEscape(String.valueOf(aNode.scalar))).append("</").append(locName).append(">\n");
                return;
            }
            if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SMART) {
                var locSchema = AIcSmartDataMetadata.forReadContract(aNode.original.getClass()).schema();
                if (profile.isDiagnostic()) {
                    if (locSchema != null && !locSchema.fingerprint().isBlank())
                        aOut.append(" schemaHash=\"").append(xmlEscape(locSchema.fingerprint())).append('"');
                } else {
                    for (var locField : aNode.fields) {
                        if (locField.descriptor.annotation().xmlAttribute() &&
                                (locField.present || effective && locField.descriptor.hasDefault())) {
                            var locValue = snapshot.fieldValue(locField, effective);
                            if (locValue.isComposite()) throw new IllegalArgumentException("XML attribute must be scalar");
                            if (locValue.kind != AIcSmartDataGraphSnapshot.AInNodeKind.NULL)
                                aOut.append(' ').append(qname(
                                        locField.descriptor.annotation().xmlLocalName().isBlank() ?
                                        locField.name() : locField.descriptor.annotation().xmlLocalName(),
                                        locField.descriptor.annotation().xmlNamespace()))
                                        .append("=\"").append(xmlEscape(String.valueOf(locValue.scalar))).append('"');
                        }
                    }
                }
                aOut.append(">\n");
                for (var locField : aNode.fields) {
                    if (locField.descriptor.annotation().xmlAttribute() && !profile.isDiagnostic()) continue;
                    if (!locField.present && !profile.isDiagnostic() && (!effective || !locField.descriptor.hasDefault())) continue;
                    String locFieldName = locField.descriptor.annotation().xmlLocalName().isBlank()
                            ? locField.name() : locField.descriptor.annotation().xmlLocalName();
                    if (profile.isDiagnostic()) {
                        aOut.append("  ".repeat(aDepth + 1)).append('<').append(locFieldName)
                            .append(" present=\"").append(locField.present).append('"');
                        if (!locField.present) aOut.append("/>\n");
                        else {
                            aOut.append(">\n");
                            xml(snapshot.fieldValue(locField, effective), aOut, aDepth + 2, "Value", true);
                            aOut.append("  ".repeat(aDepth + 1)).append("</").append(locFieldName).append(">\n");
                        }
                    } else xml(snapshot.fieldValue(locField, effective), aOut, aDepth + 1,
                            qname(locFieldName, locField.descriptor.annotation().xmlNamespace()), true);
                }
            } else if (aNode.kind == AIcSmartDataGraphSnapshot.AInNodeKind.SEQUENCE) {
                aOut.append(">\n");
                for (var locItem : aNode.elements) xml(locItem, aOut, aDepth + 1, "Item", false);
            } else {
                aOut.append(">\n");
                for (var locEntry : aNode.entries.entrySet()) {
                    aOut.append("  ".repeat(aDepth + 1)).append("<Entry>\n");
                    xml(locEntry.getKey(), aOut, aDepth + 2, "Key", false);
                    xml(locEntry.getValue(), aOut, aDepth + 2, "Value", false);
                    aOut.append("  ".repeat(aDepth + 1)).append("</Entry>\n");
                }
            }
            aOut.append("  ".repeat(aDepth)).append("</").append(locName).append(">\n");
        } finally { active.remove(aNode); }
    }
    private static String xmlEscape(String aValue) {
        return AIsTextFormatEscapes.xmlText(aValue);
    }
    private AIcSmartDataGraphRenderer() { throw new AssertionError(); }
}
