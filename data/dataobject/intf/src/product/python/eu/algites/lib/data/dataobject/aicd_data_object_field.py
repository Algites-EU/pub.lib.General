"""Normalized field identity, presence constraints and representation metadata."""
from dataclasses import dataclass
from typing import Callable, Any
from .ain_field_presence_kind import AInFieldPresenceKind


@dataclass(frozen=True, slots=True)
class AIcdDataObjectField:
    """Read-only field descriptor shared across plain DataObject and SmartDataObject."""
    name: str
    description: str = ''
    presence: AInFieldPresenceKind = AInFieldPresenceKind.ALLOWED
    allows_null: bool = False
    default_factory: Callable[[], Any] | None = None
    local_name: str = ''
    namespace: str = ''
    xml_attribute: bool = False
    expected_type: type | None = None
    getter_name: str = ''
    open_choice: bool = False
