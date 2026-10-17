package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

/**
 * Result of normalizing a portable version requirement against a concrete version scheme.
 * @param requirement normalized version requirement
 * @param informationMessages informational messages generated while normalizing
 */
public record AIrVersionRequirementNormalization(
		@Nonnull AIiVersionRequirement requirement,
		@Nonnull List<String> informationMessages
) {
	/**
	 * Creates a version requirement normalization instance with the supplied configuration.
	 * @param requirement requirement supplied to the operation
	 * @param informationMessages information messages supplied to the operation
	 */
	public AIrVersionRequirementNormalization {
		Objects.requireNonNull(requirement, "Version requirement must not be null");
		informationMessages = List.copyOf(Objects.requireNonNull(informationMessages, "Information messages must not be null"));
	}
}
