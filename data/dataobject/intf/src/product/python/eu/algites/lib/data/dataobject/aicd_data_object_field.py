"""Schema-neutral normalized data-object field metadata."""
from dataclasses import dataclass
from .ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from typing import Callable, Any
from .ain_field_presence_kind import AInFieldPresenceKind


@dataclass(frozen=True, slots=True)
class AIcdDataObjectField:
    """Field identifier, presence and nullability independent of schema wire format.

    ``default_value_resolver`` is a legacy callable used by non-annotation
    Python contracts; the separate default annotation is resolved on getters.
    """
    field_name: str
    embedded: bool = False
    presence: AInFieldPresenceKind = AInFieldPresenceKind.ALLOWED
    allows_null: bool = True
    default_value_resolver: Callable[[], Any] | None = None
    expected_type: type | None = None
    getter_name: str = ''
    setter_inheritance_policy: AInFieldSetterInheritancePolicy = AInFieldSetterInheritancePolicy.DEFAULT
    semantic_id: str = ''
    order: int = -1
    min_item_count: int = -1
    max_item_count: int = -1
    is_empty_allowed: bool = False
    string_constraints: Any = None
    numeric_constraints: Any = None
    list_constraints: Any = None
    object_constraints: Any = None

    def __post_init__(self) -> None:
        """Reject invalid schema-neutral collection limits."""
        if type(self.embedded) is not bool:
            raise TypeError('embedded must be Boolean')
        if self.embedded and self.field_name:
            raise ValueError('Embedded field cannot have a serialized field_name')
        if self.min_item_count < -1 or self.max_item_count < -1:
            raise ValueError('Invalid item count bound')
        if self.max_item_count >= 0 and self.min_item_count > self.max_item_count:
            raise ValueError('min_item_count exceeds max_item_count')

    @property
    def name(self) -> str:
        """Normalized internal field identity; wire name is held separately."""
        return self.semantic_id or self.field_name
