"""Schema-independent field constraints for string data."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldStringConstraints:
    """String restrictions, not raw presence or default substitution."""
    min_length: int = -1  # minimum Unicode code-point length; -1 disables
    max_length: int = -1  # maximum Unicode code-point length; -1 disables
    pattern: str = ''  # Java regular expression; empty disables
    format: str = ''  # declared schema format identifier; empty when absent

    def __call__(self, getter):
        """Attach constraints to a getter without wrapping the original method."""
        setattr(getter, '__data_object_field_string_constraints__', self)
        return getter
