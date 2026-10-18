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
     * Resolves the ordinary field title using the selected rendering axes.
     * Returning null or blank allows a declared literal or property-name fallback.
     *
     * @param aOwnerClass class declaring the field or getter
     * @param aFieldName Java property name to resolve
     * @param aOutputPurpose effective output purpose
     * @param aOutputFormat effective output format
     * @param aOutputLocale effective locale
     * @return resolved text or null to permit fallback
     */
    String resolveLabel(Class<?> aOwnerClass, String aFieldName, AIiRenderingOutputPurpose aOutputPurpose,
            AIiRenderingOutputFormat aOutputFormat, Locale aOutputLocale);

    /**
     * Resolves a selected semantic text role. Legacy implementations without role
     * support are applied only to {@link AIaFieldLabel#FIELD_TITLE}.
     *
     * @param aOwnerClass class declaring the property
     * @param aFieldName property name
     * @param aRoleCode desired semantic text role
     * @param aOutputPurpose effective rendering purpose
     * @param aOutputFormat effective rendering format
     * @param aOutputLocale effective locale
     * @return dynamic text or null when this resolver does not support the role
     */
    default String resolveLabel(Class<?> aOwnerClass, String aFieldName, String aRoleCode,
            AIiRenderingOutputPurpose aOutputPurpose, AIiRenderingOutputFormat aOutputFormat,
            Locale aOutputLocale) {
        return AIaFieldLabel.DEFAULT_ROLE_CODE.equals(aRoleCode)
                ? resolveLabel(aOwnerClass, aFieldName, aOutputPurpose, aOutputFormat, aOutputLocale)
                : null;
    }
}
