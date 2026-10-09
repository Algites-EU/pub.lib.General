from __future__ import annotations

from typing import Sequence
import re
from collections.abc import Sequence
from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.conversion.aicd_parsed_versioned_name import AIcdParsedVersionedName
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention
from eu.algites.lib.naming.convention.ain_version_source import AInVersionSource

class AIcDefaultNameConverter:
    """Provides default name converter functionality."""
    _camel_boundary = re.compile(r"(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")

    def tokenize(self, value: str, convention: AInNameConvention) -> tuple[str, ...]:
        """Split a governed name into normalized word tokens."""
        if convention is AInNameConvention.AS_IS:
            return (value,)
        if convention in (AInNameConvention.LOWER_SNAKE_CASE, AInNameConvention.UPPER_SNAKE_CASE):
            raw = value.split("_")
        elif convention in (AInNameConvention.LOWER_KEBAB_CASE, AInNameConvention.UPPER_KEBAB_CASE):
            raw = value.split("-")
        else:
            raw = self._camel_boundary.split(value)
        if any(not part for part in raw):
            raise ValueError(f"name contains an empty word boundary: {value}")
        return tuple(part.lower() for part in raw)

    def render(self, tokens: Sequence[str], convention: AInNameConvention) -> str:
        """Render normalized word tokens using a target naming convention."""
        if convention is AInNameConvention.AS_IS:
            return "".join(tokens)
        lowered = tuple(token.lower() for token in tokens)
        if convention is AInNameConvention.LOWER_SNAKE_CASE:
            return "_".join(lowered)
        if convention is AInNameConvention.UPPER_SNAKE_CASE:
            return "_".join(token.upper() for token in lowered)
        if convention is AInNameConvention.LOWER_KEBAB_CASE:
            return "-".join(lowered)
        if convention is AInNameConvention.UPPER_KEBAB_CASE:
            return "-".join(token.upper() for token in lowered)
        camel = "".join(token[:1].upper() + token[1:] for token in lowered)
        if convention is AInNameConvention.UPPER_CAMEL_CASE:
            return camel
        return camel[:1].lower() + camel[1:] if camel else camel

    def convert(self, value: str, input_convention: AInNameConvention, output_convention: AInNameConvention) -> str:
        """Convert a name between explicitly declared naming conventions."""
        if input_convention is AInNameConvention.AS_IS and output_convention is AInNameConvention.AS_IS:
            return value
        return self.render(self.tokenize(value, input_convention), output_convention)

    def conforms(self, value: str, convention: AInNameConvention) -> bool:
        """Return whether a name conforms exactly to a naming convention."""
        if convention is AInNameConvention.AS_IS:
            return True
        try:
            return self.render(self.tokenize(value, convention), convention) == value
        except ValueError:
            return False

    def parse_versioned_name(self, value: str, explicit_version: int | None, policy: AIcdInputVersionPolicy) -> AIcdParsedVersionedName:
        """Separate a logical name from its canonical definition version."""
        file_version = None
        logical = value
        if policy.source is not AInVersionSource.EXPLICIT_METADATA:
            match = re.fullmatch(rf"(.*){re.escape(policy.file_name_separator)}([0-9]+)", value)
            if match:
                logical, file_version = match.group(1), int(match.group(2))
        if policy.source is AInVersionSource.EXPLICIT_METADATA:
            selected = explicit_version
        elif policy.source is AInVersionSource.FILE_NAME_SUFFIX:
            selected = file_version
        else:
            selected = explicit_version if explicit_version is not None else file_version
        if policy.require_matching_sources and explicit_version is not None and file_version is not None and explicit_version != file_version:
            raise ValueError(f"definition version metadata {explicit_version} does not match file-name version {file_version} for {value}")
        if policy.source is AInVersionSource.EXPLICIT_METADATA and selected is None:
            raise ValueError(f"definition version must be supplied explicitly for {value}")
        if policy.source is AInVersionSource.FILE_NAME_SUFFIX and selected is None:
            raise ValueError(f"definition version must be present in the file name for {value}")
        if policy.require_positive_integer and selected is not None and selected < 1:
            raise ValueError(f"definition version must be a positive integer for {value}")
        return AIcdParsedVersionedName(logical, selected)

    def render_version(self, version: int | None, policy: AIcdOutputVersionPolicy) -> str:
        """Render a canonical definition version using the configured output policy."""
        if not policy.include_version or version is None:
            return ""
        return f"{policy.separator}{policy.prefix}{version}{policy.suffix}"
