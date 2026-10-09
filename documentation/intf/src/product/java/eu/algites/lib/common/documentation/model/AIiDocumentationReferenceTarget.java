package eu.algites.lib.common.documentation.model;

/**
 * Represents a renderer-neutral target of a semantic documentation reference.
 * <p>
 * A reference target describes a documented source object without depending on
 * a source technology API such as MPS. For MPS-generated documentation, the
 * target kind may identify concepts, properties, links, languages, solutions,
 * models, or other documented MPS artifacts. Renderers or link resolvers can
 * use the stable target identifier and qualified name to resolve output links.
 */
public interface AIiDocumentationReferenceTarget extends AIiDocumentationElement {

    /**
     * Returns the source-system identifier of the target.
     * <p>
     * Examples may include {@code mps}, {@code java}, or another generator
     * namespace. An empty string means that the source system is unspecified.
     *
     * @return source-system identifier, or an empty string if unspecified
     */
    String getSourceSystemId();

    /**
     * Returns the target kind.
     * <p>
     * The kind should be stable and renderer-neutral, for example
     * {@code mps.concept}, {@code mps.property}, {@code java.class}, or another
     * generator-defined kind.
     *
     * @return target kind
     */
    String getTargetKind();

    /**
     * Returns the stable target identifier.
     * <p>
     * The identifier should be stable across generation runs and may be an MPS
     * node identifier, Java binary name, generated documentation identifier, or
     * another source-specific stable key.
     *
     * @return stable target identifier
     */
    String getTargetId();

    /**
     * Returns the target qualified name.
     * <p>
     * An empty string means that no qualified name is available.
     *
     * @return qualified target name, or an empty string if unavailable
     */
    String getQualifiedName();

    /**
     * Returns the human-readable target display name.
     * <p>
     * An empty string means that no display name is available and renderers may
     * derive one from the qualified name or target identifier.
     *
     * @return display name, or an empty string if unavailable
     */
    String getDisplayName();
}
