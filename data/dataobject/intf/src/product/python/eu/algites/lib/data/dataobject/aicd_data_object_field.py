"""Schema-neutral normalized data-object field metadata."""
from dataclasses import dataclass
from typing import Callable, Any
from .ain_field_presence_kind import AInFieldPresenceKind


@dataclass(frozen=True, slots=True)
class AIcdDataObjectField:
    """Field identifier, presence and nullability independent of schema wire format.

    ``default_value_resolver`` is a callable compatibility interface used by
    non-annotation Python contracts; generated contracts may use AIaFieldDefault
    and the CategoryProviderDiscovery factory selection instead.
    """
    field_name: str
    presence: AInFieldPresenceKind = AInFieldPresenceKind.ALLOWED
    allows_null: bool = True
    default_value_resolver: Callable[[], Any] | None = None
    expected_type: type | None = None
    getter_name: str = ''

    @property
    def name(self) -> str:
        """Normalized internal field name, retained for SmartDataObject runtime callers."""
        return self.field_name
