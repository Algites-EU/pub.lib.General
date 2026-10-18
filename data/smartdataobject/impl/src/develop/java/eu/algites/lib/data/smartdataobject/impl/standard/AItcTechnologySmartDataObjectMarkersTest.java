package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.jsondefs.AIiJsonDataObject;
import eu.algites.lib.data.dataobject.yamldefs.AIiYamlDataObject;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import eu.algites.lib.data.smartdataobject.impl.jsondefs.AIcJsonSmartDataObject;
import eu.algites.lib.data.smartdataobject.impl.yamldefs.AIcYamlSmartDataObject;
import eu.algites.lib.data.smartdataobject.xmldefs.AIiXmlSmartDataObject;
import eu.algites.lib.data.smartdataobject.jsondefs.AIiJsonSmartDataObject;
import eu.algites.lib.data.smartdataobject.yamldefs.AIiYamlSmartDataObject;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Regression checks for format-specific read-only and mutable contract markers.
 *
 * <p>Code generators rely on these interface relationships to represent source
 * definition families while retaining generic SmartDataObject interoperability.
 */
public final class AItcTechnologySmartDataObjectMarkersTest {

    /** Ensures each source marker retains the common data-object supertype. */
    @Test
    public void verifyTechnologyReadOnlyMarkers() {
        assertTrue(AIiDataObject.class.isAssignableFrom(AIiXmlDataObject.class));
        assertTrue(AIiDataObject.class.isAssignableFrom(AIiJsonDataObject.class));
        assertTrue(AIiDataObject.class.isAssignableFrom(AIiYamlDataObject.class));
    }

    /** Ensures every mutable marker inherits both the shared API and its format. */
    @Test
    public void verifyTechnologyMutableMarkers() {
        assertTrue(AIiSmartDataObject.class.isAssignableFrom(AIiXmlSmartDataObject.class));
        assertTrue(AIiXmlDataObject.class.isAssignableFrom(AIiXmlSmartDataObject.class));
        assertTrue(AIiSmartDataObject.class.isAssignableFrom(AIiJsonSmartDataObject.class));
        assertTrue(AIiJsonDataObject.class.isAssignableFrom(AIiJsonSmartDataObject.class));
        assertTrue(AIiSmartDataObject.class.isAssignableFrom(AIiYamlSmartDataObject.class));
        assertTrue(AIiYamlDataObject.class.isAssignableFrom(AIiYamlSmartDataObject.class));
    }

    /** Ensures format-specific contracts are not accidentally interchangeable. */
    @Test
    public void verifyTechnologyMarkersAreDistinct() {
        assertFalse(AIiJsonDataObject.class.isAssignableFrom(AIiXmlSmartDataObject.class));
        assertFalse(AIiXmlDataObject.class.isAssignableFrom(AIiYamlSmartDataObject.class));
        assertFalse(AIiYamlDataObject.class.isAssignableFrom(AIiJsonSmartDataObject.class));
    }

    /**
     * Verifies that the technology-specific implementation classes inherit the shared
     * mutable implementation and implement the matching technology contracts.
     *
     * <p>The generated classes will extend these bases instead of extending
     * {@link AIcSmartDataObject} directly.
     */
    @Test
    public void verifyTechnologyImplementationBases() {
        assertTrue(AIcSmartDataObject.class.isAssignableFrom(AIcXmlSmartDataObject.class));
        assertTrue(AIiXmlSmartDataObject.class.isAssignableFrom(AIcXmlSmartDataObject.class));
        assertTrue(AIcSmartDataObject.class.isAssignableFrom(AIcJsonSmartDataObject.class));
        assertTrue(AIiJsonSmartDataObject.class.isAssignableFrom(AIcJsonSmartDataObject.class));
        assertTrue(AIcSmartDataObject.class.isAssignableFrom(AIcYamlSmartDataObject.class));
        assertTrue(AIiYamlSmartDataObject.class.isAssignableFrom(AIcYamlSmartDataObject.class));
        assertFalse(AIiJsonSmartDataObject.class.isAssignableFrom(AIcXmlSmartDataObject.class));
    }

    /**
     * Confirms that generated concrete subclasses may use the empty and mapping
     * constructors supplied by technology-specific implementation ancestors.
     */
    @Test
    public void verifyTechnologyConstructorAccessibility() {
        assertTrue(new AIcgdXmlExample() instanceof AIiXmlSmartDataObject);
        assertTrue(new AIcgdJsonExample() instanceof AIiJsonSmartDataObject);
        assertTrue(new AIcgdYamlExample() instanceof AIiYamlSmartDataObject);
    }

    /** Sample of the schema-generated XML implementation hierarchy. */
    public static final class AIcgdXmlExample extends AIcXmlSmartDataObject { }

    /** Sample of the schema-generated JSON implementation hierarchy. */
    public static final class AIcgdJsonExample extends AIcJsonSmartDataObject { }

    /** Sample of the schema-generated YAML implementation hierarchy. */
    public static final class AIcgdYamlExample extends AIcYamlSmartDataObject { }

}
