package eu.algites.lib.naming.convention.impl.standard;


import eu.algites.lib.naming.convention.AIiNameConventionCatalog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves YAML naming fragments, matches complete file names and converts names without generator-specific rules. */
public final class AIcNameConventionCatalog implements AIiNameConventionCatalog {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Pattern CAPTURE = Pattern.compile("\\(\\?<([A-Za-z][A-Za-z0-9_]*)>");
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
    private final JsonNode document;
    private final Map<String, Pattern> filePatterns = new LinkedHashMap<>();
    private final Map<String, JsonNode> generatedMethodProfiles = new LinkedHashMap<>();
    private final Map<String, JsonNode> profiles = new LinkedHashMap<>();
    private final Map<String, JsonNode> identifierConventions = new LinkedHashMap<>();
    private final Map<String, JsonNode> versionConventions = new LinkedHashMap<>();
    private final Map<String, String> roleBindings;
    private final String generatedMethodProfile;

    /**
 * One recognized filename and its extracted named capture values.
 * @param conventionName name of the matching naming convention
 * @param variables values extracted from named captures in the matched name
 */
    public record AIcdNameMatch(String conventionName, Map<String, String> variables) { }

    /** Reads the catalog and resolves its optional standard-conventions sibling document. */
    public static AIcNameConventionCatalog load(Path aPath) throws IOException {
        Path locPath = aPath.toAbsolutePath().normalize();
        JsonNode locDocument = MAPPER.readTree(locPath.toFile());
        JsonNode locStandard = MAPPER.createObjectNode();
        if (locDocument.has("StandardConventionsFile")) {
            String locName = locDocument.path("StandardConventionsFile").asText();
            if (!locName.matches("[A-Za-z0-9][A-Za-z0-9_.-]*\\.yml"))
                throw new IOException("Invalid standard naming catalog resource: " + locName);
            Path locSibling = locPath.resolveSibling(locName);
            if (Files.isRegularFile(locSibling)) locStandard = MAPPER.readTree(locSibling.toFile());
            else {
                String locShared = "eu/algites/lib/naming/convention/" + locName;
                try (var locStream = AIcNameConventionCatalog.class.getClassLoader().getResourceAsStream(locShared)) {
                    if (locStream == null) throw new IOException("Missing shared standard naming resource: " + locShared);
                    locStandard = MAPPER.readTree(locStream);
                }
            }
        }
        return new AIcNameConventionCatalog(locDocument, locStandard);
    }

    /**
     * Loads a YAML naming catalog from the artifact classpath, including its standard-conventions sibling.
     * This also works when the artifact is consumed from a JAR rather than a source checkout.
     */
    public static AIcNameConventionCatalog loadResource(String aFilename) throws IOException {
        if (aFilename == null || !aFilename.matches("[A-Za-z0-9][A-Za-z0-9_.-]*\\.yml"))
            throw new IllegalArgumentException("Expected a YAML catalog filename: " + aFilename);
        String locDirectory = "eu/algites/lib/naming/convention/sourcecode/";
        ClassLoader locLoader = AIcNameConventionCatalog.class.getClassLoader();
        try (var locCatalog = locLoader.getResourceAsStream(locDirectory + aFilename)) {
            if (locCatalog == null) throw new IOException("Missing packaged YAML naming catalog: " + locDirectory + aFilename);
            JsonNode locDocument = MAPPER.readTree(locCatalog);
            JsonNode locStandard = MAPPER.createObjectNode();
            if (locDocument.has("StandardConventionsFile")) {
                String locSibling = locDocument.path("StandardConventionsFile").asText();
                if (!locSibling.matches("[A-Za-z0-9][A-Za-z0-9_.-]*\\.yml"))
                    throw new IOException("Invalid standard naming catalog resource: " + locSibling);
                try (var locStream = locLoader.getResourceAsStream("eu/algites/lib/naming/convention/" + locSibling)) {
                    if (locStream == null) throw new IOException("Missing packaged standard naming catalog: " + locSibling);
                    locStandard = MAPPER.readTree(locStream);
                }
            }
            return new AIcNameConventionCatalog(locDocument, locStandard);
        }
    }

