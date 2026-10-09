package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcCustomVersionFormat}
 * </p>
 * <p>
 * Description: Simple reusable implementation of {@link AIiVersionFormat}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public final class AIcCustomVersionFormat implements AIiVersionFormat, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	private final String code;

	@Nonnull
	private final AInVersionBuildFormatPolicy buildFormatPolicy;

	@Nonnull
	private final String mappedBuildPrefix;

	@Nonnull
	private final String qualifierDelimiter;

	@Nonnull
	private final String qualifierTokenDelimiter;

	public AIcCustomVersionFormat(
			@Nonnull final String aCode,
			@Nonnull final AInVersionBuildFormatPolicy aBuildFormatPolicy
	) {
		this(aCode, aBuildFormatPolicy, "build", "-", ".");
	}

	public AIcCustomVersionFormat(
			@Nonnull final String aCode,
			@Nonnull final AInVersionBuildFormatPolicy aBuildFormatPolicy,
			@Nonnull final String aMappedBuildPrefix,
			@Nonnull final String aQualifierDelimiter,
			@Nonnull final String aQualifierTokenDelimiter
	) {
		code = Objects.requireNonNull(aCode, "Code must not be null");
		buildFormatPolicy = Objects.requireNonNull(aBuildFormatPolicy, "Build format policy must not be null");
		mappedBuildPrefix = Objects.requireNonNull(aMappedBuildPrefix, "Mapped build prefix must not be null");
		qualifierDelimiter = Objects.requireNonNull(aQualifierDelimiter, "Qualifier delimiter must not be null");
		qualifierTokenDelimiter = Objects.requireNonNull(aQualifierTokenDelimiter, "Qualifier token delimiter must not be null");

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
	@Nonnull
	public AInVersionBuildFormatPolicy buildFormatPolicy() {
		return buildFormatPolicy;
	}

	@Override
	@Nonnull
	public String mappedBuildPrefix() {
		return mappedBuildPrefix;
	}

	@Override
	@Nonnull
	public String qualifierDelimiter() {
		return qualifierDelimiter;
	}

	@Override
	@Nonnull
	public String qualifierTokenDelimiter() {
		return qualifierTokenDelimiter;
	}
}
