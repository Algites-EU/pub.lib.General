package eu.algites.lib.data.smartdataobject.impl.format.yaml;

import eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.impl.common.*;
import eu.algites.lib.text.format.AInTextFormatType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Independently packaged YAML 1.2 SDO provider. Its built-in codec handles a
 * safe mapping/sequence/scalar subset without executing YAML tags.
 *
 * @author linhart1
 */
public final class AIcYamlFormatBindingProvider implements AIiSmartDataObjectFormatBindingProvider {
    /** Stable provider identifier. */
    public static final String PROVIDER_ID = "algites-yaml";
    /** {@inheritDoc} */
    @Override public String getFormatId() { return AInTextFormatType.YAML.code(); }
    /** {@inheritDoc} */
    @Override public String getProviderId() { return PROVIDER_ID; }
    /** {@inheritDoc} */
    @Override public Optional<? extends AIiSmartDataObjectFormatBindingMetadata> resolveMetadata(
            AIiSmartDataObjectMetadata aMetadata) {
        AIaDataObjectYamlSchema schema = null;
        for (Class<?> type : AIsSmartDataObjectFormatMetadataUtils.hierarchy(aMetadata.getImplementationClass())) {
            AIaDataObjectYamlSchema next = type.getDeclaredAnnotation(AIaDataObjectYamlSchema.class);
            if (next != null) schema = next;
        }
        AIaDataObjectYamlSchema result = schema;
        return schema == null ? Optional.empty() : Optional.of(new YamlMetadata(result));
    }
    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectMarshaller createMarshaller() {
        return (object, registry) -> AIsYamlSyntax.write(AIsSmartDataObjectDataTree.toData(object, registry));
    }
    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectUnmarshaller createUnmarshaller() {
        return (text, contract, registry) -> {
            Object decoded = AIsYamlSyntax.parse(text);
            if (!(decoded instanceof Map<?, ?> map)) throw new IllegalArgumentException("YAML SDO root must be a mapping");
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return (AIiSmartDataObject) AIsSmartDataObjectFactory.fromData(contract, result, registry);
        };
    }
    /** YAML binding metadata derived from schema annotations shared by JSONDEFS/YAMLDEFS. */
    public record YamlMetadata(AIaDataObjectYamlSchema schema)
            implements AIiSmartDataObjectFormatBindingMetadata {
        /** {@inheritDoc} */
        @Override public String getFormatId() { return AInTextFormatType.YAML.code(); }
        /** {@inheritDoc} */
        @Override public String getProviderId() { return PROVIDER_ID; }
    }
}
