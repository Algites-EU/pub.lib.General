package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormatResolverFactory;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputLocaleResolverFactory;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolverFactory;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinFormat;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

/**
 * Declares semantic labels for fields, record components and getters.
 *
 * <p>Role codes distinguish stable field names, display titles and explanatory
 * descriptions, independently of rendering purpose, format and locale. A simple
 * {@link #label()} supplies the default text for {@link #roleCode()}; {@link #labels()}
 * supplies additional role/locale/purpose/format variants. Resolver factories use
 * {@code CategoryProviderDiscovery} and are selected by specialized factory SPI,
 * category and optional provider ID, never by reflective resolver constructors.
 * An empty category <em>and</em> provider ID leave an optional resolver disabled.</p>
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
    /** Stable unlocalized schema/property identifier role. */
    String FIELD_NAME = "field.systemName";
    /** Human-readable title role, independently localized for diagnostics and rendering. */
    String FIELD_TITLE = "field.name";
    /** Human-readable explanatory description role. */
    String FIELD_DESCRIPTION = "field.description";

    /** Default human-readable label role is the field title. */
    String DEFAULT_ROLE_CODE = FIELD_TITLE;

    /**
     * Semantic field-text role of the primary literal label. This is not a rendering
     * purpose and does not change locale or presentation format selection.
     *
     * @return the title role unless explicitly overridden
     */
    String roleCode() default DEFAULT_ROLE_CODE;



	/* --- LABEL axis --- */

    /**
     * Literal fallback text for the primary {@link #roleCode()}.
     * An empty value means no literal fallback; additional texts can be provided
     * by {@link #labels()} with independent role/purpose/format/locale variants.
     *
     * @return literal primary label, or empty when absent
     */
	String label() default "";

    /**
     * Explicit alternatives resolved according to semantic role and rendering axes.
     * Specifying entries and an active label resolver together is unsupported.
     *
     * @return zero or more configured label entries
     */
	Entry[] labels() default {};

    /**
     * CPDiscovery factory selection for dynamic label text. An empty category and
     * provider ID mean no resolver is requested; when enabled, {@link #labels()}
     * must not contain literal alternatives.
     *
     * @return specialized label resolver factory definition
     */
	AIaCategoryProviderFactoryDefinition labelResolver() default
            @AIaCategoryProviderFactoryDefinition(factoryClass = AIiFieldLabelResolverFactory.class);

	/* --- MODE axis --- */


    /**
     * Built-in purpose used when caller and purpose resolver do not override it.
     *
     * @return the built-in rendering purpose (DEFAULT by default)
     */
	AInRenderingOutputBuiltinPurpose purpose() default AInRenderingOutputBuiltinPurpose.DEFAULT; /* nice enum selection in iDE */
    /**
     * Alternative custom purpose code when the built-in purpose is insufficient.
     *
     * @return custom purpose code or empty when unspecified
     */
	String purposeCode() default ""; /* for custom purposes without  enums */
    /**
     * Optional CPDiscovery factory for dynamic purpose selection.
     * Category/provider left empty disable dynamic resolution.
     *
     * @return specialized purpose resolver factory definition
     */
	AIaCategoryProviderFactoryDefinition purposeResolver() default
            @AIaCategoryProviderFactoryDefinition(factoryClass = AIiRenderingOutputPurposeResolverFactory.class);

    /**
     * Built-in output format associated with this annotation.
     *
     * @return built-in format or DEFAULT for ambient configuration
     */
	AInRenderingOutputBuiltinFormat format() default AInRenderingOutputBuiltinFormat.DEFAULT; /* nice enum selection in iDE */
    /**
     * Optional custom format code, independently of the semantic field role.
     *
     * @return format code or empty for no custom format
     */
	String formatCode() default ""; /* for custom formats without  enums */
    /**
     * Optional CPDiscovery factory for choosing a dynamic rendering format.
     * Empty category/provider disable dynamic resolution.
     *
     * @return specialized format resolver factory definition
     */
	AIaCategoryProviderFactoryDefinition formatResolver() default
            @AIaCategoryProviderFactoryDefinition(factoryClass = AIiRenderingOutputFormatResolverFactory.class);

    /**
     * Explicit output locale language tag, independent of the primary field name.
     *
     * @return configured locale code or empty for ambient locale selection
     */
	String localeCode() default ""; /* for custom locales without  enums */
    /**
     * Optional CPDiscovery factory for dynamic locale selection.
     * Empty category/provider disable dynamic resolution.
     *
     * @return specialized locale resolver factory definition
     */
	AIaCategoryProviderFactoryDefinition localeResolver() default
            @AIaCategoryProviderFactoryDefinition(factoryClass = AIiRenderingOutputLocaleResolverFactory.class);
	
    /**
     * One explicit text alternative for a semantic field role and rendering axes.
     * The locale and format may be omitted to provide less-specific fallbacks.
     */
    @interface Entry {
        /**
         * Role identifying the kind of field text represented by this entry.
         *
         * @return title role unless another semantic role is specified
         */
        String roleCode() default DEFAULT_ROLE_CODE;


        /**
         * Built-in rendering purpose matched by this text entry.
         *
         * @return built-in purpose, DEFAULT when not restricted
         */
		AInRenderingOutputBuiltinPurpose purpose() default AInRenderingOutputBuiltinPurpose.DEFAULT;

        /**
         * Optional custom purpose code for alternatives not covered by built-in purposes.
         *
         * @return custom purpose code or empty for no custom purpose
         */
		String purposeCode() default "";

        /**
         * Built-in output format matched by this text entry.
         *
         * @return built-in format, DEFAULT when unrestricted
         */
		AInRenderingOutputBuiltinFormat format() default AInRenderingOutputBuiltinFormat.DEFAULT;

        /**
         * Optional custom output format identifier for this text alternative.
         *
         * @return custom format code or empty for no custom format
         */
		String formatCode() default "";

        /**
         * Locale language tag for this entry. Empty means a locale-neutral fallback.
         *
         * @return language tag or empty for any locale
         */
		String localeCode() default "";

        /**
         * Literal text used when the role, purpose, format and locale match this entry.
         *
         * @return explicit label text
         */
		String label();
	}

}
