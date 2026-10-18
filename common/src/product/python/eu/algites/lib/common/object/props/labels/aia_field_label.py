from __future__ import annotations
from dataclasses import dataclass, field
from typing import Callable
from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose as Purpose
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_format import AInRenderingOutputBuiltinFormat as Format

@dataclass(frozen=True)
class AIaFieldLabel:
    """Python reflection counterpart of Java @AIaFieldLabel.

    Apply to a getter as a decorator, or attach in dataclass field metadata
    (metadata={'AIaFieldLabel': AIaFieldLabel(...)}) / __field_labels__.
    """
    FIELD_NAME = "field.systemName"
    FIELD_TITLE = "field.name"
    FIELD_DESCRIPTION = "field.description"
    DEFAULT_ROLE_CODE = FIELD_TITLE
    label: str = ''
    roleCode: str = DEFAULT_ROLE_CODE
    labels: tuple[Entry, ...] = ()  # type: ignore[name-defined]
    labelResolver: AIaCategoryProviderFactoryDefinition | None = None
    purpose: Purpose = Purpose.DEFAULT
    purposeCode: str = ''
    purposeResolver: AIaCategoryProviderFactoryDefinition | None = None
    format: Format = Format.DEFAULT
    formatCode: str = ''
    formatResolver: AIaCategoryProviderFactoryDefinition | None = None
    localeCode: str = ''
    localeResolver: AIaCategoryProviderFactoryDefinition | None = None

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

