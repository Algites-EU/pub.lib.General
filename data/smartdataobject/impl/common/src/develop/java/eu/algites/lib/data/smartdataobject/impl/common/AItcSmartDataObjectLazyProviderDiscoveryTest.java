package eu.algites.lib.data.smartdataobject.impl.common;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Regression: eagerly initialized SDO metadata must discover and instantiate
 * format providers only once per runtime classloader.
 *
 * @author linhart1
 */
public final class AItcSmartDataObjectLazyProviderDiscoveryTest {
    /** An SDO without format-specific metadata. */
    private static final class NeutralSDO extends AIcSmartDataObject { }
    /** A separate implementation for testing shared provider discovery. */
    private static final class SecondNeutralSDO extends AIcSmartDataObject { }

    /** A context loader which counts Java ServiceLoader resource discovery. */
    private static final class CountingLoader extends ClassLoader {
        private int scans;

        /**
         * Creates a counting child of the given classloader.
         * @param parent parent to delegate other classes and resources to
         */
        CountingLoader(ClassLoader parent) { super(parent); }

        /**
         * Detects discovery of SDO format providers.
         * @param name resource name sought by ServiceLoader
         * @return matching resources from the parent loader
         * @throws IOException when the underlying loader cannot read resources
         */
        @Override public Enumeration<URL> getResources(String name) throws IOException {
            if (name.equals("META-INF/services/" +
                    AIiSmartDataObjectFormatBindingProvider.class.getName())) scans++;
            return super.getResources(name);
        }
    }

    /**
     * Runs the executable regression without a test framework.
     * @param args unused arguments
     * @throws AssertionError if provider discovery repeats or is not eager
     */
    public static void main(String[] args) {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        CountingLoader counting = new CountingLoader(original);
        try {
            thread.setContextClassLoader(counting);
            var metadata = AIsSmartDataObjectMetadataRegistry.forType(NeutralSDO.class);
            int firstScanCount = counting.scans;
            if (firstScanCount == 0) throw new AssertionError("first metadata access did not discover providers");
            var providers = AIsSmartDataObjectFormatBindingProviderRegistry.getProviders();
            for (int i = 0; i < 100; i++) {
                if (!metadata.getFields().isEmpty()) throw new AssertionError("neutral metadata changed");
                if (AIsSmartDataObjectMetadataRegistry.forType(NeutralSDO.class) != metadata)
                    throw new AssertionError("metadata were not cached by class identity");
                metadata.getFormatBindings();
                if (AIsSmartDataObjectFormatBindingProviderRegistry.getProviders() != providers)
                    throw new AssertionError("provider instances were not cached");
            }
            AIsSmartDataObjectMetadataRegistry.forType(SecondNeutralSDO.class);
            if (counting.scans != firstScanCount)
                throw new AssertionError("repeated provider discovery: " + counting.scans);
            System.out.println("OK: eager Java metadata and once-only ServiceLoader discovery");
        } finally {
            thread.setContextClassLoader(original);
        }
    }
}
