package eu.algites.lib.common.version;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcMavenLikeVersionComparator}
 * </p>
 * <p>
 * Description: Comparator implementing Maven-like version precedence rules.
 * </p>
 * <p>
 * Delegates the actual comparison logic to {@link AIsVersionComparator#compareMavenLike(AIcVersion, AIcVersion)}.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcMavenLikeVersionComparator implements AIiVersionComparator, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final AIcVersion aLeft, final AIcVersion aRight) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");
		return AIsVersionComparator.compareMavenLike(aLeft, aRight);
	}
}
