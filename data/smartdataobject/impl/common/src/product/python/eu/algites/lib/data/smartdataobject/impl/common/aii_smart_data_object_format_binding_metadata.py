"""Typed metadata contributions provided by one independent format plugin."""
from __future__ import annotations
from typing import Protocol

class AIiSmartDataObjectFormatBindingMetadata(Protocol):
    """One immutable format/provider contribution for an implementation class."""
    def getFormatId(self) -> str: ...
    def getProviderId(self) -> str: ...
