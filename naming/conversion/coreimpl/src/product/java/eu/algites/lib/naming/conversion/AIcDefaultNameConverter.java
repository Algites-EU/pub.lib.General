package eu.algites.lib.naming.conversion;

import eu.algites.lib.naming.convention.AIcdInputVersionPolicy;
import eu.algites.lib.naming.convention.AIcdOutputVersionPolicy;
import eu.algites.lib.naming.convention.AInNameConvention;
import eu.algites.lib.naming.convention.AInVersionSource;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Default deterministic naming converter based on explicit input conventions. */
public final class AIcDefaultNameConverter implements AIiNameConverter {
    /** Creates a stateless deterministic name converter. */
    public AIcDefaultNameConverter() {
    }
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    /**
     * Splits a name into normalized lower-case word tokens according to its declared convention.
     *
     * @param value source name
     * @param convention declared source convention
     * @return immutable normalized token list
     */
    @Override
    public List<String> tokenize(String value, AInNameConvention convention) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(convention, "convention");
        if (convention == AInNameConvention.AS_IS) return List.of(value);
        String[] raw;
        switch (convention) {
            case LOWER_SNAKE_CASE, UPPER_SNAKE_CASE -> raw = value.split("_", -1);
            case LOWER_KEBAB_CASE, UPPER_KEBAB_CASE -> raw = value.split("-", -1);
            case LOWER_CAMEL_CASE, UPPER_CAMEL_CASE -> raw = CAMEL_BOUNDARY.split(value, -1);
            default -> raw = new String[] {value};
        }
        List<String> result = new ArrayList<>();
        for (String item : raw) {
            if (item.isEmpty()) throw new IllegalArgumentException("Name contains an empty word boundary: " + value);
            result.add(item.toLowerCase(Locale.ROOT));
        }
        return List.copyOf(result);
    }

    /**
     * Renders normalized word tokens using the requested target convention.
     *
     * @param tokens normalized word tokens
     * @param convention requested target convention
     * @return rendered name
     */
    @Override
    public String render(List<String> tokens, AInNameConvention convention) {
        Objects.requireNonNull(tokens, "tokens");
        Objects.requireNonNull(convention, "convention");
        if (tokens.isEmpty()) return "";
        if (convention == AInNameConvention.AS_IS) return String.join("", tokens);
        return switch (convention) {
            case LOWER_SNAKE_CASE -> String.join("_", lower(tokens));
            case UPPER_SNAKE_CASE -> String.join("_", upper(tokens));
            case LOWER_KEBAB_CASE -> String.join("-", lower(tokens));
            case UPPER_KEBAB_CASE -> String.join("-", upper(tokens));
            case UPPER_CAMEL_CASE -> camel(tokens, true);
            case LOWER_CAMEL_CASE -> camel(tokens, false);
            default -> throw new IllegalStateException("Unhandled convention " + convention);
        };
    }

    /**
     * Converts a name between explicitly declared naming conventions.
     *
     * @param value source name
     * @param inputConvention source convention
     * @param outputConvention target convention
     * @return converted name
     */
    @Override
    public String convert(String value, AInNameConvention inputConvention, AInNameConvention outputConvention) {
        if (inputConvention == AInNameConvention.AS_IS && outputConvention == AInNameConvention.AS_IS) return value;
        return render(tokenize(value, inputConvention), outputConvention);
    }

    /**
     * Checks whether a name round-trips exactly through the requested convention.
     *
     * @param value name to check
     * @param convention expected convention
     * @return {@code true} when the name conforms
     */
    @Override
    public boolean conforms(String value, AInNameConvention convention) {
        try {
            if (convention == AInNameConvention.AS_IS) return true;
            return render(tokenize(value, convention), convention).equals(value);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Separates the logical source name and canonical version according to the input-version policy.
     *
     * @param value source name
     * @param explicitVersion explicit metadata version, or {@code null}
     * @param policy input-version policy
     * @return logical name and separately tracked canonical version
     */
    @Override
    public AIcdParsedVersionedName parseVersionedName(String value, Integer explicitVersion, AIcdInputVersionPolicy policy) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(policy, "policy");
        Integer fileVersion = null;
        String logical = value;
        if (policy.source() != AInVersionSource.EXPLICIT_METADATA) {
            String separator = Pattern.quote(policy.fileNameSeparator());
            Matcher matcher = Pattern.compile("^(.*)" + separator + "([0-9]+)$").matcher(value);
            if (matcher.matches()) {
                logical = matcher.group(1);
                fileVersion = Integer.valueOf(matcher.group(2));
            }
        }
        Integer selected = switch (policy.source()) {
            case EXPLICIT_METADATA -> explicitVersion;
            case FILE_NAME_SUFFIX -> fileVersion;
            case EXPLICIT_THEN_FILE_NAME -> explicitVersion != null ? explicitVersion : fileVersion;
        };
        if (policy.requireMatchingSources() && explicitVersion != null && fileVersion != null && !explicitVersion.equals(fileVersion)) {
            throw new IllegalArgumentException("Definition version metadata " + explicitVersion + " does not match file-name version " + fileVersion + " for " + value);
        }
        if (policy.source() == AInVersionSource.EXPLICIT_METADATA && selected == null) {
            throw new IllegalArgumentException("Definition version must be supplied explicitly for " + value);
        }
        if (policy.source() == AInVersionSource.FILE_NAME_SUFFIX && selected == null) {
            throw new IllegalArgumentException("Definition version must be present in the file name for " + value);
        }
        if (policy.requirePositiveInteger() && selected != null && selected < 1) {
            throw new IllegalArgumentException("Definition version must be a positive integer for " + value);
        }
        return new AIcdParsedVersionedName(logical, selected);
    }

    /**
     * Renders the canonical version according to the output-version policy.
     *
     * @param version canonical definition version
     * @param policy output-version policy
     * @return rendered version suffix
     */
    @Override
    public String renderVersion(Integer version, AIcdOutputVersionPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        if (!policy.includeVersion() || version == null) return "";
        return policy.separator() + policy.prefix() + version + policy.suffix();
    }

    private static List<String> lower(List<String> tokens) {
        return tokens.stream().map(value -> value.toLowerCase(Locale.ROOT)).toList();
    }

    private static List<String> upper(List<String> tokens) {
        return tokens.stream().map(value -> value.toUpperCase(Locale.ROOT)).toList();
    }

    private static String camel(List<String> tokens, boolean upperFirst) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i).toLowerCase(Locale.ROOT);
            if (i == 0 && !upperFirst) {
                result.append(token);
            } else if (!token.isEmpty()) {
                result.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1));
            }
        }
        return result.toString();
    }
}