    /**
     * Combines external and classpath-packaged naming catalogs into a single lookup.
     *
     * <p>Repeated identical definitions are intentionally deduplicated; conflicting
     * definitions cannot override each other based on the order of source files.
     * The {@code resource:} prefix selects a YAML resource packaged inside the
     * naming-conventions artifact, independently of the current working directory.
     *
     * @param aSources catalog locations, in lookup order; at least one is required
     * @return one resolved naming catalog covering all supplied definitions
     * @throws IOException if a source cannot be read or is not valid YAML
     * @throws IllegalArgumentException if identifiers have conflicting definitions
     */
    public static AIcNameConventionCatalog loadAll(List<String> aSources) throws IOException {
        if (aSources == null || aSources.isEmpty())
            throw new IllegalArgumentException("At least one naming catalog is required");
        var locMerged = MAPPER.createObjectNode();
        for (String locSection : List.of("IdentifierConventions", "VersionConventions", "NameFragments",
                "NameCompositions", "NameConventions")) locMerged.putObject(locSection);
        for (String locSource : aSources) {
            AIcNameConventionCatalog locCatalog = locSource.startsWith("resource:")
                    ? loadResource(locSource.substring("resource:".length()))
                    : load(Path.of(locSource));
            for (String locSection : List.of("IdentifierConventions", "VersionConventions", "NameFragments",
                    "NameCompositions", "NameConventions")) {
                JsonNode locEntries = locCatalog.document.path(locSection);
                var locTarget = (com.fasterxml.jackson.databind.node.ObjectNode) locMerged.path(locSection);
                if (locSection.equals("IdentifierConventions") || locSection.equals("VersionConventions")) {
                    /* A source catalog already imports its standard identifier definitions. */
                    locEntries = MAPPER.createObjectNode();
                }
                final JsonNode locDefinitions = locEntries;
                locDefinitions.fields().forEachRemaining(locEntry -> {
                    JsonNode locOld = locTarget.get(locEntry.getKey());
                    if (locOld != null && !locOld.equals(locEntry.getValue()))
                        throw new IllegalArgumentException("Conflicting naming definition "
                                + locSection + "/" + locEntry.getKey() + " in " + locSource);
                    locTarget.set(locEntry.getKey(), locEntry.getValue().deepCopy());
                });
            }
            /* Merge every named profile family structurally, not by its domain-specific name. */
            locCatalog.document.fields().forEachRemaining(locSectionEntry -> {
                String locSectionName = locSectionEntry.getKey();
                JsonNode locFamily = locSectionEntry.getValue();
                if (!locFamily.isObject() || !locFamily.path("NameConventions").isObject()) return;
                var locMergedFamily = locMerged.withObject(locSectionName);
                var locTarget = locMergedFamily.withObject("NameConventions");
                locFamily.path("NameConventions").fields().forEachRemaining(locEntry -> {
                    JsonNode locOld = locTarget.get(locEntry.getKey());
                    if (locOld != null && !locOld.equals(locEntry.getValue()))
                        throw new IllegalArgumentException("Conflicting naming profile "
                                + locSectionName + "/" + locEntry.getKey() + " in " + locSource);
                    locTarget.set(locEntry.getKey(), locEntry.getValue().deepCopy());
                });
            });
            for (String locSection : List.of("IdentifierConventions", "VersionConventions")) {
                var locTarget = (com.fasterxml.jackson.databind.node.ObjectNode) locMerged.path(locSection);
                Map<String, JsonNode> locEntries = locSection.equals("IdentifierConventions")
                        ? locCatalog.identifierConventions : locCatalog.versionConventions;
                locEntries.forEach((locName, locDefinition) -> {
                    JsonNode locOld = locTarget.get(locName);
                    if (locOld != null && !locOld.equals(locDefinition))
                        throw new IllegalArgumentException("Conflicting naming definition "
                                + locSection + "/" + locName + " in " + locSource);
                    locTarget.set(locName, locDefinition.deepCopy());
                });
            }
        }
        return new AIcNameConventionCatalog(locMerged, MAPPER.createObjectNode());
    }

