package eu.algites.lib.common.version.impl.standard;


import eu.algites.lib.common.version.AIiVersionBound;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Default immutable implementation of {@link AIiVersionBound}. */
public final class AIcVersionBound implements AIiVersionBound {
	@Nonnull
	private final String versionText;
	private final boolean inclusive;

	/**
	 * Creates a version bound with an endpoint value and its inclusiveness flag.
	 * @param aVersionText version text to interpret
	 * @param aInclusive inclusive supplied to the operation
	 */
	public AIcVersionBound(@Nonnull final String aVersionText, final boolean aInclusive) {
		versionText = Objects.requireNonNull(aVersionText, "Version text must not be null").trim();
		if (versionText.isEmpty()) {
			throw new IllegalArgumentException("Version text must not be blank");
		}
		inclusive = aInclusive;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String versionText() {
		return versionText;
	}

	/** {@inheritDoc} */
	@Override
	public boolean inclusive() {
		return inclusive;
	}
}
