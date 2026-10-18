package eu.algites.lib.naming.convention.impl.algites;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Native Java checks for YAML composition resolution, parsing and ownership selection. */
public final class AItcNameConventionCatalog_1Test {

    /** All Java profiles classify their generated names uniquely, including data-marker prefixes. */
    @Test
    public void AIcAllJavaProfilesAndAmbiguities() throws Exception {
        AIcNameConventionCatalog_1 locCatalog = AIcNameConventionCatalog_1.loadResource("algites-java-name-conventions.yml");
        Assert.assertEquals(locCatalog.names().size(), 48);
        for (String locName : locCatalog.names()) {
            Map<String, String> locVars = Map.of("identifier", "Customer", "version", "2");
            String locFilename = locCatalog.format(locName, "file_name_stem", locVars, null)
                    + locCatalog.format(locName, "file_name_suffix", locVars, null);
            Assert.assertEquals(locCatalog.recognizeFilename(locFilename, null).conventionName(), locName);
        }
        Assert.assertEquals(locCatalog.recognizeFilename("AIigCustomer_1.java", null).conventionName(),
                "algites-java-gen-intf-versioned");
        Assert.assertEquals(locCatalog.recognizeFilename("AIigdCustomer_1.java", null).conventionName(),
                "algites-java-gen-data-intf-versioned");
        Assert.assertEquals(locCatalog.recognizeFilename("AIig_xePayment_1.java", null).conventionName(),
                "algites-java-gen-xml-element-intf-versioned");
        Assert.assertEquals(locCatalog.recognizeFilename("AIigXePayment_1.java", null).conventionName(),
                "algites-java-gen-intf-versioned");
    }

    /** All Python convention names are interpreted natively by the JVM, without Python execution. */
    @Test
    public void AIcPythonCatalogOnJava() throws Exception {
        AIcNameConventionCatalog_1 locCatalog = AIcNameConventionCatalog_1.loadResource("algites-python-name-conventions.yml");
        Assert.assertEquals(locCatalog.names().size(), 48);
        Assert.assertEquals(locCatalog.recognizeFilename("aiig_customer_1.py", null).conventionName(),
                "algites-python-gen-intf-versioned");
        Assert.assertEquals(locCatalog.recognizeFilename("aiig__xe_payment_1.py", null).conventionName(),
                "algites-python-gen-xml-element-intf-versioned");
        Assert.assertEquals(locCatalog.recognizeFilename("aiig_xe_payment_1.py", null).conventionName(),
                "algites-python-gen-intf-versioned");
    }