    /**
     * Validates one YAML segment against the same exclusive content model as JSONDEFS.
     * The check is required even for XML-only catalogs, because XSD 1.0 cannot express
     * the dependency between a sibling Kind value and its content alternative.
     *
     * @param aSegment segment to validate
     * @param aLocation diagnostic path to the segment
     * @throws IllegalArgumentException if fields are inconsistent or a regex is invalid
     */
    private static void validateSegment(JsonNode aSegment, String aLocation) {
        if (!aSegment.isObject())
            throw new IllegalArgumentException(aLocation + ": naming segment must be an object");
        Set<String> locKeys = new java.util.HashSet<>();
        aSegment.fieldNames().forEachRemaining(locKeys::add);
        if (locKeys.contains("Ref")) {
            if (!locKeys.equals(Set.of("Ref")) || !aSegment.path("Ref").isTextual()
                    || aSegment.path("Ref").asText().isEmpty())
                throw new IllegalArgumentException(aLocation + ": Ref must be the only, nonempty field");
            return;
        }
        String locKind = aSegment.path("Kind").asText();
        Set<String> locExpected;
        switch (locKind) {
            case "constant_string" -> locExpected = Set.of("Kind", "Value");
            case "identifier", "version" -> {
                boolean locNamed = locKeys.contains("Convention");
                boolean locCustom = locKeys.contains("CustomConventionRegexp");
                if (locNamed == locCustom)
                    throw new IllegalArgumentException(aLocation + ": " + locKind
                            + " requires exactly one of Convention and CustomConventionRegexp");
                locExpected = Set.of("Kind", locNamed ? "Convention" : "CustomConventionRegexp");
            }
            case "variable" -> locExpected = Set.of("Kind", "Name", "Regexp");
            default -> throw new IllegalArgumentException(aLocation + ": unknown segment Kind " + locKind);
        }
        if (!locKeys.equals(locExpected))
            throw new IllegalArgumentException(aLocation + ": " + locKind
                    + " expects " + locExpected + ", got " + locKeys);
        for (String locKey : locExpected) {
            if (locKey.equals("Kind")) continue;
            if (!aSegment.path(locKey).isTextual() || (!locKey.equals("Value")
                    && aSegment.path(locKey).asText().isEmpty()))
                throw new IllegalArgumentException(aLocation + ": invalid or empty " + locKey);
        }
        if (locKind.equals("variable")) {
            String locName = aSegment.path("Name").asText();
            if (!locName.matches("[A-Za-z][A-Za-z0-9_]*"))
                throw new IllegalArgumentException(aLocation + ": invalid variable name " + locName);
            validateRegexp(aSegment.path("Regexp").asText(), null, aLocation);
        }
        if (locKeys.contains("CustomConventionRegexp"))
            validateRegexp(aSegment.path("CustomConventionRegexp").asText(), locKind, aLocation);
    }

    /**
     * Compiles a Java regular expression and verifies the named group used to
     * recover an identifier, version, or user-defined variable during parsing.
     *
     * @param aRegexp Java regular expression from a naming segment
     * @param aExpectedGroup required named capture
     * @param aLocation diagnostic segment location
     */
    private static void validateRegexp(String aRegexp, String aExpectedGroup, String aLocation) {
        try {
            Pattern.compile(aRegexp);
        } catch (java.util.regex.PatternSyntaxException locError) {
            throw new IllegalArgumentException(aLocation + ": invalid regular expression: "
                    + locError.getDescription(), locError);
        }
        if (aExpectedGroup == null) return;
        Matcher locCapture = CAPTURE.matcher(aRegexp);
        while (locCapture.find()) {
            if (locCapture.group(1).equals(aExpectedGroup)) return;
        }
        throw new IllegalArgumentException(aLocation + ": regular expression requires named capture "
                + aExpectedGroup);
    }

    /** Validates every defined segment, including fragments not selected by a profile. */
    private static void validateSegmentDefinitions(JsonNode aDocument) {
        for (String locSection : List.of("NameFragments", "NameCompositions")) {
            aDocument.path(locSection).fields().forEachRemaining(locEntry -> {
                JsonNode locSegments = locEntry.getValue().path("Segments");
                if (!locSegments.isArray())
                    throw new IllegalArgumentException(locSection + "/" + locEntry.getKey()
                            + " must contain a Segments array");
                for (int locIndex = 0; locIndex < locSegments.size(); locIndex++)
                    validateSegment(locSegments.get(locIndex), locSection + "/" + locEntry.getKey()
                            + "/Segments[" + locIndex + "]");
            });
        }
    }

    private AIcNameConventionCatalog(JsonNode aDocument, JsonNode aStandard) {
        roleBindings = Map.of();
        generatedMethodProfile = null;
        document = aDocument;
        validateSegmentDefinitions(aDocument);
        mergeConventions(identifierConventions, aStandard.path("IdentifierConventions"), aDocument.path("IdentifierConventions"));
        mergeConventions(versionConventions, aStandard.path("VersionConventions"), aDocument.path("VersionConventions"));
        registerProfiles(aDocument.path("NameConventions"), "NameConventions");
        aDocument.fields().forEachRemaining(locEntry -> {
            JsonNode locFamily = locEntry.getValue();
            if (!locFamily.isObject() || !locFamily.path("NameConventions").isObject()) return;
            registerProfiles(locFamily.path("NameConventions"), locEntry.getKey());
            if (locEntry.getKey().equals("GeneratedDataObjectMethodNameConventions"))
                locFamily.path("NameConventions").fields().forEachRemaining(locMethod ->
                        generatedMethodProfiles.put(locMethod.getKey(), locMethod.getValue()));
        });
    }

