package eu.algites.lib.data.dataobject;

/**
 * Explicitly separates the four Algites identifiers associated with a schema.
 *
 * @param schemaId stable unversioned logical identifier, not a URI or URL
 * @param schemaVersion positive version of that logical schema
 * @param schemaUrl optional absolute URL of a published representation
 */
public record AIcdSchemaIdentity(String schemaId, int schemaVersion, String schemaUrl) {
    /**
     * Creates a schema identity instance with the supplied configuration.
     * @param schemaId schema id supplied to the operation
     * @param schemaVersion schema version supplied to the operation
     * @param schemaUrl schema url supplied to the operation
     */
    public AIcdSchemaIdentity {
        if (schemaId == null || schemaId.isBlank())
            throw new IllegalArgumentException("SchemaId cannot be blank");
        if (schemaVersion <= 0)
            throw new IllegalArgumentException("SchemaVersion must be positive");
        if (schemaUrl == null) throw new IllegalArgumentException("SchemaUrl must not be null");
    }
    /** Unambiguous logical identity and version, intentionally distinct from SchemaUrl. */
    public String versionedSchemaId() { return schemaId + ":" + schemaVersion; }
}
