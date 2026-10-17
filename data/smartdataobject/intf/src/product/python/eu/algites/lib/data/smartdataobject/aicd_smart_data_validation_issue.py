from __future__ import annotations
from dataclasses import dataclass
from eu.algites.lib.data.smartdataobject.aii_sdo_validation_issue import AIiSdoValidationIssue
from eu.algites.lib.common.validation.aii_validation_issue import AIiValidationIssue
from eu.algites.lib.common.object.props.labels.ais_field_label_utils import AIsFieldLabelUtils
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_format import AInRenderingOutputBuiltinFormat

@dataclass(frozen=True, slots=True)
class AIcdSmartDataValidationIssue(AIiSdoValidationIssue):
    """Technical issue with optional owner metadata and nested diagnostic."""
    path: str
    code: str
    message: str
    ownerType: type | None = None
    propertyName: str | None = None
    fieldDescription: str | None = None
    nestedIssue: AIiValidationIssue | None = None

    def getCode(self) -> str: return self.code
    def getPath(self) -> str: return self.path
    def getMessage(self, aLocale: str | None) -> str:
        if self.nestedIssue is not None: return self.nestedIssue.getMessage(aLocale)
        if aLocale is None: return self.message
        cs = str(aLocale).replace('_', '-').split('-', 1)[0].lower() == 'cs' 
        messages = {
            'REQUIRED_ABSENT': ('Povinná vlastnost není nastavena.', 'Required property is not present.'),
            'NULL_FORBIDDEN': ('Hodnota null není povolena.', 'Null value is not permitted.'),
            'WRONG_TYPE': ('Nesprávný datový typ.', 'Property has an incompatible type.'),
            'COLLECTION_INVALID': ('Neplatná kardinalita kolekce.', self.message),
        }
        pair = messages.get(self.code)
        return pair[0 if cs else 1] if pair is not None else self.message

    def getRelatedTopicText(self, aTopicId: str, aLocale: str | None) -> str | None:
        name = self.propertyName or self.path.rsplit('.', 1)[-1].split('[', 1)[0] or None
        if aTopicId == self.FIELD_SYSTEM_NAME: return name
        if aLocale is None:
            if aTopicId == self.FIELD_LOCALIZED_NAME: return name
            if aTopicId == self.FIELD_LOCALIZED_DESCRIPTION: return self.fieldDescription
        if aTopicId in (self.FIELD_LOCALIZED_NAME, self.FIELD_LOCALIZED_DESCRIPTION):
            label = None
            if self.ownerType is not None and name:
                prop = name[0].lower()+name[1:]
                label = AIsFieldLabelUtils.findLabel(self.ownerType, prop,
                         AInRenderingOutputBuiltinPurpose.USER, AInRenderingOutputBuiltinFormat.PLAIN_TEXT,
                         aLocale, aTopicId)
            if aTopicId == self.FIELD_LOCALIZED_NAME:
                return name if not label or (name and label == name[0].lower()+name[1:]) else label
            return label or self.fieldDescription or None
        return (self.nestedIssue.getRelatedTopicText(aTopicId, aLocale) if self.nestedIssue else None)
