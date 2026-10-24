"""Select raw or effective SmartDataObject values without changing stored field presence."""
from enum import Enum


class AInSdoFieldValueLoadingMode(Enum):
    """Raw values preserve explicit assignments; effective values apply defaults when absent."""

    RAW_VALUES = 'RAW_VALUES'
    EFFECTIVE_VALUES = 'EFFECTIVE_VALUES'
