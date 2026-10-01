from __future__ import annotations

from typing import Protocol, Sequence

from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention
from eu.algites.lib.naming.conversion.aicd_parsed_versioned_name import AIcdParsedVersionedName


class AIiNameConverter(Protocol):
    """Defines conversion and version-handling operations for governed names."""

    def tokenize(self, value: str, convention: AInNameConvention) -> Sequence[str]:
        """Split a governed name into normalized word tokens."""
        ...

    def render(self, tokens: Sequence[str], convention: AInNameConvention) -> str:
        """Render normalized word tokens using a target naming convention."""
        ...

    def convert(self, value: str, input_convention: AInNameConvention, output_convention: AInNameConvention) -> str:
        """Convert a name between explicitly declared naming conventions."""
        ...

    def conforms(self, value: str, convention: AInNameConvention) -> bool:
        """Return whether a name conforms exactly to a naming convention."""
        ...

    def parse_versioned_name(self, value: str, explicit_version: int | None, policy: AIcdInputVersionPolicy) -> AIcdParsedVersionedName:
        """Separate a logical name from its canonical definition version."""
        ...

    def render_version(self, version: int | None, policy: AIcdOutputVersionPolicy) -> str:
        """Render a canonical definition version using the configured output policy."""
        ...
