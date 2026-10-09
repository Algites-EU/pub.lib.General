package eu.algites.lib.common.version;

import eu.algites.lib.common.enums.AIiEnumItem;
import eu.algites.lib.common.enums.uiddata.AIiUidEnumDataRecord;
import eu.algites.lib.common.enums.uiddata.AIiUidPartMetadata;
import eu.algites.lib.common.enums.uiddata.AInUidEnumDataOrigin;
import eu.algites.lib.common.enums.uiddata.AIrUidPartMetadata;
import eu.algites.lib.common.object.props.labels.AIaFieldLabel;
import eu.algites.lib.common.object.props.labels.AIsFieldLabelUtils;

import java.util.List;

/**
 * <p>
 * Title: {@link AIiVersionSchemeDataUidRecord}
 * </p>
 * <p>
 * Description:Interface for the Binding set inheritance purpose record
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 16.01.26 14:34
 */
public interface AIiVersionSchemeDataUidRecord extends AIiUidEnumDataRecord, AIiEnumItem {

	@AIaFieldLabel(label = "version-scheme-uid")
	@Override
	String uid();

	@AIaFieldLabel(label = "version-scheme-origin")
	@Override
	AInUidEnumDataOrigin origin();

	@AIaFieldLabel(label = "version-scheme-namespace")
	@Override
	String namespace();

	/**
	 * The schemeCode field name
	 */
	@SuppressWarnings("all")
	String SCHEME_CODE_FIELD_NAME = "schemeCode";

	/**
	 * @return the schemeCode
	 */
	@AIaFieldLabel(label = "version-scheme-code")
	String schemeCode();

	@Override
	default String code() { return schemeCode(); }

	/**
	 * The metadata of the specific parts of the record, expressed by fields defined on this interface
	 */
	@SuppressWarnings("unchecked")
	List<AIiUidPartMetadata<AInUidEnumDataOrigin>> RECORD_SPECIFIC_PARTS_METADATA = List.of(
			new AIrUidPartMetadata(() -> AIsFieldLabelUtils.findLabel(
					AIiVersionSchemeDataUidRecord.class,
					SCHEME_CODE_FIELD_NAME), true));

	/**
	 * Static validation of the record fields.
	 */
	static void staticValidation() {
		AIsFieldLabelUtils.requirePropertyExists(
				AIiVersionSchemeDataUidRecord.class, SCHEME_CODE_FIELD_NAME);
	}


}
