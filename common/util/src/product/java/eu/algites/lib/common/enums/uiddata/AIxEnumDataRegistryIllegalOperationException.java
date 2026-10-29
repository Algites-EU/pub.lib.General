package eu.algites.lib.common.enums.uiddata;

import eu.algites.lib.common.exception.AIxRuntimeException;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import java.util.function.Supplier;

/**
 * Illegal operation exception for {@link AIiUidEnumDataRegistry}.
 *
 * @author linhart1
 * @date 30.01.26
 */
public class AIxEnumDataRegistryIllegalOperationException extends AIxRuntimeException {

	/**
	 * Creates a enum data registry illegal operation exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 */
	public AIxEnumDataRegistryIllegalOperationException(final Supplier<String> aMessageSupplier) {
		super(aMessageSupplier, null, null, null);
	}

	/**
	 * Creates a enum data registry illegal operation exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 * @param aCause underlying cause of the failure
	 */
	public AIxEnumDataRegistryIllegalOperationException(final Supplier<String> aMessageSupplier, final Throwable aCause) {
		super(aMessageSupplier, aCause, null, null, null);
	}
}
