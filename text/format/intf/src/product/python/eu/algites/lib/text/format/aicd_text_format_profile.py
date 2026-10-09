from dataclasses import dataclass
from .ain_text_format_type import AInTextFormatType
from .ain_text_reference_encoding import AInTextReferenceEncoding
from .ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode

@dataclass(frozen=True, slots=True)
class AIcdTextFormatProfile:
    """Immutable serializable profile descriptor."""
    id: str
    format_type: AInTextFormatType
    reference_encoding: AInTextReferenceEncoding
    diagnostic: bool
    supported_modes: frozenset[AInTextOutputObjectReferenceMode]
    def is_reference_mode_supported(self, mode: AInTextOutputObjectReferenceMode) -> bool:
        return mode in self.supported_modes
