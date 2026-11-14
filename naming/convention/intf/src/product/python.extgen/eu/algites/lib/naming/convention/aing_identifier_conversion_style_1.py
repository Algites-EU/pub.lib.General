from __future__ import annotations
from typing import Any

from enum import Enum


class AIngIdentifierConversionStyle_1(str, Enum):
    """Optional lexical splitting and formatting policy. Custom regex conventions without a conversion style are validation-only. The style, not the naming convention ID, controls conversion.

    Canonical identity: eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConversionStyle; version: 1.
    Equivalent schema identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}IdentifierConversionStyle; version: 1."""

    LOWER_FLAT = 'lower_flat'
    UPPER_FLAT = 'upper_flat'
    LOWER_SNAKE = 'lower_snake'
    UPPER_SNAKE = 'upper_snake'
    LOWER_CAMEL = 'lower_camel'
    UPPER_CAMEL = 'upper_camel'
    LOWER_KEBAB = 'lower_kebab'
    UPPER_KEBAB = 'upper_kebab'

    def code(self) -> str:
        """Return the exact typed wire code."""
        return self.value
