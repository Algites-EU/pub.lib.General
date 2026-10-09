package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.Locale;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Title: {@link AInVersionQualifierKind}
 * </p>
 * <p>
 * Description: Kind of the version of the artifact
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 08.01.26 0:44
 */
public enum AInVersionQualifierKind {
	SNAPSHOT("snapshot"),
	PRE_RELEASE("pre-release"),
	RELEASE("release"),
	POST_RELEASE("post-release"),
	;

	private final String kindCode;

	AInVersionQualifierKind(final String aKindCode) {
		kindCode = aKindCode;
	}

	/**
	 * Get kind by kindCode or throw exception
	 * @param aKindCode the kind code, MUST be not null
	 * @return the kind
	 * @throws IllegalArgumentException if the kindCode is unknown
	 */
	public static AInVersionQualifierKind getByCodeOrThrow(@Nonnull String aKindCode) throws IllegalArgumentException {
		final AInVersionQualifierKind kind = findByCode(aKindCode);
		if (kind != null)
			return kind;
		throw new IllegalArgumentException("Unsupported kind: '" + aKindCode + "'");
	}

	/**
	 * Find kind by code
	 * @param aKindCode the kind code
	 * @return the kind or null if not found
	 */
	public static @Nullable AInVersionQualifierKind findByCode(@Nonnull final String aKindCode) {
		Objects.requireNonNull(aKindCode, "Kind code MUST NOT be null");
		for (AInVersionQualifierKind kind : values()) {
			if (kind.getCode().equals(aKindCode)) {
				return kind;
			}
		}
		;
		return null;
	}

	/**
	 * @return the kind Code
	 */
	public String getCode() {
		return kindCode;
	}
	/**
	 * Returns whether a label is allowed for this kind.
	 *
	 * @return true if label is allowed
	 */
	public boolean isLabelAllowed() {
		return this != RELEASE;
	}

	/**
	 * Returns whether a non-blank label is required for this kind.
	 *
	 * @return true if label must be provided
	 */
	public boolean isLabelRequired() {
		return this == PRE_RELEASE || this == POST_RELEASE;
	}

	/**
	 * Produces canonical qualifier text (without the qualifier delimiter).
	 *
	 * @param aLabel optional qualifier label
	 * @return canonical qualifier text, or empty string for {@link #RELEASE}
	 */
	@Nonnull
	public String formatQualifierText(@Nullable final String aLabel) {
		if (!isLabelAllowed()) {
			if (aLabel != null && !aLabel.isBlank()) {
				throw new IllegalArgumentException("Label must be empty for RELEASE");
			}
			return "";
		}

		if (isLabelRequired()) {
			if (aLabel == null || aLabel.isBlank()) {
				throw new IllegalArgumentException("Label is required for kind " + this);
			}
			return aLabel;
		}

		/* SNAPSHOT */
		if (aLabel == null || aLabel.isBlank()) {
			return "SNAPSHOT";
		}

		/* Keep SNAPSHOT detectable and stable */
		return "SNAPSHOT." + aLabel;
	}

	/**
	 * Infers {@link AInVersionQualifierKind} from canonical qualifier text.
	 * <p>
	 * This assumes the text was produced by {@link #formatQualifierText(String)}.
	 * </p>
	 *
	 * @param aQualifierText canonical qualifier text (without the delimiter)
	 * @return detected kind
	 */
	@Nonnull
	public static AInVersionQualifierKind detectKindFromQualifierText(@Nullable final String aQualifierText) {
		if (aQualifierText == null || aQualifierText.isBlank()) {
			return RELEASE;
		}

		String locUpper = aQualifierText.toUpperCase(Locale.ROOT);
		if (locUpper.equals("SNAPSHOT") || locUpper.startsWith("SNAPSHOT.")) {
			return SNAPSHOT;
		}

		/* Ambiguous by nature: both PRE_RELEASE and POST_RELEASE are label-based governance. */
		return PRE_RELEASE;
	}
}
