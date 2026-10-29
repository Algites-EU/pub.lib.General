package eu.algites.lib.common.enums.uiddata;

import java.util.Map;
import java.util.function.Supplier;

/**
 * <p>
 * Title: {@link AIiUidPartMetadata}
 * </p>
 * <p>
 * Description: General interface for the Uid parts metadata
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @param <O> type of the origin
 * @author linhart1
 * @date 20.01.26 7:47
 */
public interface AIiUidPartMetadata<O extends AIiUidEnumDataOrigin> {
	/**
	 * Supplier of Label to be displayed in the UI
	 * @return label
	 */
	Supplier<String> displayLabelSupplier();

	/**
	 * Whether the field value is required
	 *
	 * @return Map, where as key is the origina and value if the given field for the origin is required.
	 *    So contains true if required for origin for which the requirability has to be evaluated,
	 *    false otherwise. In the case the givne origin is unspecified in the map,
	 *    then as default is taken {@link Boolean#FALSE}.
	 *    If the Origin is specified in the map with null as the bool value, then {@link NullPointerException} is thrown.
	 */
	Map<O, Boolean> requiredForOrigin();
}
