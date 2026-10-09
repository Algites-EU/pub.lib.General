package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIcCustomVersionScheme}
 * </p>
 * <p>
 * Description: Simple reusable implementation of {@link AIiVersionScheme}.
 * </p>
 * <p>
 * This class exists to make it trivial to introduce a new version scheme without changing
 * an enum. You can use it in production code (for registries/configuration) as well as in tests.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcCustomVersionScheme implements AIiVersionScheme, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	private final String code;

	@Nonnull
	private final AIiVersionComparator versionComparator;

	@Nonnull
	private final AIiVersionStructure versionStructure;

	@Nonnull
	private final AIiVersionFormat versionFormat;

	@NotNull
	private final AIiVersionCodec versionCodec;

	public AIcCustomVersionScheme(
			@Nonnull final String aCode,
			@Nonnull final AIiVersionComparator aVersionComparator,
			@Nonnull final AIiVersionStructure aVersionStructure,
			@Nonnull final AIiVersionFormat aVersionFormat,
			@Nonnull final AIiVersionCodec aVersionCodec
	) {
		if (aCode.isBlank()) {
			throw new IllegalArgumentException("Code must not be empty or blank");
		}
		code = aCode.trim();
		versionComparator = aVersionComparator;
		versionStructure = aVersionStructure;
		versionFormat = aVersionFormat;
		versionCodec = aVersionCodec;
	}

	public AIcCustomVersionScheme(
			@Nonnull final String aCode,
			@Nonnull final AIiVersionComparator aVersionComparator,
			@Nonnull final AIiVersionStructure aVersionStructure,
			@Nonnull final AIiVersionFormat aVersionFormat
	) {
		this(aCode, aVersionComparator, aVersionStructure, aVersionFormat, AIcDefaultVersionCodec.INSTANCE);
	}

	@Override
	@Nonnull
	public String code() {
		return code;
	}

	@Override
	@Nonnull
	public AIiVersionComparator versionComparator() {
		return versionComparator;
	}

	@Override
	@Nonnull
	public AIiVersionStructure versionStructure() {
		return versionStructure;
	}

	@Override
	@Nonnull
	public AIiVersionFormat versionFormat() {
		return versionFormat;
	}

	@Override
	@NotNull public AIiVersionCodec versionCodec() {
		return versionCodec;
	}

}
