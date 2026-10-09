package eu.algites.lib.common.version;

import java.util.Comparator;

/**
 * <p>
 * Title: {@link AIiVersionComparator}
 * </p>
 * <p>
 * Description: Functional interface for comparing {@link AIcVersion} instances.
 * </p>
 * <p>
 * This interface intentionally extends {@link Comparator} to allow usage in sorting APIs.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
@FunctionalInterface
public interface AIiVersionComparator extends Comparator<AIcVersion> {

	@Override
	int compare(AIcVersion aLeft, AIcVersion aRight);
}
