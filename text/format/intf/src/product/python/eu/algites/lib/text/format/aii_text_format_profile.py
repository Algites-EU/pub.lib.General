from __future__ import annotations
from typing import Protocol
from .ain_text_format_type import AInTextFormatType
from .ain_text_reference_encoding import AInTextReferenceEncoding
from .ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode

class AIiTextFormatProfile(Protocol):
    """Immutable descriptor of a wire or diagnostic text profile."""
    @property
    def id(self) -> str: ...
    @property
    def format_type(self) -> AInTextFormatType: ...
    @property
    def reference_encoding(self) -> AInTextReferenceEncoding: ...
    @property
    def diagnostic(self) -> bool: ...
    def is_reference_mode_supported(self, mode: AInTextOutputObjectReferenceMode) -> bool: ...
