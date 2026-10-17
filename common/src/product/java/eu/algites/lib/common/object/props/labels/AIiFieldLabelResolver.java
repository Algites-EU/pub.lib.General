package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose;

import java.util.Locale;

/**
 * <p>
 * Title: {@link AIiFieldLabelResolver}
 * </p>
 * <p>
 * Description: Interface defining the field label resolution
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 6:10
 */
public interface AIiFieldLabelResolver {
	/**
	 * Performs the class property label resolution
	 *
	 * @param aOwnerClass
	 * @param aFieldName
	 * @param aOutputPurpose
	 * @param aOutputFormat
	 * @param aOutputLocale
	 * @return label or null => fallback
	 */
    String resolveLabel(Class<?> aOwnerClass, String aFieldName, AIiRenderingOutputPurpose aOutputPurpose,
            AIiRenderingOutputFormat aOutputFormat, Locale aOutputLocale);

    /** Role-aware extension; old resolvers remain compatible for field titles. */
    default String resolveLabel(Class<?> aOwnerClass, String aFieldName, String aRoleCode,
            AIiRenderingOutputPurpose aOutputPurpose, AIiRenderingOutputFormat aOutputFormat,
            Locale aOutputLocale) {
        return AIaFieldLabel.DEFAULT_ROLE_CODE.equals(aRoleCode)
                ? resolveLabel(aOwnerClass, aFieldName, aOutputPurpose, aOutputFormat, aOutputLocale)
                : null;
    }
}
