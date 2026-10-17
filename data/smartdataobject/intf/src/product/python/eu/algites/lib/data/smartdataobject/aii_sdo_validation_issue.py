from __future__ import annotations
from eu.algites.lib.common.validation.aii_validation_issue import AIiValidationIssue

class AIiSdoValidationIssue(AIiValidationIssue):
    FIELD_SYSTEM_NAME = 'field.systemName'
    FIELD_LOCALIZED_NAME = 'field.name'
    FIELD_LOCALIZED_DESCRIPTION = 'field.description'

    def getFieldName(self) -> str | None:
        return self.getRelatedTopicText(self.FIELD_SYSTEM_NAME, None)

    def getFieldTitle(self, aLocale: str | None) -> str | None:
        return self.getRelatedTopicText(self.FIELD_LOCALIZED_NAME, aLocale)

    def getFieldDescription(self, aLocale: str | None) -> str | None:
        return self.getRelatedTopicText(self.FIELD_LOCALIZED_DESCRIPTION, aLocale)
