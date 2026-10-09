package eu.algites.lib.common.version;

import eu.algites.lib.common.enums.uiddata.AIiUidEnumData;
import eu.algites.lib.common.enums.uiddata.AInUidEnumDataOrigin;

/**
 * <p>
 * Title: {@link AIiVersionSchemeData}
 * </p>
 * <p>
 * Description: Defines the version scheme
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 18.01.26 15:28
 */
public interface AIiVersionSchemeData extends
		AIiUidEnumData<
				AIiVersionSchemeDataUidRecord, AInUidEnumDataOrigin,
				AIiVersionSchemeDataType>,
		AIiVersionSchemeDataUidRecord, AIiVersionScheme {

}
