package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Build an identity-preserving, uniformly typed virtual graph from the shared SDO snapshot. */
final class AIcSmartDataVirtualMapper {
    /** Nodes are allocated before their children so recursive references retain identity. */
    private static final class AIcValue implements AIidVirtualFieldValue {
        private final Class<?> valueClass;
        private Object representation;
        private AIcValue(Class<?> aValueClass) { valueClass = aValueClass; }
        /** {@inheritDoc} */
        @Override public Object getValueRepresentation() { return representation; }
        /** {@inheritDoc} */
        @Override public Class<?> getValueClass() { return valueClass; }
    }

    private final AIcSmartDataGraphSnapshot snapshot;
    private final boolean effective;
    private final IdentityHashMap<AIcSmartDataGraphSnapshot.AIcNode, AIcValue> values = new IdentityHashMap<>();

    private AIcSmartDataVirtualMapper(AIcSmartDataGraphSnapshot aSnapshot, boolean aEffective) {
        snapshot = aSnapshot;
        effective = aEffective;
    }

    @SuppressWarnings("unchecked")
    static Map<String, AIidVirtualFieldValue> capture(AIcSmartDataObject aObject, boolean aEffective) {
        var locSnapshot = AIcSmartDataGraphSnapshot.capture(aObject);
        var locMapper = new AIcSmartDataVirtualMapper(locSnapshot, aEffective);
        return (Map<String, AIidVirtualFieldValue>) locMapper.convert(locSnapshot.root()).getValueRepresentation();
    }

    private AIidVirtualFieldValue convert(AIcSmartDataGraphSnapshot.AIcNode aNode) {
        AIcValue locKnown = values.get(aNode);
        if (locKnown != null) return locKnown;
        var locValue = new AIcValue(aNode.original == null ? Void.class : aNode.original.getClass());
        values.put(aNode, locValue);
        switch (aNode.kind) {
            case NULL -> locValue.representation = null;
            case SCALAR, OPAQUE -> locValue.representation = aNode.original;
            case SMART -> {
                Map<String, AIidVirtualFieldValue> locProperties = new LinkedHashMap<>();
                locValue.representation = locProperties;
                for (var locField : aNode.fields) {
                    if (!locField.present && (!effective || !locField.descriptor.hasDefault())) continue;
                    locProperties.put(locField.name(), convert(snapshot.fieldValue(locField, effective)));
                }
            }
            case SEQUENCE -> {
                List<AIidVirtualFieldValue> locElements = new ArrayList<>();
                locValue.representation = locElements;
                for (var locElement : aNode.elements) locElements.add(convert(locElement));
            }
            case MAPPING -> {
                Map<AIidVirtualFieldValue, AIidVirtualFieldValue> locEntries = new LinkedHashMap<>();
                locValue.representation = locEntries;
                for (var locEntry : aNode.entries.entrySet())
                    locEntries.put(convert(locEntry.getKey()), convert(locEntry.getValue()));
            }
        }
        return locValue;
    }
}
