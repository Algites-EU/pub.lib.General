"""Method-bound Python counterpart of Java ``@AIaDataObjectField``."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Callable

from .aicd_data_object_field import AIcdDataObjectField
from .ain_field_presence_kind import AInFieldPresenceKind
from .ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy


@dataclass(frozen=True, slots=True)
class AIaDataObjectField:
    """Bind field metadata to a read-only getter without a repeated getter name.

    ``field_name`` is an opaque external data property. Internal semantic field
    identity is resolved from the decorated method's name by the selected input
    convention, not from the contents of ``field_name``. A method-bound field
    cannot override its effective data name inherited from a parent contract.
    """
    field_name: str = ''
    presence: AInFieldPresenceKind = AInFieldPresenceKind.ALLOWED
    allows_null: bool = True
    default_value_resolver: Callable[[], Any] | None = None
    expected_type: type | None = None
    setter_inheritance_policy: AInFieldSetterInheritancePolicy = AInFieldSetterInheritancePolicy.DEFAULT

    def __call__(self, getter):
        """Attach a runtime descriptor to the getter without wrapping its behavior."""
        if not callable(getter):
            raise TypeError('AIaDataObjectField requires a callable getter')
        setattr(getter, '__data_object_field__', AIcdDataObjectField(
            field_name=self.field_name,
            presence=self.presence,
            allows_null=self.allows_null,
            default_value_resolver=self.default_value_resolver,
            expected_type=self.expected_type,
            getter_name=getter.__name__,
            setter_inheritance_policy=self.setter_inheritance_policy))
        return getter
