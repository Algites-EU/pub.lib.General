package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Default immutable implementation of {@link AIiVersionBound}. */
public final class AIcVersionBound implements AIiVersionBound {
	@Nonnull
	private final String versionText;
	private final boolean inclusive;

	public AIcVersionBound(@Nonnull final String aVersionText, final boolean aInclusive) {
		versionText = Objects.requireNonNull(aVersionText, "Version text must not be null").trim();
		if (versionText.isEmpty()) {
			throw new IllegalArgumentException("Version text must not be blank");
		}
		inclusive = aInclusive;
	}

	@Override
	@Nonnull
	public String versionText() {
		return versionText;
	}

	@Override
	public boolean inclusive() {
		return inclusive;
	}
}