    /** Registers only profiles that explicitly declare file naming roles as filename matchers. */
    private void registerProfiles(JsonNode aProfiles, String aSection) {
        aProfiles.fields().forEachRemaining(locEntry -> {
            String locName = locEntry.getKey();
            JsonNode locDefinition = locEntry.getValue();
            JsonNode locOld = profiles.putIfAbsent(locName, locDefinition);
            if (locOld != null && !locOld.equals(locDefinition))
                throw new IllegalArgumentException("Conflicting naming profile: " + locName);
            if (locDefinition.path("StructureNaming").hasNonNull("file_name_stem") &&
                    locDefinition.path("StructureNaming").hasNonNull("file_name_suffix"))
                filePatterns.put(locName, filenamePattern(locName));
        });
    }

    private AIcNameConventionCatalog(AIcNameConventionCatalog aSource, Map<String, String> aRoles) {
        document = aSource.document;
        filePatterns.putAll(aSource.filePatterns);
        generatedMethodProfiles.putAll(aSource.generatedMethodProfiles);
        profiles.putAll(aSource.profiles);
        identifierConventions.putAll(aSource.identifierConventions);
        versionConventions.putAll(aSource.versionConventions);
        roleBindings = Map.copyOf(aRoles);
        generatedMethodProfile = aSource.generatedMethodProfile;
        for (String locName : roleBindings.values()) {
            if (!filePatterns.containsKey(locName))
                throw new IllegalArgumentException("Unknown naming convention selected for role: " + locName);
        }
    }

    private AIcNameConventionCatalog(AIcNameConventionCatalog aSource, String aMethodProfile) {
        document = aSource.document;
        filePatterns.putAll(aSource.filePatterns);
        generatedMethodProfiles.putAll(aSource.generatedMethodProfiles);
        profiles.putAll(aSource.profiles);
        identifierConventions.putAll(aSource.identifierConventions);
        versionConventions.putAll(aSource.versionConventions);
        roleBindings = aSource.roleBindings;
        if (!generatedMethodProfiles.containsKey(aMethodProfile))
            throw new IllegalArgumentException("Unknown generated method naming profile: " + aMethodProfile);
        generatedMethodProfile = aMethodProfile;
    }

    /**
     * Selects the same explicit DO/SDO method naming profile for all field-specific methods.
     * @param aMethodProfile ID under GeneratedDataObjectMethodNameConventions.NameConventions
     * @return immutable catalog view sharing every parsed composition and source profile
     * @throws IllegalArgumentException when the method profile is unavailable
     */
    @Deprecated
    public AIcNameConventionCatalog withGeneratedMethodProfile(String aMethodProfile) {
        return new AIcNameConventionCatalog(this, aMethodProfile);
    }

    /**
     * Returns the selected method-profile identifier, if any.
     * @return exact profile ID or null when source-structure naming alone is configured
     */
    @Override
    public String generatedMethodProfile() { return generatedMethodProfile; }

    /**
     * Return the configured literal generated-method name; no fallback is applied.
     *
     * @param role method naming role to resolve
     * @return the literal method name or an empty string when unconfigured
     * @throws IllegalArgumentException if a configured role is not a Java identifier
     */
    @Override
    public String generatedMethodLiteral(String role) {
        String name = generatedMethodReference(role);
        if (name == null) return "";
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*"))
            throw new IllegalArgumentException("Invalid generated method name for " + role + ": " + name);
        return name;
    }

    /**
     * Retrieves the composition ID assigned to an optional generated method role.
     * An absent or blank value explicitly disables that method and must not be
     * replaced by a generator-specific default. The selected profile must exist.
     *
     * @param aRole generated method role, such as {@code raw_getter}
     * @return the composition ID or {@code null} when the role is disabled or no
     *         generated-method profile is selected
     */
    @Override
    public String generatedMethodReference(String aRole) {
        if (generatedMethodProfile == null) return null;
        JsonNode locValue = generatedMethodProfiles.get(generatedMethodProfile)
                .path("StructureNaming").path(aRole);
        return locValue.isTextual() && !locValue.asText().isBlank() ? locValue.asText() : null;
    }

