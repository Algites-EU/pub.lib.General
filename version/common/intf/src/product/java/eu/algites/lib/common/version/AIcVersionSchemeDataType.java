package eu.algites.lib.common.version;

import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.ORIGIN_UID_POSITION;

import eu.algites.lib.common.enums.uiddata.AIiUidPartMetadata;
import eu.algites.lib.common.enums.uiddata.AInUidEnumDataOrigin;

import java.util.List;
import java.util.function.BiFunction;

/**
 * <p>
 * Title: {@link AIcVersionSchemeDataType}
 * </p>
 * <p>
 * Description: data type definition for the version scheme. By default works
 *    with {@link AInBuiltinVersionScheme} and {@link AIrVersionSchemeDataUidRecord}.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 28.01.26 13:01
 */
public class AIcVersionSchemeDataType implements AIiVersionSchemeDataType {

	@Override
	public BiFunction<String, List<String>, ? extends AIiVersionSchemeDataUidRecord> getUidRecordFactory() {
		return (aUid, aParts) -> {
			AInUidEnumDataOrigin locOrigin = AInUidEnumDataOrigin.getByCodeOrThrow(aParts.get(ORIGIN_UID_POSITION));
			if (locOrigin != AInUidEnumDataOrigin.BUILTIN)
				return AIrVersionSchemeDataUidRecord.getUidRecordFactory().apply(aUid, aParts);

			return AInBuiltinVersionScheme.getByUidOrThrow(aUid);
		};
	}

	@Override
	public List<AIiUidPartMetadata<AInUidEnumDataOrigin>> getSpecificUidPartsMetadata() {
		return AIiVersionSchemeDataUidRecord.RECORD_SPECIFIC_PARTS_METADATA;
	}

}
