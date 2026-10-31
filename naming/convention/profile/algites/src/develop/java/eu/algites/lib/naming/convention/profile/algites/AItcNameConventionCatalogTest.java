package eu.algites.lib.naming.convention.profile.algites;

import eu.algites.lib.naming.convention.impl.standard.AIcNameConventionCatalog;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Native Java checks for YAML composition resolution, parsing and ownership selection. */
public final class AItcNameConventionCatalogTest {

    /** All Java profiles classify their generated names uniquely, including data-marker prefixes. */
    @Test
    public void AIcAllJavaProfilesAndAmbiguities() throws Exception {
        AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.loadResource("algites-java-name-conventions.yml");
        Assert.assertEquals(locCatalog.names().size(), 59);
        for (String locName : locCatalog.names()) {
            Map<String, String> locVars = Map.of("identifier", "Customer", "version", "2");
            String locFilename = locCatalog.format(locName, "file_name_stem", locVars, null)
                    + locCatalog.format(locName, "file_name_suffix", locVars, null);
            Assert.assertEquals(locCatalog.recognizeFilename(locFilename, null).conventionName(), locName);
        }
        Assert.assertEquals(locCatalog.recognizeFilename("AIigCustomer_1.java", null).conventionName(),
                "algites-java-gen-intf-versioned_1");
        Assert.assertEquals(locCatalog.recognizeFilename("AIigdCustomer_1.java", null).conventionName(),
                "algites-java-gen-data-intf-versioned_1");
        Assert.assertEquals(locCatalog.recognizeFilename("AIig_xePayment_1.java", null).conventionName(),
                "algites-java-gen-xml-element-intf-versioned_1");
        Assert.assertEquals(locCatalog.recognizeFilename("AIigXePayment_1.java", null).conventionName(),
                "algites-java-gen-intf-versioned_1");
    }

    /** All Python convention names are interpreted natively by the JVM, without Python execution. */
    @Test
    public void AIcPythonCatalogOnJava() throws Exception {
        AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.loadResource("algites-python-name-conventions.yml");
        Assert.assertEquals(locCatalog.names().size(), 59);
        Assert.assertEquals(locCatalog.recognizeFilename("aiig_customer_1.py", null).conventionName(),
                "algites-python-gen-intf-versioned_1");
        Assert.assertEquals(locCatalog.recognizeFilename("aiig__xe_payment_1.py", null).conventionName(),
                "algites-python-gen-xml-element-intf-versioned_1");
        Assert.assertEquals(locCatalog.recognizeFilename("aiig_xe_payment_1.py", null).conventionName(),
                "algites-python-gen-intf-versioned_1");
    }

