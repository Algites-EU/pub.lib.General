package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.AIiFieldDefaultValueFactoryContext;
import java.lang.reflect.Type;
import java.util.Objects;

/** Immutable factory invocation data; returned argument arrays are defensive copies. */
public final class AIcFieldDefaultValueFactoryContext implements AIiFieldDefaultValueFactoryContext {
    private final Type expectedType;
    private final String[] defaultArguments;

    public AIcFieldDefaultValueFactoryContext(Type aExpectedType, String[] aDefaultArguments) {
        expectedType = Objects.requireNonNull(aExpectedType, "expectedType");
        defaultArguments = Objects.requireNonNull(aDefaultArguments, "defaultArguments").clone();
    }
    @Override public Type getExpectedType() { return expectedType; }
    @Override public String[] getDefaultArguments() { return defaultArguments.clone(); }
}