    /**
     * Binds semantic generation roles to exact identifiers without altering their definitions.
     * @param aRoles role-to-convention mapping; an absent role stays disabled
     * @return a catalog view carrying the requested role bindings
     */
    public AIcNameConventionCatalog withRoles(Map<String, String> aRoles) {
        return new AIcNameConventionCatalog(this, aRoles);
    }

    /**
     * Resolves a semantic codegen role using its configured naming identifier.
     * @param aRole semantic role, such as {@code xmldefs_type}
     * @return selected naming convention identifier
     * @throws IllegalArgumentException if this role is not configured
     */
    @Override
    public String selectedRole(String aRole) {
        String locSelected = roleBindings.get(aRole);
        if (locSelected == null)
            throw new IllegalArgumentException("No naming convention configured for " + aRole);
        return locSelected;
    }

    /**
     * Reports the lexical convention of the first identifier in a configured structure.
     * @param aRole exact naming profile identifier
     * @param aStructure structural name to inspect
     * @return identifier convention ID, or {@code null} for a custom regexp / no identifier
     */
    public String identifierConvention(String aRole, String aStructure) {
        for (JsonNode locSegment : structureSegments(aRole, aStructure)) {
            if (locSegment.path("Kind").asText().equals("identifier"))
                return locSegment.has("Convention") ? locSegment.get("Convention").asText() : null;
        }
        return null;
    }

    private static void mergeConventions(Map<String, JsonNode> aResult, JsonNode aBase, JsonNode aLocal) {
        aBase.fields().forEachRemaining(aItem -> aResult.put(aItem.getKey(), aItem.getValue()));
        aLocal.fields().forEachRemaining(aItem -> {
            if (aResult.putIfAbsent(aItem.getKey(), aItem.getValue()) != null)
                throw new IllegalArgumentException("Duplicate standard convention: " + aItem.getKey());
        });
    }

    /** Returns the configured profile identifiers in stable YAML order. */
    public Set<String> names() { return Collections.unmodifiableSet(filePatterns.keySet()); }

    /**
     * Names of generated DataObject method profiles, which do not participate in filename recognition.
     * @return configured method-profile identifiers in deterministic document order
     */
    @Deprecated
    public Set<String> generatedMethodNames() {
        return Collections.unmodifiableSet(generatedMethodProfiles.keySet());
    }

    /** Returns the structural kind declared by the profile. */
    public String containerStructureType(String aProfile) {
        return profile(aProfile).path("ContainerStructureType").asText();
    }

    /**
     * Returns the schema metadata field name configured for a source naming profile.
     * An empty result disables extraction or emission of that metadata field.
     *
     * @param aProfile source naming profile identifier
     * @param aSide either {@code schema_source} or {@code data_source}
     * @param aKind one of {@code url}, {@code identifier}, or {@code version}
     * @return configured field name, or an empty string when disabled
     * @throws IllegalArgumentException if a side or kind is unsupported
     */
    @Override
    @Deprecated
    public String schemaMetadataField(String aProfile, String aSide, String aKind) {
        if (!(aSide.equals("schema_source") || aSide.equals("data_source")) ||
                !(aKind.equals("url") || aKind.equals("identifier") || aKind.equals("version")))
            throw new IllegalArgumentException("Unsupported schema metadata field: " + aSide + "/" + aKind);
        JsonNode locProfile = profile(aProfile);
        String locSection = locProfile.has("SchemaMetadataNaming") ? "SchemaMetadataNaming" : "";
        return profileValue(aProfile, locSection, aSide + "_schema_" + aKind);
    }

    /**
     * Returns a value in a named profile section without interpreting its role.
     * A profile family decides which section and keys are valid via its own schema.
     *
     * @param aProfile profile identifier
     * @param aSection optional profile section, or empty string for the profile root
     * @param aKey exact key within the selected section
     * @return text value or an empty string when not declared
     */
    public String profileValue(String aProfile, String aSection, String aKey) {
        JsonNode locProfile = profile(aProfile);
        JsonNode locMap = aSection == null || aSection.isBlank()
                ? locProfile : locProfile.path(aSection);
        return locMap.path(aKey).asText("");
    }

    private JsonNode profile(String aName) {
        JsonNode locProfile = profiles.get(aName);
        if (locProfile == null) throw new IllegalArgumentException("Unknown naming profile: " + aName);
        return locProfile;
    }

