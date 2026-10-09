package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AIiVersionQualifier}
 * </p>
 * <p>
 * Description: Defines the properties of qualifying the version and version handling
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 08.01.26 0:54
 */
public interface AIiVersionQualifier {

	/**
	 * Gets the kind of the qualifier
	 *
	 * @return the kind of the qualifier
	 */
	AInVersionQualifierKind kind();

	/**
	 * Gets the label of the qualifier
	 *
	 * @return the label of the qualifier
	 */
	String label();

}
