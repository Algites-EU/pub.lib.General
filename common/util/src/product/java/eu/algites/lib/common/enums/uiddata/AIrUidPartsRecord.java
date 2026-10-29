package eu.algites.lib.common.enums.uiddata;

/**
 * <p>
 * Title: {@link AIrUidPartsRecord}
 * </p>
 * <p>
 * Description: General interface for the Uid parts records
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @param origin origin (builtin/custom)
 * @param namespace segment (empty for builtin, non-empty for custom)
 * @author linhart1
 * @date 18.01.26 15:46
 */
public record AIrUidPartsRecord(
		AInUidEnumDataOrigin origin,
		String namespace
) { }
