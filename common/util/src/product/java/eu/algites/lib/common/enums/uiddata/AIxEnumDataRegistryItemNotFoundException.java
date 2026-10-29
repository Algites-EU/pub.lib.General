package eu.algites.lib.common.enums.uiddata;

import eu.algites.lib.common.exception.AIxRuntimeException;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import java.util.function.Supplier;

/**
 * Item not found exception for {@link AIiUidEnumDataRegistry}.
 *
 * @author linhart1
 * @date 30.01.26
 */
public class AIxEnumDataRegistryItemNotFoundException extends AIxRuntimeException {

	/**
	 * Creates a enum data registry item not found exception instance with the supplied configuration.
	 * @param aMessageSupplier supplier producing the diagnostic message when needed
	 */
	public AIxEnumDataRegistryItemNotFoundException(final Supplier<String> aMessageSupplier) {
		super(aMessageSupplier, null, null, null);
	}
}
