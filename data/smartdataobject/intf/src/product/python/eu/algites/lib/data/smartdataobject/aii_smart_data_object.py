from __future__ import annotations
from abc import abstractmethod
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.common.validation.aii_validatable import AIiValidatable
from typing import Set, Mapping
from eu.algites.lib.data.smartdataobject.aiid_virtual_field_value import AIidVirtualFieldValue
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.text.format.aii_text_format_profile import AIiTextFormatProfile
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode

class AIiSmartDataObject(AIiDataObject, AIiValidatable):
    """Runtime contract implemented by mutable schema-generated data objects."""
    def isValid(self) -> bool:
        return self.is_DeeplyByEffectiveValuesValid()

    def getInvalidStateMessage(self, aLocale: str | None) -> str:
        if self.isValid(): return ''
        locCzech = str(aLocale or '').lower().startswith('cs')
        messages = {'REQUIRED_ABSENT': 'Povinná vlastnost není nastavena.',
                    'NULL_FORBIDDEN': 'Hodnota null není povolena.',
                    'WRONG_TYPE': 'Nesprávný datový typ.',
                    'COLLECTION_INVALID': 'Neplatná kardinalita kolekce.'}
        return '\n'.join((f'{issue.path}: ' if issue.path else '') +
                         (messages.get(issue.code, issue.message) if locCzech else issue.message)
                         for issue in self.get_DeeplyEffectiveValuesValidationIssues())

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
