package eu.algites.lib.common.interval;

import eu.algites.lib.common.enums.AIiEnumItem;

/**
 * <p>
 * Title: {@link AInIntervalBoundary}
 * </p>
 * <p>
 * Description: defines the interval boundary
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 26.01.26 15:36
 */
public enum AInIntervalBoundary implements AIiEnumItem {
	/**
	 * Exclusive interval endpoint; the boundary value is not included.
	 */
	OPEN("open", false, true, "(", ")"),
	/**
	 * Inclusive interval endpoint; the boundary value is included.
	 */
	CLOSED("closed", false, false, "[", "]"),
	/**
	 * Missing interval endpoint; no boundary value is imposed.
	 */
	UNBOUNDED("unbounded", true, true, "(*", "*)");

	private final String code;
	private final boolean boundaryValueIgnored;
	private final boolean open;
	private final String leftBoundaryStringRepresentation;
	private final String rightBoundaryStringRepresentation;

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @return the leftBoundaryStringRepresentation
	 */
	public String getLeftBoundaryStringRepresentation() {
		return leftBoundaryStringRepresentation;
	}

	/**
	 * @return the rightBoundaryStringRepresentation
	 */
	public String getRightBoundaryStringRepresentation() {
		return rightBoundaryStringRepresentation;
	}

	AInIntervalBoundary(final String aCode,
			final boolean aBoundaryValueIgnored,
			final boolean aOpen,
			String aLeftBoundaryStringRepresentation,
			String aRightBoundaryStringRepresentation) {
		code = aCode;
		boundaryValueIgnored = aBoundaryValueIgnored;
		open = aOpen;
		leftBoundaryStringRepresentation = aLeftBoundaryStringRepresentation;
		rightBoundaryStringRepresentation = aRightBoundaryStringRepresentation;
	}

	/** {@inheritDoc} */
	@Override
	public String code() {
		return code;
	}

	/**
	 * @return the boundaryValueIgnored
	 */
	public boolean isBoundaryValueIgnored() {
		return boundaryValueIgnored;
	}
}
