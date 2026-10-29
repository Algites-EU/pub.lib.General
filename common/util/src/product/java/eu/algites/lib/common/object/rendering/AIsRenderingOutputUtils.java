package eu.algites.lib.common.object.rendering;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIsRenderingOutputUtils}
 * </p>
 * <p>
 * Description: General utilities for the Java objects functionality handling.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 4:14
 */
public class AIsRenderingOutputUtils {

	private static ThreadLocal<AIiRenderingOutputPurpose> RENDERING_OUTPUT_PURPOSE = ThreadLocal.withInitial(() -> AInRenderingOutputBuiltinPurpose.DEFAULT);
	private static ThreadLocal<AIiRenderingOutputFormat> RENDERING_OUTPUT_FORMAT = ThreadLocal.withInitial(() -> AInRenderingOutputBuiltinFormat.DEFAULT);
	private static ThreadLocal<Locale> RENDERING_OUTPUT_LOCALE = ThreadLocal.withInitial(Locale::getDefault);


	/**
	 * Gets the information if the current rendering output is user-friendly with external namings
	 * @param aRenderingOutputPurpose output purpose to be checked
	 * @return true if the Strings have to be produced with passed OutputPurpose parameter and false otherwise.
	 */
	public static boolean isUsedRenderingOutputPurpose(final AIiRenderingOutputPurpose aRenderingOutputPurpose) {
		return usedRenderingOutputPurpose() != aRenderingOutputPurpose;
	}

	/**
	 * Defines the currently used output purpose
	 * @return oputput purpose currently used
	 */
	public static AIiRenderingOutputPurpose usedRenderingOutputPurpose() {
		return RENDERING_OUTPUT_PURPOSE.get();
	}

	/**
	 * Defines as current rendering output to be the given purpose
	 * @param aRenderingOutputPurpose purpose to be used for the rendering outputs
	 */
	public static void useRenderingOutputPurpose(final AIiRenderingOutputPurpose aRenderingOutputPurpose) {
		RENDERING_OUTPUT_PURPOSE.set(aRenderingOutputPurpose);
	}

	/**
	 * Gets the information if the current rendering output is user-friendly with external namings
	 * @param aRenderingOutputFormat output format to be checked
	 * @return true if the Strings have to be produced with passed output format parameter and false otherwise.
	 */
	public static boolean isUsedRenderingOutputFormat(final AIiRenderingOutputFormat aRenderingOutputFormat) {
		return usedRenderingOutputFormat() != aRenderingOutputFormat;
	}

	/**
	 * Defines the currently used output format
	 * @return oputput format currently used
	 */
	public static AIiRenderingOutputFormat usedRenderingOutputFormat() {
		return RENDERING_OUTPUT_FORMAT.get();
	}

	/**
	 * Defines as current rendering output to be the given format
	 * @param aRenderingOutputFormat format to be used for the rendering outputs
	 */
	public static void useRenderingOutputFormat(final AIiRenderingOutputFormat aRenderingOutputFormat) {
		RENDERING_OUTPUT_FORMAT.set(aRenderingOutputFormat);
	}

	/**
	 * Gets the information if the current rendering output is user-friendly with external namings
	 * @param aRenderingOutputLocale output format to be checked
	 * @return true if the Strings have to be produced with passed output format parameter and false otherwise.
	 */
	public static boolean isUsedRenderingOutputLocale(final Locale aRenderingOutputLocale) {
		return Objects.equals(usedRenderingOutputLocale(),  aRenderingOutputLocale);
	}

	/**
	 * Defines the currently used output format
	 * @return oputput format currently used
	 */
	public static Locale usedRenderingOutputLocale() {
		return RENDERING_OUTPUT_LOCALE.get();
	}

	/**
	 * Defines as current rendering output to be the given format
	 * @param aRenderingOutputLocale format to be used for the rendering outputs
	 */
	public static void useRenderingOutputLocale(final Locale aRenderingOutputLocale) {
		RENDERING_OUTPUT_LOCALE.set(aRenderingOutputLocale);
	}

	/**
	 * Resolves the rendering output according to the current purpose. The supplier can use the method
	 *
	 * @param aStringOutputSupplier supplier to be called, delivering the message. The supplier should ask for the message according to the
	 * 		current purpose returned by usage of call {@link #usedRenderingOutputPurpose()} and {@link #usedRenderingOutputFormat()}.
	 * @param aRenderingOutputPurpose rendering output purpose to be used for evaluation
	 * @param aRenderingOutputFormat rendering output format to be used for evaluation
	 * @param aRenderingLocale rendering output locale to be used for evaluation
	 * @return the rendering output according to the current purpose returned from the supplier
	 */
	public static String resolveStringOutput(@NotNull Supplier<String> aStringOutputSupplier, final AIiRenderingOutputPurpose aRenderingOutputPurpose,
			final AIiRenderingOutputFormat aRenderingOutputFormat,
			final Locale aRenderingLocale) {
		AIiRenderingOutputPurpose locOriginalPurpose = usedRenderingOutputPurpose();
		useRenderingOutputPurpose(aRenderingOutputPurpose == null ? locOriginalPurpose : aRenderingOutputPurpose);
		AIiRenderingOutputFormat locOriginalFormat = usedRenderingOutputFormat();
		useRenderingOutputFormat(aRenderingOutputFormat == null ? locOriginalFormat : aRenderingOutputFormat);
		Locale locOriginalLocale = usedRenderingOutputLocale();
		useRenderingOutputLocale(aRenderingLocale == null ? locOriginalLocale : aRenderingLocale);
		try {
			return aStringOutputSupplier.get();
		} finally {
			useRenderingOutputPurpose(locOriginalPurpose);
			useRenderingOutputFormat(locOriginalFormat);
			useRenderingOutputLocale(locOriginalLocale);
		}
	}

}
