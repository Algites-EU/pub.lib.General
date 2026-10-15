package eu.algites.lib.storage.object;

import java.time.Instant;

/**
 * Immutable snapshot of metadata for one observed stored object.
 * Metadata is descriptive, not an independently verified content integrity proof.
 */
public interface AIiObjectStorageObjectMetadata {

    /** Canonical namespace-relative key of the observed object. */
    String getKey();

    /** Exact binary object length, in bytes; never negative. */
    long getLength();

    /** Optional media type without provider-specific transport headers. */
    String getContentType();

    /**
     * Optional SHA-256 hexadecimal content checksum. May be provider metadata
     * supplied by the uploader; security-sensitive callers must verify bytes.
     * It must never be confused with S3 multipart ETag.
     */
    String getSha256();

    /**
     * Opaque backend concurrency token for this exact key/revision, or null if
     * no strong conditional update token can be supplied. Not a content hash.
     * The token must be usable without translation by the same backend's CAS API.
     */
    String getRevisionToken();

    /** Server-observed modification time if available; never a CAS identity. */
    Instant getLastModifiedAt();
}
