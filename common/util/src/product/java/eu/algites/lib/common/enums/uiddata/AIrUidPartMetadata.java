package eu.algites.lib.common.enums.uiddata;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>
 * Title: {@link AIrUidPartMetadata}
 * </p>
 * <p>
 * Description: Metadata for the UID segment handling.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 * @param displayLabelSupplier field name of the field in the UID record to be used for the resolution of the display label in UI
 * @param requiredForOrigin whether the field is required for given origin
 * @param <O> type of the origin
 *
 * @author linhart1
 * @date 20.01.26 7:44
 */
public record AIrUidPartMetadata<O extends AIiUidEnumDataOrigin>(
		Supplier<String> displayLabelSupplier,
		Map<O, Boolean> requiredForOrigin
) implements AIiUidPartMetadata<O> {

	/**
	 * Constructor
	 * @param aDisplayLabelSupplier field name of the field in the UID record to be used for the resolution of the display label in UI
	 * @param aOriginValues origin-like enum values for which the requirement is defined
	 * @param aRequiredForAllItems whether the field is required for all given origins
	 */
	public AIrUidPartMetadata(final Supplier<String> aDisplayLabelSupplier, O[] aOriginValues, boolean aRequiredForAllItems) {
		this(aDisplayLabelSupplier, Arrays.stream(aOriginValues)
				.collect(Collectors.toMap(
						locEnumValue -> locEnumValue,
						locEnumValue -> aRequiredForAllItems
				)));
	}

	/**
	 * Constructor using the default builtin origin {@link AInUidEnumDataOrigin} for all values.
	 * @param aDisplayLabelSupplier field name of the field in the UID record to be used for the resolution of the display label in UI
	 * @param aRequiredForAllItems whether the field is required for all given origins
	 */
	@SuppressWarnings("unchecked")
	public AIrUidPartMetadata(final Supplier<String> aDisplayLabelSupplier, boolean aRequiredForAllItems) {
		this(aDisplayLabelSupplier, (O[]) AInUidEnumDataOrigin.values(), aRequiredForAllItems);
	}
}
