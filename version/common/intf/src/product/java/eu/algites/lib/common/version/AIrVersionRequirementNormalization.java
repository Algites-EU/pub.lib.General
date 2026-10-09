package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Objects;

/** Result of normalizing a portable version requirement against a concrete version scheme. */
public record AIrVersionRequirementNormalization(
		@Nonnull AIiVersionRequirement requirement,
		@Nonnull List<String> informationMessages
) {
	public AIrVersionRequirementNormalization {
		Objects.requireNonNull(requirement, "Version requirement must not be null");
		informationMessages = List.copyOf(Objects.requireNonNull(informationMessages, "Information messages must not be null"));
	}
}
