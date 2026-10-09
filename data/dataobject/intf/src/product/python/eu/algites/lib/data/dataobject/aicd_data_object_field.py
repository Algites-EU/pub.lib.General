from dataclasses import dataclass
from typing import Callable, Any

@dataclass(frozen=True, slots=True)
class AIcdDataObjectField:
    """Normalized field descriptor independent of SmartDataObject implementations."""
    name: str
    description: str = ''
    presence_required: bool = False
    allows_null: bool = False
    default_factory: Callable[[], Any] | None = None
    xml_local_name: str = ''
    xml_namespace: str = ''
    xml_attribute: bool = False
    expected_type: type | None = None
    getter_name: str = '' 
