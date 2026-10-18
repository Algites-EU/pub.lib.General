from __future__ import annotations
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel
from eu.algites.lib.common.validation.aii_validation_issue import AIiValidationIssue

class AIiSdoValidationIssue(AIiValidationIssue):

    def getFieldName(self) -> str | None:
        return self.getRelatedTopicText(AIaFieldLabel.FIELD_NAME, None)

    def getFieldTitle(self, aLocale: str | None) -> str | None:
        return self.getRelatedTopicText(AIaFieldLabel.FIELD_TITLE, aLocale)

    def getFieldDescription(self, aLocale: str | None) -> str | None:
        return self.getRelatedTopicText(AIaFieldLabel.FIELD_DESCRIPTION, aLocale)
