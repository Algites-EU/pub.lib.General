from __future__ import annotations
from dataclasses import dataclass, field
from typing import Callable
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose as Purpose
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_format import AInRenderingOutputBuiltinFormat as Format

@dataclass(frozen=True)
class AIaFieldLabel:
    """Python reflection counterpart of Java @AIaFieldLabel.

    Apply to a getter as a decorator, or attach in dataclass field metadata
    (metadata={'AIaFieldLabel': AIaFieldLabel(...)}) / __field_labels__.
    """
    DEFAULT_ROLE_CODE = "field.name"
    label: str = ''
    roleCode: str = DEFAULT_ROLE_CODE
    labels: tuple[Entry, ...] = ()  # type: ignore[name-defined]
    labelResolver: type | None = None
    purpose: Purpose = Purpose.DEFAULT
    purposeCode: str = ''
    purposeResolver: type | None = None
    format: Format = Format.DEFAULT
    formatCode: str = ''
    formatResolver: type | None = None
    localeCode: str = ''
    localeResolver: type | None = None

    def __call__(self, aFunction):
        setattr(aFunction,'__field_label__',self)
        return aFunction

    @dataclass(frozen=True)
    class Entry:
        label: str
        roleCode: str = "field.name"
        purpose: Purpose = Purpose.DEFAULT
        purposeCode: str = ''
        format: Format = Format.DEFAULT
        formatCode: str = ''
        localeCode: str = ''

    class NoLabelResolver: pass
    class NoPurposeResolver: pass
    class NoFormatResolver: pass
    class NoLocaleResolver: pass
