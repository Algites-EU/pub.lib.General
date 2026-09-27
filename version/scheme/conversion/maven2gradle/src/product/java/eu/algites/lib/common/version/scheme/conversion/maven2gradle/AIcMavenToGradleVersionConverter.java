package eu.algites.lib.common.version.scheme.conversion.maven2gradle;

import eu.algites.lib.common.version.scheme.conversion.AIrVersionConversionResult;
import eu.algites.lib.common.version.scheme.conversion.AIiVersionConverter;
import eu.algites.lib.common.version.scheme.gradle.AIrGradleVersionConstraint;
import eu.algites.lib.common.version.scheme.maven.AIrMavenVersionRequirement;
import jakarta.annotation.Nonnull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts Maven-native version requirements into Gradle rich constraints. */
public final class AIcMavenToGradleVersionConverter implements AIiVersionConverter<AIrMavenVersionRequirement, AIrGradleVersionConstraint> {
	private static final Pattern RANGE = Pattern.compile("^([\\[(])([^,]*),([^,]*)([\\])])$");
	private static final Pattern NUMERIC_VERSION = Pattern.compile("^[0-9]+(?:\\.[0-9]+)*$");

	@Override
	@Nonnull
	public AIrVersionConversionResult<AIrGradleVersionConstraint> convert(@Nonnull final AIrMavenVersionRequirement aSource) {
		String locText = aSource.text();
		Matcher locMatcher = RANGE.matcher(locText);
		if (!locMatcher.matches()) {
			return AIrVersionConversionResult.exact(AIrGradleVersionConstraint.require(locText));
		}
		String locLower = locMatcher.group(2).trim();
		String locUpper = locMatcher.group(3).trim();
		String locGradleRange = ("[".equals(locMatcher.group(1)) ? "[" : "]")
				+ locLower + "," + locUpper
				+ ("]".equals(locMatcher.group(4)) ? "]" : "[");
		boolean locNumericOnly = (locLower.isEmpty() || NUMERIC_VERSION.matcher(locLower).matches())
				&& (locUpper.isEmpty() || NUMERIC_VERSION.matcher(locUpper).matches());
		if (locNumericOnly) {
			return AIrVersionConversionResult.normalized(
					new AIrGradleVersionConstraint(null, locGradleRange, null, java.util.List.of()),
					"Maven range delimiters were normalized to Gradle range syntax");
		}
		return new AIrVersionConversionResult<>(
				new AIrGradleVersionConstraint(null, locGradleRange, null, java.util.List.of()),
				eu.algites.lib.common.version.scheme.conversion.AInVersionConversionQuality.LOSSY,
				java.util.List.of("Maven and Gradle may order qualifier-bearing versions differently"));
	}
}
