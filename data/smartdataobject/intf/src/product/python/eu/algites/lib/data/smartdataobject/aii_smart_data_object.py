from __future__ import annotations
from abc import abstractmethod
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.common.validation.aii_validatable import AIiValidatable
from eu.algites.lib.data.smartdataobject.aii_sdo_validation_issue import AIiSdoValidationIssue
from typing import Set, Mapping
from eu.algites.lib.data.smartdataobject.aiid_virtual_field_value import AIidVirtualFieldValue
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.text.format.aii_text_format_profile import AIiTextFormatProfile
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode

class AIiSmartDataObject(AIiDataObject, AIiValidatable[AIiSdoValidationIssue]):

    def getValidationIssues(self) -> list[AIiSdoValidationIssue]:
        return self.get_DeeplyEffectiveValuesValidationIssues()

    """Runtime contract implemented by mutable schema-generated data objects."""
    def isValid(self) -> bool:
        return self.is_DeeplyByEffectiveValuesValid()

    def getInvalidStateMessage(self, aLocale: str | None) -> str:
        def format_issue(issue: AIiSdoValidationIssue) -> str:
            title = issue.getFieldTitle(aLocale) if aLocale is not None else None
            location = title or issue.getPath()
            return (location + ': ' if location else '') + issue.getMessage(aLocale)
        return '\n'.join(format_issue(issue) for issue in self.getValidationIssues())

    @abstractmethod
    def is_ByRawValuesValid(self) -> bool: ...
    @abstractmethod
    def is_ByEffectiveValuesValid(self) -> bool: ...
    @abstractmethod
    def is_DeeplyByRawValuesValid(self) -> bool: ...
    @abstractmethod
    def is_DeeplyByEffectiveValuesValid(self) -> bool: ...
    @abstractmethod
    def get_RawNotPresentRequiredFields(self) -> Set[str]: ...
    @abstractmethod
    def get_EffectiveNotPresentRequiredFields(self) -> Set[str]: ...
    @abstractmethod
    def get_DeeplyRawNotPresentRequiredFields(self) -> Set[str]: ...
    @abstractmethod
    def get_DeeplyEffectiveNotPresentRequiredFields(self) -> Set[str]: ...
    @abstractmethod
    def get_RawValuesValidationIssues(self) -> list[AIcdSmartDataValidationIssue]: ...
    @abstractmethod
    def get_EffectiveValuesValidationIssues(self) -> list[AIcdSmartDataValidationIssue]: ...
    @abstractmethod
    def get_DeeplyRawValuesValidationIssues(self) -> list[AIcdSmartDataValidationIssue]: ...
    @abstractmethod
    def get_DeeplyEffectiveValuesValidationIssues(self) -> list[AIcdSmartDataValidationIssue]: ...
    @abstractmethod
    def toMapping_RawValues(self) -> Mapping[str, AIidVirtualFieldValue]: ...
    @abstractmethod
    def toMapping_EffectiveValues(self) -> Mapping[str, AIidVirtualFieldValue]: ...
    @abstractmethod
    def toString_RawValues(self, aProfile: AIiTextFormatProfile, aMode: AInTextOutputObjectReferenceMode) -> str: ...
    @abstractmethod
    def toString_EffectiveValues(self, aProfile: AIiTextFormatProfile, aMode: AInTextOutputObjectReferenceMode) -> str: ...
