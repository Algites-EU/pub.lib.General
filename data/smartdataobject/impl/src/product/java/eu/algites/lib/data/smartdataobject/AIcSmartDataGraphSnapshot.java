package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Internal identity-preserving snapshot shared by validators and text renderers. */
final class AIcSmartDataGraphSnapshot {
    enum AInNodeKind { NULL, SCALAR, OPAQUE, SMART, SEQUENCE, MAPPING }

    static final class AIcNode {
        final AInNodeKind kind;
        final Object original;
        final Object scalar;
        final List<AIcField> fields = new ArrayList<>();
        final List<AIcNode> elements = new ArrayList<>();
        final Map<AIcNode, AIcNode> entries = new LinkedHashMap<>();
        AIcNode(AInNodeKind aKind, Object aOriginal, Object aScalar) {
            kind = aKind;
            original = aOriginal;
            scalar = aScalar;
        }
        boolean isComposite() { return kind == AInNodeKind.SMART || kind == AInNodeKind.SEQUENCE || kind == AInNodeKind.MAPPING; }
    }
    static final class AIcField {
        final AIcSmartDataMetadata.AIcdField descriptor;
        final boolean present;
        final AIcNode raw;
        AIcNode effective;
        boolean effectiveResolved;
        AIcField(AIcSmartDataMetadata.AIcdField aDescriptor, boolean aPresent, AIcNode aRaw) {
            descriptor = aDescriptor;
            present = aPresent;
            raw = aRaw;
            effective = aRaw;
            effectiveResolved = aPresent;
        }
        String name() { return descriptor.name(); }
    }

    private static final int MAX_NODES = 50000;
    private final IdentityHashMap<Object, AIcNode> identity = new IdentityHashMap<>();
    private final AIcNode root;
    private final AIcNode nil = new AIcNode(AInNodeKind.NULL, null, null);

    private AIcSmartDataGraphSnapshot(AIcSmartDataObject aRoot) { root = build(aRoot); }
    static AIcSmartDataGraphSnapshot capture(AIcSmartDataObject aRoot) {
        return new AIcSmartDataGraphSnapshot(aRoot);
    }
    AIcNode root() { return root; }

    private AIcNode build(Object aValue) {
        if (aValue == null) return nil;
        AIcNode locExisting = identity.get(aValue);
        if (locExisting != null) return locExisting;
        if (identity.size() >= MAX_NODES) throw new IllegalStateException("SmartData graph node limit exceeded");
        AInNodeKind locKind;
        if (aValue instanceof AIiDataObject) locKind = AInNodeKind.SMART;
        else if (aValue instanceof Map<?, ?>) locKind = AInNodeKind.MAPPING;
        else if (aValue instanceof Iterable<?> || aValue.getClass().isArray()) locKind = AInNodeKind.SEQUENCE;
        else if (aValue instanceof String || aValue instanceof Number || aValue instanceof Boolean ||
                aValue instanceof Character || aValue instanceof Enum<?>) locKind = AInNodeKind.SCALAR;
        else locKind = AInNodeKind.OPAQUE;
        Object locText = locKind == AInNodeKind.OPAQUE ? String.valueOf(aValue) : aValue;
        AIcNode locNode = new AIcNode(locKind, aValue, locText);
        identity.put(aValue, locNode);
        if (locKind == AInNodeKind.SMART) {
            AIcSmartDataObject locSmart = aValue instanceof AIcSmartDataObject locData ? locData : null;
            for (var locDescriptor : AIcSmartDataMetadata.forReadContract(aValue.getClass()).fields()) {
                boolean locPresent = locSmart == null || locSmart.hasRaw(locDescriptor.name());
                Object locRaw = null;
                if (locPresent) {
                    if (locSmart != null) locRaw = locSmart.raw(locDescriptor.name());
                    else {
                        try { locRaw = locDescriptor.getter().invoke(aValue); }
                        catch (ReflectiveOperationException aException) {
                            throw new IllegalStateException("Cannot read schema getter: " + locDescriptor.getter(), aException);
                        }
                    }
                }
                locNode.fields.add(new AIcField(locDescriptor, locPresent,
                        locPresent ? build(locRaw) : nil));
            }
        } else if (locKind == AInNodeKind.MAPPING) {
            for (var locEntry : ((Map<?, ?>) aValue).entrySet()) {
                locNode.entries.put(build(locEntry.getKey()), build(locEntry.getValue()));
            }
        } else if (aValue instanceof Iterable<?> locIterable) {
            for (Object locEntry : locIterable) locNode.elements.add(build(locEntry));
        } else if (aValue.getClass().isArray()) {
            for (int locIndex = 0; locIndex < Array.getLength(aValue); locIndex++)
                locNode.elements.add(build(Array.get(aValue, locIndex)));
        }
        return locNode;
    }

