from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIcdSmartDataValidationIssue:
    """One local or graph-path validation diagnostic."""
    path: str
    code: str
    message: str
