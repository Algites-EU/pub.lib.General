package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.common.enums.AIiEnumItem;
import eu.algites.lib.data.dataobject.AIiFieldDefaultScalarValueFactory;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueFactoryContext;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/** Standard strict lexical conversion of scalar DataObject defaults. */
@AIaCategoryProviderFactory(providerId = "algites-standard", categories = {"default"})
public final class AIcFieldDefaultScalarValueFactory implements AIiFieldDefaultScalarValueFactory {
    @SuppressWarnings("unchecked")
    @Override public Class<Optional<?>> getImplementationInterfaceType() {
        // Java cannot express Optional<?>.class, but the registry validates the raw type.
        return (Class<Optional<?>>) (Class<?>) Optional.class;
    }

    @Override public Class<AIiFieldDefaultValueFactoryContext> getContextInterfaceType() {
        return AIiFieldDefaultValueFactoryContext.class;
    }

    @Override public Optional<?> createInstance(String aCategory, AIiFieldDefaultValueFactoryContext aContext) {
        if (!"default".equals(aCategory))
            throw new IllegalArgumentException("Unsupported scalar default category: " + aCategory);
        Objects.requireNonNull(aContext, "context");
        Type type = Objects.requireNonNull(aContext.getExpectedType(), "expectedType");
        String[] arguments = Objects.requireNonNull(aContext.getDefaultArguments(), "defaultArguments");
        if (arguments.length != 1 || arguments[0] == null)
            throw new IllegalArgumentException("Scalar default requires one non-null argument for " + type);
        String value = arguments[0];
        try {
            if (type == String.class) return Optional.of(value);
            if (type == Boolean.class || type == boolean.class) {
                if (!"true".equals(value) && !"false".equals(value))
                    throw new IllegalArgumentException("Invalid Boolean default: " + value);
                return Optional.of(Boolean.valueOf(value));
            }
            if (type == Byte.class || type == byte.class) return Optional.of(Byte.valueOf(value));
            if (type == Short.class || type == short.class) return Optional.of(Short.valueOf(value));
            if (type == Integer.class || type == int.class) return Optional.of(Integer.valueOf(value));
            if (type == Long.class || type == long.class) return Optional.of(Long.valueOf(value));
            if (type == Float.class || type == float.class) return Optional.of(Float.valueOf(value));
            if (type == Double.class || type == double.class) return Optional.of(Double.valueOf(value));
            if (type == Character.class || type == char.class) {
                if (value.length() != 1) throw new IllegalArgumentException("Expected one character: " + value);
                return Optional.of(value.charAt(0));
            }
            if (type == BigInteger.class) return Optional.of(new BigInteger(value));
            if (type == BigDecimal.class) return Optional.of(new BigDecimal(value));
            if (type instanceof Class<?> enumType && enumType.isEnum()) {
                for (Object constant : enumType.getEnumConstants()) {
                    if (constant instanceof AIiEnumItem<?> item && value.equals(String.valueOf(item.code())))
                        return Optional.of(constant);
                }
                throw new IllegalArgumentException("No matching enum code for default '" + value + "' in " + type);
            }
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Invalid scalar default '" + value + "' for " + type, error);
        }
        throw new IllegalArgumentException("Unsupported scalar default type: " + type);
    }
}
