package eu.algites.lib.common.enums;

/**
 * <p>
 * Title: {@link AIiEnumItem}
 * </p>
 * <p>
 * Description: Basic interface for enums and enum-like classes
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @param <CT> the type of the code
 * @author linhart1
 * @date 20.01.26 6:58
 */
public interface AIiEnumItem<CT> {
	/**
	 * Gets the code of the enum. It is the value, used for writing into databases
	 * or configuration files, the final code how the enum item is presented
	 * outside of the java classes.
	 * @return code of the item. It is unique between the all codes
	 *    belonging to the enum type or to the enum-like type.
	 */
	CT code();

	/**
	 * Defines the type of the code
	 * @return type of the used code
	 */
	default Class<? extends CT> codeClass() {
		return (Class<? extends CT>) code().getClass();
	}
}
