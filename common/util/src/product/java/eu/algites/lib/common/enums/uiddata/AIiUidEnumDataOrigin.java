package eu.algites.lib.common.enums.uiddata;

import eu.algites.lib.common.enums.AIiEnumItem;

/**
 * <p>
 * Title: {@link AIiUidEnumDataOrigin}
 * </p>
 * <p>
 * Description: general interface for the enum uiddata origins
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 20.01.26 7:54
 */
public interface AIiUidEnumDataOrigin extends AIiEnumItem {
	/**
	 * Is a namespace required for this enum uiddata origin?
	 * @return true if namespace is required, false otherwise
	 */
	boolean namespaceUsed();
}