    /** Returns all registered profile names, including non-file-naming profile families. */
    public Set<String> profileNames() { return Collections.unmodifiableSet(profiles.keySet()); }

    /** Resolves a declared structure role without assigning domain meaning to it. */
    public String compositionReference(String aProfile, String aRole) {
        return profileValue(aProfile, "StructureNaming", aRole);
    }

    private List<JsonNode> segments(String aComposition) {
        List<JsonNode> locResult = new ArrayList<>();
        expand(aComposition, new ArrayList<>(), locResult);
        return List.copyOf(locResult);
    }

    private void expand(String aName, List<String> aStack, List<JsonNode> aResult) {
        if (aStack.contains(aName)) throw new IllegalArgumentException("Cyclic NameFragments reference: " + aStack + " -> " + aName);
        JsonNode locDefinition = document.path("NameFragments").path(aName);
        if (locDefinition.isMissingNode()) locDefinition = document.path("NameCompositions").path(aName);
        if (locDefinition.isMissingNode()) throw new IllegalArgumentException("Unknown composition: " + aName);
        aStack.add(aName);
        for (JsonNode locSegment : locDefinition.path("Segments")) {
            if (locSegment.has("Ref")) expand(locSegment.get("Ref").asText(), aStack, aResult);
            else aResult.add(locSegment);
        }
        aStack.remove(aStack.size() - 1);
    }

    /** Temporary API bridge for pre-migration generators; remove after the tool upgrade. */
    @Deprecated
    private static String legacyGeneratedMethodRole(String aStructure) {
        return switch (aStructure) {
            case "getter_method_name" -> "standard_getter";
            case "setter_method_name" -> "standard_setter";
            default -> null;
        };
    }

    private List<JsonNode> structureSegments(String aProfile, String aStructure) {
        String locProfile = aProfile;
        String locStructure = aStructure;
        if (generatedMethodProfile != null) {
            String locLegacyRole = legacyGeneratedMethodRole(aStructure);
            if (locLegacyRole != null) {
                locProfile = generatedMethodProfile;
                locStructure = locLegacyRole;
            }
        }
        JsonNode locValue = profile(locProfile).path("StructureNaming").path(locStructure);
        if (!locValue.isTextual()) throw new IllegalArgumentException("Missing structure naming " + aStructure + " in " + aProfile);
        return segments(locValue.asText());
    }

    private String expression(JsonNode aSegment) {
        String locKind = aSegment.path("Kind").asText();
        if (locKind.equals("constant_string")) {
            if (!aSegment.has("Value")) throw new IllegalArgumentException("Missing constant Value");
            return Pattern.quote(aSegment.path("Value").asText());
        }
        if (locKind.equals("variable")) {
            String locName = aSegment.path("Name").asText();
            if (!locName.matches("[A-Za-z][A-Za-z0-9_]*")) throw new IllegalArgumentException("Invalid variable name: " + locName);
            return "(?<" + locName + ">" + aSegment.path("Regexp").asText() + ")";
        }
        if (!locKind.equals("identifier") && !locKind.equals("version"))
            throw new IllegalArgumentException("Unsupported naming segment: " + locKind);
        if (aSegment.has("Convention") == aSegment.has("CustomConventionRegexp"))
            throw new IllegalArgumentException("Provide exactly one of Convention and CustomConventionRegexp: " + locKind);
        if (aSegment.has("CustomConventionRegexp")) return aSegment.path("CustomConventionRegexp").asText();
        Map<String, JsonNode> locCatalog = locKind.equals("identifier") ? identifierConventions : versionConventions;
        JsonNode locEntry = locCatalog.get(aSegment.path("Convention").asText());
        if (locEntry == null) throw new IllegalArgumentException("Undefined standard convention: " + aSegment.path("Convention").asText());
        return locEntry.path("Regexp").asText();
    }

    private Pattern compile(List<JsonNode> aSegments) {
        StringBuilder locExpression = new StringBuilder("\\A(?:");
        for (JsonNode locSegment : aSegments) locExpression.append(expression(locSegment));
        locExpression.append(")\\z");
        return Pattern.compile(locExpression.toString());
    }

    /** Returns fully expanded immutable YAML segments for reuse by compatibility adapters. */
    public List<JsonNode> expandedSegments(String aProfile, String aStructure) {
        return List.copyOf(structureSegments(aProfile, aStructure));
    }

    /** Matches the entire name for a selected structure role. */
    public boolean matches(String aProfile, String aStructure, String aName) {
        return compile(structureSegments(aProfile, aStructure)).matcher(aName).matches();
    }

