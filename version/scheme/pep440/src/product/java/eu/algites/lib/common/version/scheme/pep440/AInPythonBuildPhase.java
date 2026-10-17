package eu.algites.lib.common.version.scheme.pep440;

/** Python dependency-resolution phases derived from a portable version requirement. */
public enum AInPythonBuildPhase {
	/**
	 * Preferred constraint group used during PEP 440 conversion.
	 */
	PREFERRED,
	/**
	 * Non-strict upper bounds emitted for PEP 440 compatibility.
	 */
	NON_STRICT_MAXIMUMS,
	/**
	 * Strict upper bounds emitted for PEP 440 compatibility.
	 */
	STRICT_MAXIMUMS
}
