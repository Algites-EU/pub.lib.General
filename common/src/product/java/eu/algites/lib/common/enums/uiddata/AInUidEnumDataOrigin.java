package eu.algites.lib.common.enums.uiddata;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Title: {@link AInUidEnumDataOrigin}
 * </p>
 * <p>
 * Description: Definition of the origin of the given uiddata - builtin or custom.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 13.01.26 17:00
 */
public enum AInUidEnumDataOrigin implements AIiUidEnumDataOrigin {
	/**
	 * Identifier origin managed by the built-in registry.
	 */
	BUILTIN("builtin", false),
	/**
	 * Identifier origin provided by application-defined data.
	 */
	CUSTOM("custom", true);

	private final String code;
	private boolean namespaceUsed;

	AInUidEnumDataOrigin(String aCode, final boolean aNamespaceUsed) {
		code = aCode;
		namespaceUsed = aNamespaceUsed;
	}

	/**
	 * @return kind class code used in UIDs ({@code builtin} or {@code custom})
	 */
	public String code() {
		return code;
	}

	/**
	 * Parse origin from an output type code.
	 *
	 * @param aCode kind class code
	 * @return parsed enum value
	 * @throws IllegalArgumentException if the code is unknown
	 */
	public static AInUidEnumDataOrigin getByCodeOrThrow(String aCode) throws IllegalArgumentException {
		final AInUidEnumDataOrigin builtin = findByCodeOrNull(aCode);
		if (builtin != null)
			return builtin;
		throw new IllegalArgumentException("Unsupported enumItemOrigin: '" + aCode + "'");
	}

	/**
	 * Parse origin from an output type code.
	 *
	 * @param aCode kind class code
	 * @return parsed enum value or null if no item matches
	 */
	public static @Nullable AInUidEnumDataOrigin findByCodeOrNull(final String aCode) {
		if (BUILTIN.code.equals(aCode)) {
			return BUILTIN;
		}
		if (CUSTOM.code.equals(aCode)) {
			return CUSTOM;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean namespaceUsed() {
		return namespaceUsed;
	}
}
