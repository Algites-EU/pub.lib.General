package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormatResolver;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputLocaleResolver;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolver;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinFormat;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

/**
 * <p>
 * Title: {@link AIaFieldLabel}
 * </p>
 * <p>
 * Description: Annotation for the custom field labeling used
 *    for the evaluation what label should be used for the field.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 6:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
public @interface AIaFieldLabel {
    /** Default role used by pre-existing field title annotations. */
    String DEFAULT_ROLE_CODE = "field.name";

    /** The semantic role of the primary label (not a rendering purpose). */
    String roleCode() default DEFAULT_ROLE_CODE;



	/* --- LABEL axis --- */

	/**
	 * Hard label definition (single label purpose)
	 * @return hard labe definition
	 */
	String label() default "";

	/**
	 * Mapping, according to purpose
	 * @return mapping, according to purpose
	 */
	Entry[] labels() default {};

	/**
	 * Resolver, used for the resolution of the labels.
	 * In the case with label resolver, the explicit labels assigned are ignored but are
	 * only resolved the value from the label resolver.
	 * @return label resolver used
	 */
	Class<? extends AIiFieldLabelResolver> labelResolver() default NoLabelResolver.class;

	/* --- MODE axis --- */


	/**
	 * Returns the built-in rendering purpose associated with this label.
	 * @return the result of the operation
	 */
	AInRenderingOutputBuiltinPurpose purpose() default AInRenderingOutputBuiltinPurpose.DEFAULT; /* nice enum selection in iDE */
	/**
	 * Returns an optional custom rendering purpose code.
	 * @return the result of the operation
	 */
	String purposeCode() default ""; /* for custom purposes without  enums */
	/**
	 * Returns the resolver used to select the rendering purpose.
	 * @return the result of the operation
	 */
	Class<? extends AIiRenderingOutputPurposeResolver> purposeResolver() default NoPurposeResolver.class;

	/**
	 * Formats  using the configured formatting rules.
	 * @return the format
	 */
	AInRenderingOutputBuiltinFormat format() default AInRenderingOutputBuiltinFormat.DEFAULT; /* nice enum selection in iDE */
	/**
	 * Formats code using the configured formatting rules.
	 * @return the format code
	 */
	String formatCode() default ""; /* for custom formats without  enums */
	/**
	 * Formats resolver using the configured formatting rules.
	 * @return the format resolver
	 */
	Class<? extends AIiRenderingOutputFormatResolver> formatResolver() default NoFormatResolver.class;

	/**
	 * Returns the explicitly configured locale code, or an empty value for the default.
	 * @return the result of the operation
	 */
	String localeCode() default ""; /* for custom locales without  enums */
	/**
	 * Returns the resolver used to determine the effective output locale.
	 * @return the result of the operation
	 */
	Class<? extends AIiRenderingOutputLocaleResolver> localeResolver() default NoLocaleResolver.class;
	
    @interface Entry {
        /** Field text role, independently of purpose, format and locale. */
        String roleCode() default DEFAULT_ROLE_CODE;


		/**
		 * Mode of the label if the default {@link AInRenderingOutputBuiltinPurpose} is sufficient to be used.
		 * @return purpose of the label
		 */
		AInRenderingOutputBuiltinPurpose purpose() default AInRenderingOutputBuiltinPurpose.DEFAULT;

		/**
		 * Mode of the label if the default {@link AInRenderingOutputBuiltinPurpose} is not sufficient.
		 * @return custom purpose code of the label
		 */
		String purposeCode() default "";

		/**
		 * Mode of the label if the default {@link AInRenderingOutputBuiltinFormat} is sufficient to be used.
		 * @return format of the label
		 */
		AInRenderingOutputBuiltinFormat format() default AInRenderingOutputBuiltinFormat.DEFAULT;

		/**
		 * Mode of the label if the default {@link AInRenderingOutputBuiltinFormat} is not sufficient.
		 * @return custom format code of the label
		 */
		String formatCode() default "";

		/**
		 * Mode of the label if the default {@link Locale} is not sufficient.
		 * @return custom locale code of the label
		 */
		String localeCode() default "";

		/**
		 * Label value oif the label resolver is not used, otherwise ignored
		 * @return label value
		 */
		String label();
	}

	abstract class NoLabelResolver implements AIiFieldLabelResolver {}
	abstract class NoPurposeResolver implements AIiRenderingOutputPurposeResolver {}
	abstract class NoFormatResolver implements AIiRenderingOutputFormatResolver {}
	abstract class NoLocaleResolver implements AIiRenderingOutputLocaleResolver {}
}