    AIcNode fieldValue(AIcField aField, boolean aEffective) {
        if (!aEffective || aField.present) return aField.raw;
        if (!aField.effectiveResolved) {
            aField.effectiveResolved = true;
            if (aField.descriptor.hasDefault()) aField.effective = build(aField.descriptor.createDefault());
            else aField.effective = nil;
        }
        return aField.effective;
    }

    List<AIcdSmartDataValidationIssue> localIssues(boolean aEffective) {
        List<AIcdSmartDataValidationIssue> locIssues = new ArrayList<>();
        validateOwn(root, "", aEffective, locIssues);
        return List.copyOf(locIssues);
    }

    List<AIcdSmartDataValidationIssue> deepIssues(boolean aEffective) {
        List<AIcdSmartDataValidationIssue> locIssues = new ArrayList<>();
        Set<AIcNode> locVisited = Collections.newSetFromMap(new IdentityHashMap<>());
        deep(root, "", aEffective, locIssues, locVisited);
        return List.copyOf(locIssues);
    }

    private void validateOwn(AIcNode aNode, String aPath, boolean aEffective,
            List<AIcdSmartDataValidationIssue> aIssues) {
        if (aNode.kind != AInNodeKind.SMART) return;
        for (AIcField locField : aNode.fields) {
            var locDefinition = locField.descriptor.annotation();
            String locPath = aPath.isEmpty() ? locField.name() : aPath + "." + locField.name();
            if (!locField.present && locDefinition.presenceRequired() &&
                    (!aEffective || !locField.descriptor.hasDefault())) {
                aIssues.add(new AIcdSmartDataValidationIssue(locPath, "REQUIRED_ABSENT", "Required property is not present"));
                continue;
            }
            if (!locField.present && (!aEffective || !locField.descriptor.hasDefault())) continue;
            AIcNode locValue = fieldValue(locField, aEffective);
            if (locValue.kind == AInNodeKind.NULL && !locDefinition.allowsNull()) {
                aIssues.add(new AIcdSmartDataValidationIssue(locPath, "NULL_FORBIDDEN", "Null value is not permitted"));
            } else if (locValue.kind != AInNodeKind.NULL && !locField.descriptor.getter().getReturnType().isInstance(locValue.original)) {
                aIssues.add(new AIcdSmartDataValidationIssue(locPath, "WRONG_TYPE", "Property value is not assignable to its getter type"));
            }
        }
    }

    private void deep(AIcNode aNode, String aPath, boolean aEffective,
            List<AIcdSmartDataValidationIssue> aIssues, Set<AIcNode> aVisited) {
        if (aNode == null || !aVisited.add(aNode)) return;
        if (aNode.kind == AInNodeKind.SMART) {
            validateOwn(aNode, aPath, aEffective, aIssues);
            for (AIcField locField : aNode.fields) {
                if (!locField.present && (!aEffective || !locField.descriptor.hasDefault())) continue;
                String locPath = aPath.isEmpty() ? locField.name() : aPath + "." + locField.name();
                deep(fieldValue(locField, aEffective), locPath, aEffective, aIssues, aVisited);
            }
        } else if (aNode.kind == AInNodeKind.SEQUENCE) {
            int locIndex = 0;
            for (AIcNode locChild : aNode.elements)
                deep(locChild, aPath + "[" + locIndex++ + "]", aEffective, aIssues, aVisited);
        } else if (aNode.kind == AInNodeKind.MAPPING) {
            for (var locEntry : aNode.entries.entrySet())
                deep(locEntry.getValue(), aPath + "[" + String.valueOf(locEntry.getKey().scalar) + "]",
                        aEffective, aIssues, aVisited);
        }
    }
}
