package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * <p>
 * Title: {@link AIrVersionQualifier}
 * </p>
 * <p>
 * Description: Record implementation for the given
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 29.01.26 8:29
 */
public record AIrVersionQualifier(
		@Nonnull AInVersionQualifierKind kind,
		@Nullable String label
) implements AIiVersionQualifier {
}
