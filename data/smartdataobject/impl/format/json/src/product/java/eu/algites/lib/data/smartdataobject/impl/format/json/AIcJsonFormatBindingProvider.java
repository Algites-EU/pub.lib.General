package eu.algites.lib.data.smartdataobject.impl.format.json;

import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema;
import eu.algites.lib.data.smartdataobject.impl.common.*;
import eu.algites.lib.text.format.AInTextFormatType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service-discovered JSON format binding for the new common SDO runtime.
 * The provider does not depend on the deprecated JSON SDO marker classes.
 *
 * @author linhart1
 */
public final class AIcJsonFormatBindingProvider implements AIiSmartDataObjectFormatBindingProvider {
    /** Stable built-in provider identifier. */
    public static final String PROVIDER_ID = "algites-json";

    /** {@inheritDoc} */
    @Override public String getFormatId() { return AInTextFormatType.JSON.code(); }
    /** {@inheritDoc} */
    @Override public String getProviderId() { return PROVIDER_ID; }

    /** {@inheritDoc} */
    @Override public Optional<? extends AIiSmartDataObjectFormatBindingMetadata> resolveMetadata(
            AIiSmartDataObjectMetadata aMetadata) {
        AIaDataObjectJsonSchema schema = null;
        for (Class<?> type : AIsSmartDataObjectFormatMetadataUtils.hierarchy(aMetadata.getImplementationClass())) {
            AIaDataObjectJsonSchema declared = type.getDeclaredAnnotation(AIaDataObjectJsonSchema.class);
            if (declared != null) schema = declared;
        }
        AIaDataObjectJsonSchema captured = schema;
        return schema == null ? Optional.empty() : Optional.of(new JsonMetadata(captured));
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectMarshaller createMarshaller() {
        return (object, registry) -> AIsJsonSyntax.write(AIsSmartDataObjectDataTree.toData(object, registry));
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectUnmarshaller createUnmarshaller() {
        return (text, contract, registry) -> {
            Object decoded = AIsJsonSyntax.parse(text);
            if (!(decoded instanceof Map<?, ?> map))
                throw new IllegalArgumentException("JSON root SDO must be an object");
            Map<String, Object> result = new LinkedHashMap<>();
            for (var entry : map.entrySet()) result.put((String) entry.getKey(), entry.getValue());
            return (AIiSmartDataObject) AIsSmartDataObjectFactory.fromData(contract, result, registry);
        };
    }

    /** Immutable JSON Schema identity contributed to a class's metadata. */
    public record JsonMetadata(AIaDataObjectJsonSchema schema)
            implements AIiSmartDataObjectFormatBindingMetadata {
        /** @return external JSON code */
        @Override public String getFormatId() { return AInTextFormatType.JSON.code(); }
        /** @return built-in JSON provider ID */
        @Override public String getProviderId() { return PROVIDER_ID; }
        /** @return schema identifier (possibly empty) */
        public String getSchemaId() { return schema.schemaId(); }
        /** @return schema version or -1 */
        public int getSchemaVersion() { return schema.schemaVersion(); }
        /** @return schema URL, possibly empty */
        public String getSchemaUrl() { return schema.schemaUrl(); }
    }
}