    /** Parses all named capture variables for a selected structure role. */
    public Map<String, String> parse(String aProfile, String aStructure, String aName) {
        Pattern locPattern = compile(structureSegments(aProfile, aStructure));
        Matcher locMatcher = locPattern.matcher(aName);
        if (!locMatcher.matches()) throw new IllegalArgumentException("Name does not match " + aProfile + "/" + aStructure + ": " + aName);
        return capture(locPattern, locMatcher);
    }

    private Pattern filenamePattern(String aProfile) {
        List<JsonNode> locSegments = new ArrayList<>(structureSegments(aProfile, "file_name_stem"));
        locSegments.addAll(structureSegments(aProfile, "file_name_suffix"));
        return compile(locSegments);
    }

    /** Recognizes a complete filename. Rejects overlapping profiles rather than guessing. */
    public AIcdNameMatch recognizeFilename(String aFilename, Set<String> aAllowed) {
        AIcdNameMatch locResult = null;
        for (var locEntry : filePatterns.entrySet()) {
            if (aAllowed != null && !aAllowed.contains(locEntry.getKey())) continue;
            Matcher locMatcher = locEntry.getValue().matcher(aFilename);
            if (!locMatcher.matches()) continue;
            if (locResult != null) throw new IllegalArgumentException("Ambiguous filename: " + aFilename
                    + " matches " + locResult.conventionName() + " and " + locEntry.getKey());
            locResult = new AIcdNameMatch(locEntry.getKey(), capture(locEntry.getValue(), locMatcher));
        }
        return locResult;
    }

    private static Map<String, String> capture(Pattern aPattern, Matcher aMatch) {
        Map<String, String> locResult = new LinkedHashMap<>();
        Matcher locGroups = CAPTURE.matcher(aPattern.pattern());
        while (locGroups.find()) {
            String locName = locGroups.group(1);
            String locValue = aMatch.group(locName);
            if (locValue != null) locResult.put(locName, locValue);
        }
        return Map.copyOf(locResult);
    }

    /** Deletes only files matching the requested output conventions, in the entire subtree. */
    public List<Path> clean(Path aRoot, Set<String> aAllowed) throws IOException {
        Path locRoot = aRoot.toAbsolutePath().normalize();
        if (!Files.isDirectory(locRoot)) return List.of();
        List<Path> locDeleted = new ArrayList<>();
        try (var locFiles = Files.walk(locRoot)) {
            for (Path locPath : locFiles.filter(Files::isRegularFile).sorted().toList()) {
                if (Files.isSymbolicLink(locPath)) continue;
                if (recognizeFilename(locPath.getFileName().toString(), aAllowed) == null) continue;
                Files.delete(locPath);
                locDeleted.add(locPath);
            }
        }
        return List.copyOf(locDeleted);
    }

    /** Formats values through a composition; validates each interpolated value against its regex. */
    public String format(String aProfile, String aStructure, Map<String, String> aValues, String aSourceIdentifierConvention) {
        StringBuilder locResult = new StringBuilder();
        for (JsonNode locSegment : structureSegments(aProfile, aStructure)) {
            String locKind = locSegment.path("Kind").asText();
            if (locKind.equals("constant_string")) {
                locResult.append(locSegment.path("Value").asText());
                continue;
            }
            String locName = switch (locKind) {
                case "identifier" -> "identifier";
                case "version" -> "version";
                case "variable" -> locSegment.path("Name").asText();
                default -> throw new IllegalArgumentException("Unknown segment: " + locKind);
            };
            String locValue = aValues.get(locName);
            if (locValue == null) throw new IllegalArgumentException("Missing variable: " + locName);
            if (locKind.equals("identifier") && aSourceIdentifierConvention != null && locSegment.has("Convention"))
                locValue = convertIdentifierByDeclaredStyle(locValue, aSourceIdentifierConvention, locSegment.get("Convention").asText());
            if (!Pattern.compile("\\A(?:" + expression(locSegment) + ")\\z").matcher(locValue).matches())
                throw new IllegalArgumentException("Invalid naming variable " + locName + "=" + locValue);
            locResult.append(locValue);
        }
        return locResult.toString();
    }

