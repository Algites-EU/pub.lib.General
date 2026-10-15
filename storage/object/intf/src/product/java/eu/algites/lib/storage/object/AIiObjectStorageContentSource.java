package eu.algites.lib.storage.object;

import java.io.IOException;
import java.io.InputStream;

/**
 * Repeatably readable binary input for an object transfer.
 * This is intentionally stream-based: multi-gigabyte objects must not require
 * materializing their bytes in memory. The caller retains responsibility for
 * keeping the source bytes identical across retries.
 */
public interface AIiObjectStorageContentSource {

    /** Exact expected number of bytes, never negative. */
    long getContentLength();

    /** Optional normalized media type, or null. */
    String getContentType();

    /** Optional expected hexadecimal SHA-256 digest, or null. */
    String getSha256();

    /**
     * Opens a fresh stream at offset zero on every invocation. The receiving
     * backend closes each stream it opens, including after cancellation/failure.
     * A retry must supply exactly the same bytes unless it is a new operation.
     */
    InputStream openStream() throws IOException;
}