    /** Cleanup removes only matching generated output files across package subdirectories. */
    @Test
    public void AIcCleanOnlyOwnRole() throws Exception {
        AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.loadResource("algites-java-name-conventions.yml");
        Path locRoot = Files.createTempDirectory("algites-naming-catalog-test");
        try {
            Path locNested = Files.createDirectories(locRoot.resolve("eu/algites/test"));
            for (String locName : new String[]{"AIigCustomer_1.java", "AIigdCustomer_1.java", "AIcgdCustomer_1.java"})
                Files.writeString(locNested.resolve(locName), "test");
            var locDeleted = locCatalog.clean(locRoot, Set.of("algites-java-gen-intf-versioned_1"));
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
        AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.loadResource("algites-java-name-conventions.yml");
        Map<String, String> locValues = Map.of("identifier", "customer", "version", "1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-intf-versioned_1", "type_name",
                locValues, "lower_kebab_case_1"), "AIigCustomer_1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-data-class-versioned_1", "type_name",
                locValues, "lower_kebab_case_1"), "AIcgdCustomer_1");
        Assert.assertEquals(locCatalog.format("algites-java-gen-enum-versioned_1", "type_name",
                locValues, "lower_kebab_case_1"), "AIngCustomer_1");
    }

    /** Complete filenames have exactly one suffix and all profiles share parameter/local rules. */
    @Test
    public void AIcSourceElementConventionsAndCompleteFilenames() throws Exception {
        for (String locLanguage : new String[]{"java", "python"}) {
            AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.loadResource("algites-" + locLanguage + "-name-conventions.yml");
            boolean locJava = locLanguage.equals("java");
            String locIdentifier = locJava ? "CustomerAddress" : "customer_address";
            String locParameter = locJava ? "aCustomerAddress" : "a_customer_address";
            String locLocal = locJava ? "locCustomerAddress" : "loc_customer_address";
            String locSourceConvention = locJava ? "upper_camel_case_1" : "lower_snake_case_1";
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
        Assert.assertEquals(AIcNameConventionCatalog.loadResource("algites-java-name-conventions.yml").names().size(), 59);
        Assert.assertEquals(AIcNameConventionCatalog.loadResource("algites-python-name-conventions.yml").names().size(), 59);
    }

    /** User-defined captures are extracted and reordered entirely through the example YAML catalogs. */
    @Test
    public void AIcCustomVariableCompositionExample() throws Exception {
        AIcNameConventionCatalog locSource = AIcNameConventionCatalog.loadResource("example-input.yml");
        AIcNameConventionCatalog locTarget = AIcNameConventionCatalog.loadResource("example-output.yml");
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
                "example-input_1", locTarget, "example-output_1", "file_name_stem");
        String locOutput = locOutputStem + locTarget.format("example-output_1", "file_name_suffix",
                locMatch.variables(), null);
        Assert.assertEquals(locOutput, "AIigCustomerAddress__beta__7-2__eu_west.java");
        Assert.assertEquals(locTarget.recognizeFilename(locOutput, null).variables().get("region"), "eu_west");
        Assert.assertNull(locTarget.recognizeFilename(locOutput + ".java", null));
    }

    /**
     * Confirms that the Java naming catalog reads generated-method metadata from
     * its distinct, enum-keyed mapping and continues to expose all six normalized
     * metadata roles through the shared naming API.
     *
     * @throws Exception if the temporary catalog cannot be created or parsed
     */
    @Test
    public void AIcGeneratedMethodSchemaMetadataNamespace() throws Exception {
        Path locCatalogFile = Files.createTempFile("algites-schema-metadata-naming", ".yml");
        try {
            Files.writeString(locCatalogFile, """
                    GeneratedDataObjectMethodNameConventions:
                      NameConventions:
                        example-methods_1:
                          StructureNaming:
                            standard_getter: getter
                            standard_setter: setter
                          SchemaMetadataNaming:
                            schema_source_schema_url: $id
                            schema_source_schema_identifier: x-algites-schema-id
                            data_source_schema_url: $schema
                            data_source_schema_identifier: ''
                    """);
            AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.load(locCatalogFile);
            Assert.assertEquals(locCatalog.schemaMetadataField("example-methods_1", "schema_source", "url"), "$id");
            Assert.assertEquals(locCatalog.schemaMetadataField("example-methods_1", "data_source", "url"), "$schema");
            Assert.assertEquals(locCatalog.schemaMetadataField("example-methods_1", "data_source", "version"), "");
            Files.writeString(locCatalogFile, """
                    GeneratedDataObjectMethodNameConventions:
                      NameConventions:
                        example-methods_1:
                          StructureNaming:
                            standard_getter: getter
                            standard_setter: setter
                          SchemaMetadataNaming:
                            schema_source_schema_url: $id
                            data_source_schema_url: $schema
                            unsupported_metadata_role: bad
                    """);
            Assert.assertEquals(AIcNameConventionCatalog.load(locCatalogFile)
                    .profileValue("example-methods_1", "SchemaMetadataNaming", "unsupported_metadata_role"), "bad");
        } finally {
            Files.deleteIfExists(locCatalogFile);
        }
    }



    /**
     * Rejects incompatible inline segment alternatives during catalog load, before
     * a selected profile attempts to generate or parse a name.
     *
     * @throws Exception when a temporary catalog cannot be written or removed
     */
    @Test
    public void AIcSegmentKindControlsPermittedFields() throws Exception {
        for (String locBody : new String[] {
                "Kind: identifier\n        Convention: upper_camel_case_1\n        Value: Hello",
                "Kind: version\n        Convention: positive_integer_1\n        Name: value",
                "Kind: variable\n        Name: region",
                "Kind: constant_string\n        Value: X\n        CustomConventionRegexp: Y",
                "Ref: another\n        Kind: constant_string",
                "Kind: identifier\n        Convention: upper_camel_case_1\n        CustomConventionRegexp: '(?<identifier>[a-z]+)'"
        }) {
            Path locFile = Files.createTempFile("algites-invalid-segment", ".yml");
            try {
                Files.writeString(locFile, "NameCompositions:\n  segment:\n    Segments:\n      - " + locBody + "\n");
                try {
                    AIcNameConventionCatalog.load(locFile);
                    Assert.fail("Invalid naming segment was accepted: " + locBody);
                } catch (IllegalArgumentException locExpected) {
                    Assert.assertTrue(locExpected.getMessage().contains("segment"), locExpected.getMessage());
                }
            } finally {
                Files.deleteIfExists(locFile);
            }
        }
    }

    /**
     * An inline identifier/version regular expression must compile under Java's
     * regex engine and contain its required named capture group.
     *
     * @throws Exception when a temporary catalog cannot be written or removed
     */
    @Test
    public void AIcCustomConventionRegexpIsValidatedEagerly() throws Exception {
        for (String locCustom : new String[] {
                "(?<version>[0-9]+)",
                "(?<identifier>[a-z]+",
                "(?<identifier>[a-z]+)(?<identifier>[0-9]+)"
        }) {
            Path locFile = Files.createTempFile("algites-invalid-regexp", ".yml");
            try {
                Files.writeString(locFile, "NameCompositions:\n  segment:\n    Segments:\n"
                        + "      - Kind: identifier\n"
                        + "        CustomConventionRegexp: '" + locCustom + "'\n");
                try {
                    AIcNameConventionCatalog.load(locFile);
                    Assert.fail("Invalid custom regexp was accepted: " + locCustom);
                } catch (IllegalArgumentException locExpected) {
                    Assert.assertTrue(locExpected.getMessage().contains("segment"), locExpected.getMessage());
                }
            } finally {
                Files.deleteIfExists(locFile);
            }
        }
        Path locFile = Files.createTempFile("algites-valid-regexp", ".yml");
        try {
            Files.writeString(locFile, "NameCompositions:\n  segment:\n    Segments:\n"
                    + "      - Kind: identifier\n"
                    + "        CustomConventionRegexp: '(?<identifier>[a-z]+)'\n"
                    + "NameConventions:\n  profile:\n    StructureNaming:\n"
                    + "      type_name: segment\n");
            AIcNameConventionCatalog locCatalog = AIcNameConventionCatalog.load(locFile);
            Assert.assertEquals(locCatalog.parse("profile", "type_name", "customer")
                    .get("identifier"), "customer");
        } finally {
            Files.deleteIfExists(locFile);
        }
    }
}
