"""Schema-independent field constraints for list data."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldListConstraints:
    """List restrictions, not raw presence or default substitution."""
    min_items: int = -1  # minimum number of elements; -1 disables
    max_items: int = -1  # maximum number of elements; -1 disables
    unique_items: bool = False  # reject duplicate elements

    def __call__(self, getter):
        """Attach constraints to a getter without wrapping the original method."""
        setattr(getter, '__data_object_field_list_constraints__', self)
        return getter
