package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcCustomVersionStructure}
 * </p>
 * <p>
 * Description: Simple reusable implementation of {@link AIiVersionStructure}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public final class AIcCustomVersionStructure implements AIiVersionStructure, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	private final String code;

	private final boolean versionBeforeBuild;

	@Nonnull
	private final String buildDelimiter;

	@Nonnull
	private final AInVersionBuildComparisonPolicy buildComparisonPolicy;

	public AIcCustomVersionStructure(
			@Nonnull final String aCode,
			final boolean aVersionBeforeBuild,
			@Nonnull final String aBuildDelimiter,
			@Nonnull final AInVersionBuildComparisonPolicy aBuildComparisonPolicy
	) {
		code = Objects.requireNonNull(aCode, "Code must not be null");
		versionBeforeBuild = aVersionBeforeBuild;
		buildDelimiter = Objects.requireNonNull(aBuildDelimiter, "Build delimiter must not be null");
		buildComparisonPolicy = Objects.requireNonNull(aBuildComparisonPolicy, "Build comparison policy must not be null");

		if (code.trim().isEmpty()) {
			throw new IllegalArgumentException("Code must not be empty");
		}
	}

	@Override
	@Nonnull
	public String code() {
		return code;
	}

	@Override
	public boolean versionBeforeBuild() {
		return versionBeforeBuild;
	}

	@Override
	@Nonnull
	public String buildDelimiter() {
		return buildDelimiter;
	}

	@Override
	@Nonnull
	public AInVersionBuildComparisonPolicy buildComparisonPolicy() {
		return buildComparisonPolicy;
	}
}
