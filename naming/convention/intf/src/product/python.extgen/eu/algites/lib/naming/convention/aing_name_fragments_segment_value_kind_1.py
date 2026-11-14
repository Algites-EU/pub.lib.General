from __future__ import annotations
from typing import Any

from enum import Enum


class AIngNameFragmentsSegmentValueKind_1(str, Enum):
    """Determines how the segment value is interpreted during name construction.

    Canonical identity: eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValueKind; version: 1.
    Equivalent schema identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}NameFragmentsSegmentValueKind; version: 1."""

    CONSTANT_STRING = 'constant_string'
    IDENTIFIER = 'identifier'
    VERSION = 'version'
    VARIABLE = 'variable'

    def code(self) -> str:
        """Return the exact typed wire code."""
        return self.value
