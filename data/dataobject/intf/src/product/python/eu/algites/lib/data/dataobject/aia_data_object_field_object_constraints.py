"""Schema-independent field constraints for object data."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldObjectConstraints:
    """Object restrictions, not raw presence or default substitution."""
    min_properties: int = -1  # minimum number of properties; -1 disables
    max_properties: int = -1  # maximum number of properties; -1 disables

    def __call__(self, getter):
        """Attach constraints to a getter without wrapping the original method."""
        setattr(getter, '__data_object_field_object_constraints__', self)
        return getter
