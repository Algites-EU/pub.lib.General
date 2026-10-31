"""Common enum-item contract for canonical serialized values in Algites.

This protocol is structural: both handwritten and generated Python enums can
implement it by exposing ``code()``; they must not inherit from ``Protocol``
when deriving from ``enum.Enum`` because their metaclasses are incompatible.
"""

from typing import Protocol, runtime_checkable


@runtime_checkable
class AIiEnumItem(Protocol):
    """Expose a stable serialized code for a member of an enum-like type.

    The code is the exact value written to documents, configuration, or storage
    and may differ from the Python member identifier (``name``). Codes are
    unique within one enum's members. Runtime protocol checks establish only
    the presence of ``code``; callers requiring a string must invoke it.
    """

    def code(self) -> str:
        """Return the non-null canonical code associated with this enum item.

        Returns:
            The exact wire-level value defined by the underlying schema,
            without conversion to upper case or member-name substitution.
        """
        ...
