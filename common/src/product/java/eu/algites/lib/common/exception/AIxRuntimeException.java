package eu.algites.lib.common.exception;

import static eu.algites.lib.common.object.rendering.AIsRenderingOutputUtils.resolveStringOutput;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import java.util.Locale;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIxRuntimeException}
 * </p>
 * <p>
 * Description: Exception for runtime errors
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 4:11
 */
public class AIxRuntimeException extends RuntimeException{
	private final AInRenderingOutputBuiltinPurpose messageOutputPurpose;

	/**
	 * Creates a runtime exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 * @param aCause underlying cause of the failure
	 * @param aMessageOutputPurpose purpose for rendering the exception message
	 * @param aRenderingOutputFormat requested output format for the exception message
	 * @param aLocale locale for presenting the message; null selects technical diagnostics
	 */
	public AIxRuntimeException(final Supplier<String> aMessageSupplier, final Throwable aCause, final AInRenderingOutputBuiltinPurpose aMessageOutputPurpose,
			final AIiRenderingOutputFormat aRenderingOutputFormat, final Locale aLocale) {
		super(resolveStringOutput(aMessageSupplier, aMessageOutputPurpose, aRenderingOutputFormat, aLocale), aCause);
		messageOutputPurpose = aMessageOutputPurpose;
	}

	/**
	 * Creates a runtime exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 * @param aCause underlying cause of the failure
	 * @param aEnableSuppression whether suppressed exceptions are enabled
	 * @param aWritableStackTrace whether stack trace collection is enabled
	 * @param aMessageOutputPurpose purpose for rendering the exception message
	 * @param aRenderingOutputFormat requested output format for the exception message
	 * @param aLocale locale for presenting the message; null selects technical diagnostics
	 */
	public AIxRuntimeException(
			final Supplier<String> aMessageSupplier,
			final Throwable aCause,
			final boolean aEnableSuppression,
			final boolean aWritableStackTrace,
			final AInRenderingOutputBuiltinPurpose aMessageOutputPurpose, final AIiRenderingOutputFormat aRenderingOutputFormat,
			final Locale aLocale) {
		super(resolveStringOutput(aMessageSupplier, aMessageOutputPurpose, aRenderingOutputFormat, aLocale), aCause, aEnableSuppression, aWritableStackTrace);
		messageOutputPurpose = aMessageOutputPurpose;
	}

	/**
	 * Creates a runtime exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 * @param aMessageOutputPurpose purpose for rendering the exception message
	 * @param aRenderingOutputFormat requested output format for the exception message
	 * @param aLocale locale for presenting the message; null selects technical diagnostics
	 */
	public AIxRuntimeException(@NotNull final Supplier<String> aMessageSupplier, AInRenderingOutputBuiltinPurpose aMessageOutputPurpose,
			final AIiRenderingOutputFormat aRenderingOutputFormat, final Locale aLocale) {
		super(resolveStringOutput(aMessageSupplier, aMessageOutputPurpose, aRenderingOutputFormat, aLocale));
		messageOutputPurpose = aMessageOutputPurpose;
	}

	/**
	 * @return the messageOutputPurpose
	 */
	public AInRenderingOutputBuiltinPurpose getMessageOutputPurpose() {
		return messageOutputPurpose;
	}
}