    /** Converts between declared lexical styles without interpreting naming profile IDs. */
    public String convertIdentifierByDeclaredStyle(String aValue, String aSource, String aTarget) {
        if (aSource.equals(aTarget)) return aValue;
        JsonNode locSource = identifierConventions.get(aSource);
        JsonNode locTarget = identifierConventions.get(aTarget);
        if (locSource == null || locTarget == null)
            throw new IllegalArgumentException("Unknown lexical convention: " + aSource + " -> " + aTarget);
        String locSourceStyle = locSource.path("ConversionStyle").asText();
        String locTargetStyle = locTarget.path("ConversionStyle").asText();
        if (locSourceStyle.isBlank() || locTargetStyle.isBlank())
            throw new IllegalArgumentException("Automatic conversion requires declared ConversionStyle: "
                    + aSource + " -> " + aTarget);
        return convertIdentifierStyle(aValue, locSourceStyle, locTargetStyle);
    }

    /** Performs conversion based on documented lexical style tokens, not convention identifiers. */
    private static String convertIdentifierStyle(String aValue, String aSourceStyle, String aTargetStyle) {
        String[] locWords;
        if (aSourceStyle.endsWith("_snake")) locWords = aValue.split("_");
        else if (aSourceStyle.endsWith("_kebab")) locWords = aValue.split("-");
        else if (aSourceStyle.endsWith("_camel")) locWords = CAMEL_BOUNDARY.split(aValue);
        else if (aSourceStyle.endsWith("_flat")) locWords = new String[]{aValue};
        else throw new IllegalArgumentException("Unsupported source lexical style: " + aSourceStyle);
        List<String> locLower = new ArrayList<>();
        for (String locWord : locWords) locLower.add(locWord.toLowerCase(Locale.ROOT));
        return switch (aTargetStyle) {
            case "lower_snake" -> String.join("_", locLower);
            case "upper_snake" -> String.join("_", locLower).toUpperCase(Locale.ROOT);
            case "lower_kebab" -> String.join("-", locLower);
            case "upper_kebab" -> String.join("-", locLower).toUpperCase(Locale.ROOT);
            case "lower_flat" -> String.join("", locLower);
            case "upper_flat" -> String.join("", locLower).toUpperCase(Locale.ROOT);
            case "lower_camel", "upper_camel" -> {
                StringBuilder locBuilder = new StringBuilder();
                for (int i = 0; i < locLower.size(); i++) {
                    String locWord = locLower.get(i);
                    if (locWord.isEmpty()) continue;
                    if (i == 0 && aTargetStyle.equals("lower_camel")) locBuilder.append(locWord);
                    else locBuilder.append(Character.toUpperCase(locWord.charAt(0))).append(locWord.substring(1));
                }
                yield locBuilder.toString();
            }
            default -> throw new IllegalArgumentException("Unsupported target lexical style: " + aTargetStyle);
        };
    }

    /** Legacy convention-ID converter retained for callers not yet migrated to declared lexical styles. */
    @Deprecated
    public String convert(String aValue, String aSourceProfile, AIcNameConventionCatalog aTarget,
            String aTargetProfile, String aStructure) {
        Map<String, String> locValues = parse(aSourceProfile, aStructure, aValue);
        String locSource = null;
        for (JsonNode locSegment : structureSegments(aSourceProfile, aStructure)) {
            if (locSegment.path("Kind").asText().equals("identifier")) {
                if (locSegment.has("Convention")) locSource = locSegment.get("Convention").asText();
                break;
            }
        }
        return aTarget.format(aTargetProfile, aStructure, locValues, locSource);
    }

    /** Normalizes only the lexical identifier: custom regex formats require already formatted values. */
    public static String convertIdentifier(String aValue, String aSource, String aTarget) {
        if (aSource.equals(aTarget)) return aValue;
        String locSourceStyle = legacyStyle(aSource);
        String locTargetStyle = legacyStyle(aTarget);
        return convertIdentifierStyle(aValue, locSourceStyle, locTargetStyle);
    }

    /** Compatibility mapping for older callers using the static utility. */
    @Deprecated
    private static String legacyStyle(String aIdentifier) {
        return switch (aIdentifier) {
            case "all_lower_1" -> "lower_flat";
            case "all_upper_1" -> "upper_flat";
            case "lower_snake_case_1" -> "lower_snake";
            case "upper_snake_case_1" -> "upper_snake";
            case "lower_kebab_case_1" -> "lower_kebab";
            case "upper_kebab_case_1" -> "upper_kebab";
            case "lower_camel_case_1" -> "lower_camel";
            case "upper_camel_case_1" -> "upper_camel";
            default -> throw new IllegalArgumentException("Unknown historic lexical style: " + aIdentifier);
        };
    }
}
