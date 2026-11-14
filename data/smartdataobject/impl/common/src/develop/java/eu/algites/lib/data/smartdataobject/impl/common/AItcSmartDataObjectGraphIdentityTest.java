package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.common.enums.AIiEnumItem;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Regression tests for strict persisted enum codes, aliases and graph identity.
 * Run as a plain Java main class; no external test framework is required.
 *
 * @author linhart1
 */
public final class AItcSmartDataObjectGraphIdentityTest {
    private AItcSmartDataObjectGraphIdentityTest() { }

    /** External codes must not be derived from Java enum constant names. */
    enum Color implements AIiEnumItem<String> {
        RED("r-stable"), BLUE("b-stable");
        private final String value;
        Color(String aCode) { value = aCode; }
        @Override public String code() { return value; }
    }
    /** Deliberately nonpersistable enum for rejection checks. */
    enum InternalColor { RED }

    interface Node extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="next") Node getNext();
        @AIaDataObjectField(dataFieldName="color") Color getColor();
    }

    /** Concrete SDO implementation with its effective contract. */
    @AIaSmartDataObjectImplementationBinding(readContracts={Node.class})
    public static class NodeSDO extends AIcSmartDataObject implements Node {
        /** Initializes a node. */
        public NodeSDO() { }
        /** {@inheritDoc} */
        @Override public Node getNext() { return (Node) get_EffectiveField("Next"); }
        /** {@inheritDoc} */
        @Override public Color getColor() { return (Color) get_EffectiveField("Color"); }
        /** @param aNext linked node */
        public void setNext(Node aNext) { set_RawField("Next", aNext); }
        /** @param aColor color */
        public void setColor(Color aColor) { set_RawField("Color", aColor); }
    }

    /** Executes the regression assertions.
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        var registry = AIcSmartDataObjectBindingRegistry.builder().register(NodeSDO.class).build();
        var node = new NodeSDO(); node.setColor(Color.RED);
        var tree = AIsSmartDataObjectDataTree.toData(node, registry);
        if (!"r-stable".equals(tree.get("color"))) throw new AssertionError("Enum code not used");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("color", "r-stable");
        var restored = AIsSmartDataObjectFactory.fromData(Node.class, data, registry);
        if (restored.getColor() != Color.RED) throw new AssertionError("Enum code not decoded");
        assertRejected(() -> AIsSmartDataObjectFactory.fromData(Node.class,
                Map.of("color", "RED"), registry), "Unknown enum name accepted");
        Map<String, Object> duplicate = new LinkedHashMap<>();
        duplicate.put("color", "r-stable"); duplicate.put("Color", "b-stable");
        assertRejected(() -> AIsSmartDataObjectFactory.fromData(Node.class, duplicate, registry),
                "Data/semantic aliases accepted for the same field");
        Map<String, Object> recursive = new LinkedHashMap<>();
        recursive.put("next", recursive);
        var linked = AIsSmartDataObjectFactory.fromData(Node.class, recursive, registry);
        if (linked != linked.getNext()) throw new AssertionError("Cyclic graph identity lost");
        assertRejected(() -> AIsSmartDataObjectDataTree.toData((NodeSDO) linked, registry),
                "Cycle silently serialized without a reference-aware profile");
        System.out.println("OK: stable enum codes, aliases, graph identity, cyclic encoding rejection");
    }

    private static void assertRejected(Runnable aAction, String aFailure) {
        try { aAction.run(); }
        catch (IllegalArgumentException aExpected) { return; }
        throw new AssertionError(aFailure);
    }
}
