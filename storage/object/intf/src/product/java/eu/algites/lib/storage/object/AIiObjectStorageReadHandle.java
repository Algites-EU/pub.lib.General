package eu.algites.lib.storage.object;

import java.io.IOException;
import java.io.InputStream;

/**
 * Closeable read of one consistent object revision.
 * Metadata and bytes MUST correspond to the same observed revision, even when
 * another process concurrently replaces the key. Implementations that cannot
 * provide this consistency must not return a handle pretending they can.
 */
public interface AIiObjectStorageReadHandle extends AutoCloseable {

    /** Immutable metadata paired with this byte stream. */
    AIiObjectStorageObjectMetadata getMetadata();

    /** Single streaming view; closing the handle also closes this stream. */
    InputStream getInputStream();

    /** Releases all resources; safe to call more than once. */
    @Override
    void close() throws IOException;
}
