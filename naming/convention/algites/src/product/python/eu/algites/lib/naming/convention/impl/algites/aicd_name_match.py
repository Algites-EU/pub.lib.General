from __future__ import annotations

from dataclasses import dataclass
from typing import Mapping


@dataclass(frozen=True, slots=True)
class AIcdNameMatch:
    """One unambiguously recognized filename, with captured name variables."""

    convention_name: str
    variables: Mapping[str, str]
