package eu.algites.lib.naming.convention;

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
    private final Map<String, JsonNode> identifierConventions = new LinkedHashMap<>();
    private final Map<String, JsonNode> versionConventions = new LinkedHashMap<>();

    /** One recognized filename and its extracted named capture values. */
    public record AIcdNameMatch(String conventionName, Map<String, String> variables) { }

    /** Reads the catalog and resolves its optional standard-conventions sibling document. */
    public static AIcNameConventionCatalog load(Path aPath) throws IOException {
        Path locPath = aPath.toAbsolutePath().normalize();
        JsonNode locDocument = MAPPER.readTree(locPath.toFile());
        JsonNode locStandard = locDocument.has("StandardConventionsFile")
                ? MAPPER.readTree(locPath.resolveSibling(locDocument.get("StandardConventionsFile").asText()).toFile())
                : MAPPER.createObjectNode();
        return new AIcNameConventionCatalog(locDocument, locStandard);
    }

    /** Loads the standard Algites catalog from its packaged YAML resources. */
    public static AIcNameConventionCatalog standard(String aTechnology) throws IOException {
        if (!aTechnology.equals("java") && !aTechnology.equals("python"))
            throw new IllegalArgumentException("Unknown standard technology: " + aTechnology);
        String locDirectory = "eu/algites/lib/naming/convention/";
        ClassLoader locLoader = AIcNameConventionCatalog.class.getClassLoader();
        try (var locCatalog = locLoader.getResourceAsStream(locDirectory + "algites-" + aTechnology + "-name-conventions.yml");
             var locStandard = locLoader.getResourceAsStream(locDirectory + "standard-conventions.yml")) {
            if (locCatalog == null || locStandard == null) throw new IOException("Missing packaged Algites YAML naming catalog for " + aTechnology);
            return new AIcNameConventionCatalog(MAPPER.readTree(locCatalog), MAPPER.readTree(locStandard));
        }
    }

    private AIcNameConventionCatalog(JsonNode aDocument, JsonNode aStandard) {
        document = aDocument;
        mergeConventions(identifierConventions, aStandard.path("IdentifierConventions"), aDocument.path("IdentifierConventions"));
        mergeConventions(versionConventions, aStandard.path("VersionConventions"), aDocument.path("VersionConventions"));
        aDocument.path("NameConventions").fieldNames().forEachRemaining(aName -> filePatterns.put(aName, filenamePattern(aName)));
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

    /** Returns the structural kind declared by the profile. */
    public String containerStructureType(String aProfile) {
        return document.path("NameConventions").path(aProfile).path("ContainerStructureType").asText();
    }

    private JsonNode profile(String aName) {
        JsonNode locProfile = document.path("NameConventions").path(aName);
        if (locProfile.isMissingNode()) throw new IllegalArgumentException("Unknown naming profile: " + aName);
        return locProfile;
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

    private List<JsonNode> structureSegments(String aProfile, String aStructure) {
        JsonNode locValue = profile(aProfile).path("StructureNaming").path(aStructure);
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
                locValue = convertIdentifier(locValue, aSourceIdentifierConvention, locSegment.get("Convention").asText());
            if (!Pattern.compile("\\A(?:" + expression(locSegment) + ")\\z").matcher(locValue).matches())
                throw new IllegalArgumentException("Invalid naming variable " + locName + "=" + locValue);
            locResult.append(locValue);
        }
        return locResult.toString();
    }

    /** Converts the captured identifier according to the two named lexical conventions. */
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
        String[] locWords;
        if (aSource.endsWith("_snake_case")) locWords = aValue.split("_");
        else if (aSource.endsWith("_kebab_case")) locWords = aValue.split("-");
        else if (aSource.endsWith("_camel_case")) locWords = CAMEL_BOUNDARY.split(aValue);
        else if (aSource.equals("all_lower") || aSource.equals("all_upper")) locWords = new String[]{aValue};
        else throw new IllegalArgumentException("Custom regex cannot automatically format: " + aSource);
        List<String> locLower = new ArrayList<>();
        for (String locWord : locWords) locLower.add(locWord.toLowerCase(Locale.ROOT));
        return switch (aTarget) {
            case "lower_snake_case" -> String.join("_", locLower);
            case "upper_snake_case" -> String.join("_", locLower).toUpperCase(Locale.ROOT);
            case "lower_kebab_case" -> String.join("-", locLower);
            case "upper_kebab_case" -> String.join("-", locLower).toUpperCase(Locale.ROOT);
            case "all_lower" -> String.join("", locLower);
            case "all_upper" -> String.join("", locLower).toUpperCase(Locale.ROOT);
            case "lower_camel_case", "upper_camel_case" -> {
                StringBuilder locBuilder = new StringBuilder();
                for (int i = 0; i < locLower.size(); i++) {
                    String locWord = locLower.get(i);
                    if (i == 0 && aTarget.equals("lower_camel_case")) locBuilder.append(locWord);
                    else locBuilder.append(Character.toUpperCase(locWord.charAt(0))).append(locWord.substring(1));
                }
                yield locBuilder.toString();
            }
            default -> throw new IllegalArgumentException("Unsupported lexical convention: " + aTarget);
        };
    }
}