    /** Cleanup removes only matching generated output files across package subdirectories. */
    @Test
    public void AIcCleanOnlyOwnRole() throws Exception {
        AIcNameConventionCatalog_1 locCatalog = AIcNameConventionCatalog_1.loadResource("algites-java-name-conventions.yml");
        Path locRoot = Files.createTempDirectory("algites-naming-catalog-test");
        try {
            Path locNested = Files.createDirectories(locRoot.resolve("eu/algites/test"));
            for (String locName : new String[]{"AIigCustomer_1.java", "AIigdCustomer_1.java", "AIcgdCustomer_1.java"})
                Files.writeString(locNested.resolve(locName), "test");
            var locDeleted = locCatalog.clean(locRoot, Set.of("algites-java-gen-intf-versioned"));
            Assert.assertEquals(locDeleted.size(), 1);
            Assert.assertFalse(Files.exists(locNested.resolve("AIigCustomer_1.java")));
            Assert.assertTrue(Files.exists(locNested.resolve("AIigdCustomer_1.java")));
        } finally {
            try (var locPaths = Files.walk(locRoot)) {
                for (Path locPath : locPaths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(locPath);
            }
        }
    }

    /** The declared YAML patterns render all three generator roles natively. */
    @Test
    public void AIcNativeRenderingUsesYaml() throws Exception {
        AIcNameConventionCatalog_1 locCatalog = AIcNameConventionCatalog_1.loadResource("algites-java-name-conventions.yml");
        Map<String, String> locValues = Map.of("identifier", "customer", "version", "1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-intf-versioned", "main_container_name",
                locValues, "lower_kebab_case"), "AIigCustomer_1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-data-class-versioned", "main_container_name",
                locValues, "lower_kebab_case"), "AIcgdCustomer_1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-enum-versioned", "main_container_name",
                locValues, "lower_kebab_case"), "AIngCustomer_1");
    }

    /** Complete filenames have exactly one suffix and all profiles share parameter/local rules. */
    @Test
    public void AIcSourceElementConventionsAndCompleteFilenames() throws Exception {
        for (String locLanguage : new String[]{"java", "python"}) {
            AIcNameConventionCatalog_1 locCatalog = AIcNameConventionCatalog_1.loadResource("algites-" + locLanguage + "-name-conventions.yml");
            boolean locJava = locLanguage.equals("java");
            String locIdentifier = locJava ? "CustomerAddress" : "customer_address";
            String locParameter = locJava ? "aCustomerAddress" : "a_customer_address";
            String locLocal = locJava ? "locCustomerAddress" : "loc_customer_address";
            String locSourceConvention = locJava ? "upper_camel_case" : "lower_snake_case";
            String locExtension = locJava ? ".java" : ".py";
            for (String locRole : locCatalog.names()) {
                Map<String, String> locNames = Map.of("identifier", locIdentifier, "version", "12");
                Assert.assertEquals(locCatalog.format(locRole, "method_parameter_name", locNames,
                        locSourceConvention), locParameter);
                Assert.assertEquals(locCatalog.format(locRole, "local_variable_name", locNames,
                        locSourceConvention), locLocal);
                Assert.assertTrue(locCatalog.matches(locRole, "method_parameter_name", locParameter));
                Assert.assertTrue(locCatalog.matches(locRole, "local_variable_name", locLocal));
                Assert.assertFalse(locCatalog.matches(locRole, "method_parameter_name", locIdentifier));
                String locStem = locCatalog.format(locRole, "file_name_stem", locNames, locSourceConvention);
                String locSuffix = locCatalog.format(locRole, "file_name_suffix", locNames, locSourceConvention);
                Assert.assertEquals(locSuffix, locExtension);
                Assert.assertFalse(locStem.endsWith(locExtension));
                Assert.assertEquals(locCatalog.recognizeFilename(locStem + locSuffix, null).conventionName(),
                        locRole);
                Assert.assertNull(locCatalog.recognizeFilename(locStem + locSuffix + locSuffix, null));
            }
        }
    }

    /** Standard runtime catalogs are loadable from the compiled artifact classpath. */
    @Test
    public void AIcStandardCatalogResourceAvailable() throws Exception {
        Assert.assertEquals(AIcNameConventionCatalog_1.standard("java").names().size(), 32);
        Assert.assertEquals(AIcNameConventionCatalog_1.standard("python").names().size(), 32);
    }

    /** User-defined captures are extracted and reordered entirely through the example YAML catalogs. */
    @Test
    public void AIcCustomVariableCompositionExample() throws Exception {
        AIcNameConventionCatalog_1 locSource = AIcNameConventionCatalog_1.loadResource("example-input.yml");
        AIcNameConventionCatalog_1 locTarget = AIcNameConventionCatalog_1.loadResource("example-output.yml");
        String locInput = "customer_address--eu_west--beta--2.7.demo.schema";
        var locMatch = locSource.recognizeFilename(locInput, null);
        Assert.assertNotNull(locMatch);
        Assert.assertEquals(locMatch.variables().get("identifier"), "customer_address");
        Assert.assertEquals(locMatch.variables().get("region"), "eu_west");
        Assert.assertEquals(locMatch.variables().get("channel"), "beta");
        Assert.assertEquals(locMatch.variables().get("version"), "2.7");
        Assert.assertEquals(locMatch.variables().get("major"), "2");
        Assert.assertEquals(locMatch.variables().get("minor"), "7");
        String locOutputStem = locSource.convert(locInput.substring(0, locInput.length() - ".demo.schema".length()),
                "example-input", locTarget, "example-output", "file_name_stem");
        String locOutput = locOutputStem + locTarget.format("example-output", "file_name_suffix",
                locMatch.variables(), null);
        Assert.assertEquals(locOutput, "AIigCustomerAddress__beta__7-2__eu_west.java");
        Assert.assertEquals(locTarget.recognizeFilename(locOutput, null).variables().get("region"), "eu_west");
        Assert.assertNull(locTarget.recognizeFilename(locOutput + ".java", null));
    }

}
