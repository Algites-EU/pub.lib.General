/**
 * Renderer-neutral documentation element model.
 * <p>
 * The package contains interfaces and default implementations for a small
 * documentation object tree. The model is intentionally independent from MPS,
 * HTML, Markdown, and any other source or target technology.
 * <p>
 * Every element type has a read-only {@code AIiDocumentation...} interface and
 * a corresponding {@code AIiDocumentation...WriteAccess} capability interface.
 * Construction, aggregation, and model-processing code can use write-access
 * interfaces, while validators, renderers, and public consumers can depend only
 * on read-only interfaces. Default {@code AIcDocumentation...} implementations
 * implement the corresponding write-access contracts.
 * <p>
 * The model distinguishes block elements, which form the document structure,
 * from inline elements, which form paragraph-like textual content. Semantic
 * references are represented separately from plain links so renderers can
 * resolve references to documented source objects in an output-specific way.
 */
package eu.algites.lib.common.documentation.model;
