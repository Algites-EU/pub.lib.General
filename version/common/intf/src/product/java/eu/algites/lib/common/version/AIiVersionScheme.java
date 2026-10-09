package eu.algites.lib.common.version;

import eu.algites.lib.common.enums.AIiEnumItem;
import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AIiVersionScheme}
 * </p>
 * <p>
 * Description: General interface for version comparison schemes.
 * </p>
 * <p>
 * Each scheme provides a {@link AIiVersionComparator} implementation that defines
 * how {@link AIcVersion} instances are compared.
 * </p>
 * <p>
 * In addition, a scheme can expose configuration describing how build-identification is represented
 * (delimiter, ordering) and whether it participates in precedence.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 26.01.26 22:45
 */
public interface AIiVersionScheme extends AIiEnumItem {

	/**
	 * Provides the comparator implementing the given scheme.
	 *
	 * @return comparator for this scheme
	 */
	AIiVersionComparator versionComparator();

	/**
	 * Provides the structural interpretation for this scheme.
	 *
	 * @return version structure
	 */
	@Nonnull
	AIiVersionStructure versionStructure();

	/**
	 * Provides the format specification for this scheme.
	 *
	 * @return version format specification
	 */
	@Nonnull
	AIiVersionFormat versionFormat();

	/**
	 * @return codec to parse and format versions for this scheme
	 */
	@Nonnull
	AIiVersionCodec versionCodec();

}
