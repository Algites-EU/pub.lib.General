"""Schema-independent field constraints for numeric data."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldNumericConstraints:
    """Numeric restrictions, not raw presence or default substitution."""
    minimum: str = ''  # inclusive decimal lower bound; empty disables
    maximum: str = ''  # inclusive decimal upper bound; empty disables
    exclusive_minimum: bool = False  # exclude the lower bound
    exclusive_maximum: bool = False  # exclude the upper bound
    multiple_of: str = ''  # positive decimal factor; empty disables
    format: str = ''  # declared schema format identifier; empty when absent

    def __call__(self, getter):
        """Attach constraints to a getter without wrapping the original method."""
        setattr(getter, '__data_object_field_numeric_constraints__', self)
        return getter
