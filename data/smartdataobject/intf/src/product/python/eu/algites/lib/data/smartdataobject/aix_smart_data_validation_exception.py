"""Validation error for effective SmartDataObject wire serialization."""

class AIxSmartDataValidationException(ValueError):
    """Preserves field-path diagnostics without silently serializing an invalid DTO."""
    def __init__(self, issues):
        self.issues = tuple(issues)
        super().__init__('SmartDataObject effective values are invalid: ' + str(self.issues))
