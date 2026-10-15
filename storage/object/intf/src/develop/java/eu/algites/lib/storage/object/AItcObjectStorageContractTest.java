package eu.algites.lib.storage.object;

import java.io.IOException;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * API-shape regressions. Real filesystem/S3 adapters must separately pass the
 * behavioral conformance tests described in OBJECT-STORAGE.md.
 */
public class AItcObjectStorageContractTest {

    @Test
    public void readAndWritePermissionsAreSeparated() {
        Assert.assertFalse(AIiObjectStorageReader.class.isAssignableFrom(
                AIiObjectStorageCreateOnlyWriter.class));
        Assert.assertFalse(AIiObjectStorageOverwriter.class.isAssignableFrom(
                AIiObjectStorageCreateOnlyWriter.class));
        Assert.assertFalse(AIiObjectStorageConditionalDeleter.class.isAssignableFrom(
                AIiObjectStorageStronglyConsistentBackend.class));
    }

    @Test
    public void ledgerBackendDemandsStrongReaderAndAtomicWrites() {
        Assert.assertTrue(AIiObjectStorageReader.class.isAssignableFrom(
                AIiObjectStorageStronglyConsistentBackend.class));
        Assert.assertTrue(AIiObjectStorageCreateOnlyWriter.class.isAssignableFrom(
                AIiObjectStorageStronglyConsistentBackend.class));
        Assert.assertTrue(AIiObjectStorageConditionalUpdater.class.isAssignableFrom(
                AIiObjectStorageStronglyConsistentBackend.class));
    }

    @Test
    public void absenceIsNotIndistinguishableFromIOException() throws Exception {
        Assert.assertEquals(AIiObjectStorageReader.class.getMethod(
                "head", String.class, AIiObjectStorageOperationContext.class).getReturnType(),
                Optional.class);
        Assert.assertEquals(AIiObjectStorageReader.class.getMethod(
                "openRead", String.class, AIiObjectStorageOperationContext.class).getReturnType(),
                Optional.class);
        Assert.assertTrue(IOException.class.isAssignableFrom(
                AIxObjectStoragePreconditionFailedException.class));
    }

    @Test
    public void contentIsStreamingAndReadHandlesAreCloseable() throws Exception {
        Assert.assertEquals(AIiObjectStorageContentSource.class.getMethod(
                "openStream").getReturnType(), java.io.InputStream.class);
        Assert.assertTrue(AutoCloseable.class.isAssignableFrom(
                AIiObjectStorageReadHandle.class));
        Assert.assertNotNull(AIiObjectStorageReadHandle.class.getMethod("getMetadata"));
    }

    @Test
    public void casUsesOpaqueRevisionNotLastModifiedTime() throws Exception {
        Assert.assertNotNull(AIiObjectStorageObjectMetadata.class.getMethod("getRevisionToken"));
        Assert.assertNotNull(AIiObjectStorageConditionalUpdater.class.getMethod(
                "replaceIfRevisionMatches", String.class, String.class,
                AIiObjectStorageContentSource.class, AIiObjectStorageOperationContext.class));
    }

    @Test
    public void listingAndCleanupRemainOptional() {
        Assert.assertFalse(AIiObjectStorageLister.class.isAssignableFrom(AIiObjectStorageReader.class));
        Assert.assertFalse(AIiObjectStorageConditionalDeleter.class.isAssignableFrom(
                AIiObjectStorageCreateOnlyWriter.class));
    }
}
