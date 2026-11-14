"""Format-independent mutable SmartDataObject runtime, separate from deprecated runtime."""
from __future__ import annotations
from collections.abc import Mapping
from decimal import Decimal
from typing import Any
import re

from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.data.smartdataobject.ain_sdo_field_value_loading_mode import AInSdoFieldValueLoadingMode
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
from .ais_smart_data_object_format_binding_provider_registry import AIsSmartDataObjectFormatBindingProviderRegistry

_EMPTY = object()

class AIcSmartDataObject(AIiSmartDataObject):
    """Common SDO with semantic storage, presence and schema-neutral validation.

    Neither this implementation nor the metadata registry consults legacy SDO
    metadata. Instances hold no binding registry or format binding provider.
    """

    def __init__(self, aMapping: Mapping[str, Any] | None = None) -> None:
        self._sdo_values: dict[str, Any] = {}
        if aMapping is not None:
            for key, value in aMapping.items():
                self.set_RawField(key, value.getValueRepresentation()
                                  if hasattr(value, 'getValueRepresentation') else value)

    def getRawValues(self) -> dict[str, Any]:
        """Copy raw, explicitly present semantic field values."""
        return {name: '' if value is _EMPTY else value
                for name, value in self._sdo_values.items()}

    def isRawFieldPresent(self, aSemanticFieldName: str) -> bool:
        """Check explicit presence, which is distinct from an explicit null."""
        self._field(aSemanticFieldName)
        return aSemanticFieldName in self._sdo_values

    def getRawValue(self, aSemanticFieldName: str) -> Any:
        """Read an explicitly assigned raw field or return None."""
        return self.get_RawField(aSemanticFieldName)

    def _metadata(self):
        from .ais_smart_data_object_metadata_registry import AIsSmartDataObjectMetadataRegistry
        return AIsSmartDataObjectMetadataRegistry.forType(type(self))

    def _field(self, name: str):
        result = self._metadata().getField(name)
        if result is None:
            raise ValueError('Unknown semantic SDO field: ' + str(name))
        return result

    def set_RawField(self, name: str, value: Any) -> None:
        """Set one validated field without changing other raw presence bits."""
        field = self._field(name)
        definition = field.getDefinition()
        if definition.presence is AInFieldPresenceKind.FORBIDDEN:
            raise ValueError('Forbidden field: ' + name)
        if value is None and not definition.allows_null:
            raise ValueError('Null forbidden for ' + name)
        from typing import get_origin
        required = getattr(definition, 'expected_type', None) or field.getEffectiveReturnType()
        expected_class = get_origin(required) or required
        if (value is not None and isinstance(expected_class, type) and
                (not isinstance(value, expected_class) or
                 (expected_class is int and isinstance(value, bool)))):
            raise TypeError(f'Incompatible field value for {name}: {type(value)}; expected {expected_class}')
        self._validate(field, value)
        self._sdo_values[name] = value

    def get_RawField(self, name: str) -> Any:
        """Return the raw value without materializing defaults."""
        self._field(name)
        value = self._sdo_values.get(name)
        return '' if value is _EMPTY else value

    def get_EffectiveField(self, name: str) -> Any:
        """Return the value after applying schema default policy."""
        field = self._field(name)
        definition = field.getDefinition()
        present = name in self._sdo_values
        raw = self._sdo_values.get(name)
        if present and raw is not _EMPTY:
            return raw
        default = definition.default_value
        if default.defined and (not present and default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT or
                                present and raw is _EMPTY and default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY):
            return self._parse_default(field)
        return '' if present and raw is _EMPTY else None

    @staticmethod
    def _parse_default(field):
        definition = field.getDefinition()
        val = definition.default_value.value
        typ = getattr(definition, 'expected_type', None) or field.getEffectiveReturnType()
        if typ in (None, str, Any): return val
        if typ is bool: return val.lower() in ('true', '1')
        if typ in (int, float, Decimal): return typ(val)
        return val

    @staticmethod
    def _validate(field, value):
        if value is None: return
        definition = field.getDefinition()
        default = definition.default_value
        if default.defined and default.kind is AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT:
            if value != AIcSmartDataObject._parse_default(field):
                raise ValueError('FIXED constraint violated: ' + field.getSemanticFieldName())
        if isinstance(value, (list, tuple)):
            if definition.min_item_count >= 0 and len(value) < definition.min_item_count or (
                    definition.max_item_count >= 0 and len(value) > definition.max_item_count):
                raise ValueError('Collection cardinality constraint violated: ' + field.getSemanticFieldName())
        string = field.getStringConstraints()
        if isinstance(value, str) and string is not None:
            if (string.min_length >= 0 and len(value) < string.min_length or
                    string.max_length >= 0 and len(value) > string.max_length):
                raise ValueError('String length constraint violated: ' + field.getSemanticFieldName())
            if string.pattern and re.fullmatch(string.pattern, value) is None:
                raise ValueError('String pattern constraint violated: ' + field.getSemanticFieldName())
        numeric = field.getNumericConstraints()
        if isinstance(value, (int, float, Decimal)) and not isinstance(value, bool) and numeric is not None:
            number = Decimal(str(value))
            if not number.is_finite():
                raise ValueError('Non-finite numeric value')
            if (numeric.minimum and (number < Decimal(numeric.minimum) or
                    numeric.exclusive_minimum and number == Decimal(numeric.minimum))):
                raise ValueError('Numeric minimum constraint violated: ' + field.getSemanticFieldName())
            if (numeric.maximum and (number > Decimal(numeric.maximum) or
                    numeric.exclusive_maximum and number == Decimal(numeric.maximum))):
                raise ValueError('Numeric maximum constraint violated: ' + field.getSemanticFieldName())
            if numeric.multiple_of and number % Decimal(numeric.multiple_of) != 0:
                raise ValueError('Numeric multipleOf constraint violated: ' + field.getSemanticFieldName())
        lst = field.getListConstraints()
        if isinstance(value, (list, tuple)) and lst is not None:
            if (lst.min_items >= 0 and len(value) < lst.min_items or
                    lst.max_items >= 0 and len(value) > lst.max_items):
                raise ValueError('List cardinality constraint violated: ' + field.getSemanticFieldName())
            if lst.unique_items and any(value[i] == value[j]
                    for i in range(len(value)) for j in range(i + 1, len(value))):
                raise ValueError('List uniqueness constraint violated: ' + field.getSemanticFieldName())
        obj = field.getObjectConstraints()
        if isinstance(value, Mapping) and obj is not None:
            if (obj.min_properties >= 0 and len(value) < obj.min_properties or
                    obj.max_properties >= 0 and len(value) > obj.max_properties):
                raise ValueError('Object cardinality constraint violated: ' + field.getSemanticFieldName())

    def set_EmptyField(self, name: str) -> None:
        """Assign the explicit empty sentinel if the schema permits it."""
        if not self._field(name).getDefinition().is_empty_allowed:
            raise ValueError('EMPTY not permitted: ' + name)
        self._sdo_values[name] = _EMPTY

    def isEmpty_Field(self, name: str) -> bool:
        """Check explicit empty sentinel."""
        self._field(name)
        return self._sdo_values.get(name) is _EMPTY

    def isPresent_Field(self, name: str) -> bool:
        """Check explicit raw presence."""
        return self.isRawFieldPresent(name)

    def unset_Field(self, name: str) -> None:
        """Unset a field without mutating external retained collection instances."""
        self._field(name)
        self._sdo_values.pop(name, None)

    def get_OrCreateCollectionField(self, name: str, mapped: bool) -> Any:
        """Return the stable writable list, creating an indexed list when requested.

        Args:
            name: Semantic identifier of an existing collection field.
            mapped: Whether the generated getter requires AIiMultiMappedList.

        Returns:
            The current field value or a newly attached mutable collection.
            An explicitly assigned None remains None, not an empty collection.

        Raises:
            ValueError: When the field is unknown or forbidden by its definition.
            TypeError: When the new collection violates the declared field type.
        """
        self._field(name)
        if name not in self._sdo_values:
            if mapped:
                from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
                value = AIcMultiMappedList()
            else:
                value = []
            self.set_RawField(name, value)
        return self.get_RawField(name)

    def get_OrCreateNamedMappedCollectionField(self, name: str, factory: Any) -> Any:
        """Get or attach a generated nominal collection."""
        if name not in self._sdo_values:
            self.set_RawField(name, factory())
        return self.get_RawField(name)

    def _snapshot(self, effective: bool) -> dict[str, Any]:
        result: dict[str, Any] = {}
        for field in self._metadata().getFields():
            name = field.getSemanticFieldName()
            default = field.getDefinition().default_value
            if name in self._sdo_values or (effective and default.defined and
                    default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT):
                result[name] = self.get_EffectiveField(name) if effective else self.get_RawField(name)
        return result

    def _missing(self, effective: bool) -> set[str]:
        result = set()
        for field in self._metadata().getFields():
            name = field.getSemanticFieldName()
            definition = field.getDefinition()
            default = definition.default_value
            if (definition.presence is AInFieldPresenceKind.REQUIRED and name not in self._sdo_values and
                    not (effective and default.defined and
                         default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT)):
                result.add(name)
        return result

    def get_RawNotPresentRequiredFields(self) -> set[str]: return self._missing(False)
    def get_EffectiveNotPresentRequiredFields(self) -> set[str]: return self._missing(True)

    def _getValidationIssues(self, aMode: AInSdoFieldValueLoadingMode) -> list[AIcdSmartDataValidationIssue]:
        """Validate local fields using raw or effective semantics."""
        effective = aMode is AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES
        issues = [AIcdSmartDataValidationIssue(name, 'REQUIRED_ABSENT', 'Required field not present')
                  for name in sorted(self._missing(effective))]
        for field in self._metadata().getFields():
            name = field.getSemanticFieldName()
            if name not in self._sdo_values: continue
            value = self.get_EffectiveField(name) if effective else self.get_RawField(name)
            try:
                if value is None and not field.getDefinition().allows_null:
                    raise ValueError('Null forbidden')
                self._validate(field, value)
            except (ValueError, TypeError) as error:
                issues.append(AIcdSmartDataValidationIssue(name, 'FIELD_CONSTRAINT', str(error)))
        return issues

    def _deep_issues(self, effective: bool) -> list[AIcdSmartDataValidationIssue]:
        issues: list[AIcdSmartDataValidationIssue] = []
        seen: set[int] = set()
        def visit(value: Any, path: str) -> None:
            if value is None or id(value) in seen: return
            seen.add(id(value))
            if isinstance(value, AIcSmartDataObject):
                for issue in value._getValidationIssues(AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES if effective
                                                       else AInSdoFieldValueLoadingMode.RAW_VALUES):
                    issues.append(AIcdSmartDataValidationIssue(path + issue.path, issue.code, issue.message))
                for field, child in value._snapshot(effective).items():
                    visit(child, path + field + '.')
            elif isinstance(value, Mapping):
                for key, child in value.items(): visit(child, path + str(key) + '.')
            elif isinstance(value, (list, tuple)):
                for index, child in enumerate(value): visit(child, path + f'[{index}].')
        visit(self, '')
        return issues

    def get_DeeplyRawValuesValidationIssues(self): return self._deep_issues(False)
    def get_DeeplyEffectiveValuesValidationIssues(self): return self._deep_issues(True)
    def is_ByRawValuesValid(self): return not self._getValidationIssues(AInSdoFieldValueLoadingMode.RAW_VALUES)
    def is_ByEffectiveValuesValid(self): return not self._getValidationIssues(AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES)
    def is_DeeplyByRawValuesValid(self): return not self.get_DeeplyRawValuesValidationIssues()
    def is_DeeplyByEffectiveValuesValid(self): return not self.get_DeeplyEffectiveValuesValidationIssues()
    def get_DeeplyRawNotPresentRequiredFields(self):
        return {i.path for i in self.get_DeeplyRawValuesValidationIssues() if i.code == 'REQUIRED_ABSENT'}
    def get_DeeplyEffectiveNotPresentRequiredFields(self):
        return {i.path for i in self.get_DeeplyEffectiveValuesValidationIssues() if i.code == 'REQUIRED_ABSENT'}

    def _toMapping(self, aMode: AInSdoFieldValueLoadingMode):
        """Return a typed in-process snapshot preserving raw presence."""
        from dataclasses import dataclass
        @dataclass(frozen=True)
        class VirtualValue:
            value: Any
            def getValueRepresentation(self): return self.value
            def getValueClass(self): return type(self.value)
        return {name: VirtualValue(value) for name, value in self._snapshot(
            aMode is AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES).items()}

    def _render(self, aProfile, aMode, effective: bool) -> str:
        format_kind = aProfile.getFormatType()
        code = format_kind.code() if hasattr(format_kind, 'code') else format_kind.value
        providers = AIsSmartDataObjectFormatBindingProviderRegistry.getProviders(code)
        if len(providers) != 1:
            raise ValueError(f'Exactly one format binding provider required for {code}: {len(providers)} found')
        from .aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry
        registry = AIcSmartDataObjectBindingRegistry.builder().register(type(self)).build()
        return providers[0].createMarshaller().marshal(self, registry)

    def toString_RawValues(self, aProfile, aMode=None) -> str:
        return self._render(aProfile, aMode, False)

    def toString_EffectiveValues(self, aProfile, aMode=None) -> str:
        return self._render(aProfile, aMode, True)
