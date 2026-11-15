"""Independent XML format binding, with QName-aware and hardened XML parsing.

This module deliberately does not depend on the legacy XML SmartDataObject
implementations or their generated concrete-type maps. Nested implementation
selection belongs to the purpose-scoped SDO binding registry.
"""
from __future__ import annotations

from collections.abc import Mapping, Sequence
from dataclasses import dataclass
from decimal import Decimal
from enum import Enum
from typing import Any, get_args, get_origin
import re
import xml.etree.ElementTree as ET

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.common.list.aii_bounded_list import AIiBoundedList
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_factory import AIsSmartDataObjectFactory

_XSI = 'http://www.w3.org/2001/XMLSchema-instance'
ET.register_namespace('xsi', _XSI)
_MAX_LENGTH = 8_000_000
_MAX_DEPTH = 128


def _qualified(namespace: str, local: str) -> str:
    """Return an ElementTree expanded QName, preserving the no-namespace case."""
    return f'{{{namespace}}}{local}' if namespace else local


@dataclass(frozen=True)
class _XmlField:
    """Resolved XML field location, independent of SDO value type."""
    namespace: str
    local_name: str
    is_attribute: bool


@dataclass(frozen=True)
class _XmlMetadata:
    """One XML provider contribution for a concrete SDO implementation."""
    namespace: str
    root_name: str
    fields: Mapping[str, _XmlField]

    def getFormatId(self) -> str:
        """Return the stable built-in format code."""
        return 'XML'

    def getProviderId(self) -> str:
        """Return the stable provider identifier."""
        return 'algites-xml'


def _metadata(binding: Any) -> _XmlMetadata:
    """Require the XML contribution attached by metadata discovery."""
    result = binding.getMetadata().getFormatBinding('XML', 'algites-xml')
    if not isinstance(result, _XmlMetadata):
        raise ValueError('Missing XML binding metadata for ' + repr(binding.getImplementationClass()))
    return result


def _scalar(value: Any) -> str:
    """Encode scalar values without Python-specific True/False lexical forms."""
    if isinstance(value, Enum):
        if not callable(getattr(value, 'code', None)):
            raise ValueError('XML enum must implement code(): ' + repr(type(value)))
        value = value.code()
    if isinstance(value, bool):
        return 'true' if value else 'false'
    if isinstance(value, (str, int, float, Decimal)):
        return str(value)
    raise TypeError('Unsupported XML scalar: ' + repr(type(value)))


def _encode(node: AIcSmartDataObject, element: ET.Element, registry: Any,
            active: set[int], depth: int) -> None:
    """Render a registered SDO, refusing cycles and excessive nesting."""
    if depth > _MAX_DEPTH or id(node) in active:
        raise ValueError('XML SDO cycle or nesting limit exceeded')
    active.add(id(node))
    try:
        binding = registry.getBinding(type(node))
        if binding is None:
            raise ValueError('Unregistered XML SDO implementation: ' + repr(type(node)))
        xml = _metadata(binding)
        raw = node.getRawValues()
        for field in binding.getMetadata().getFields():
            semantic = field.getSemanticFieldName()
            if semantic not in raw:
                continue
            location = xml.fields.get(semantic)
            namespace = location.namespace if location else ''
            local = location.local_name if location and location.local_name else field.getDataFieldName()
            qualified = _qualified(namespace, local)
            value = raw[semantic]
            if location and location.is_attribute:
                if value is not None:
                    element.set(qualified, _scalar(value))
                continue
            for item in value if isinstance(value, (list, tuple, AIiBoundedList)) else (value,):
                child = ET.SubElement(element, qualified)
                if item is None:
                    child.set(_qualified(_XSI, 'nil'), 'true')
                elif isinstance(item, AIcSmartDataObject):
                    _encode(item, child, registry, active, depth + 1)
                else:
                    child.text = _scalar(item)
    finally:
        active.remove(id(node))


def _read_contract(value_type: Any) -> type | None:
    """Find the declared SDO read contract, including typed collections."""
    if isinstance(value_type, type):
        return value_type if issubclass(value_type, AIiDataObject) else None
    return next((found for part in get_args(value_type)
                 if (found := _read_contract(part)) is not None), None)


def _repeated(value_type: Any) -> bool:
    """Determine whether the effective getter declares a collection."""
    origin = get_origin(value_type)
    return origin in (list, tuple, set, frozenset) or (
        isinstance(origin, type) and issubclass(origin, AIiBoundedList)) or (
        isinstance(value_type, type) and issubclass(value_type, AIiBoundedList))


def _decode(element: ET.Element, binding: Any, registry: Any, depth: int) -> dict[str, Any]:
    """Decode an XML element using only the selected binding and its field metadata."""
    if depth > _MAX_DEPTH:
        raise ValueError('XML SDO nesting limit exceeded')
    result: dict[str, Any] = {}
    xml = _metadata(binding)
    for field in binding.getMetadata().getFields():
        location = xml.fields.get(field.getSemanticFieldName())
        namespace = location.namespace if location else ''
        local = location.local_name if location and location.local_name else field.getDataFieldName()
        tag = _qualified(namespace, local)
        if location and location.is_attribute:
            if tag in element.attrib:
                result[field.getDataFieldName()] = element.attrib[tag]
            continue
        children = [child for child in element if child.tag == tag]
        if not children:
            continue
        requested_type = field.getEffectiveReturnType()
        contract = _read_contract(requested_type)
        values: list[Any] = []
        for child in children:
            if child.attrib.get(_qualified(_XSI, 'nil')) in ('true', '1'):
                values.append(None)
            elif contract is not None:
                nested_binding = registry.resolveBinding(contract, binding, field.getSemanticFieldName())
                values.append(_decode(child, nested_binding, registry, depth + 1))
            else:
                if len(child):
                    raise ValueError('Unexpected nested XML structure for scalar field: ' + local)
                values.append(child.text if child.text is not None else '')
        if _repeated(requested_type):
            result[field.getDataFieldName()] = values
        elif len(values) == 1:
            result[field.getDataFieldName()] = values[0]
        else:
            raise ValueError('Repeated XML element for scalar field: ' + local)
    return result


class _Marshaller:
    """Format-specific XML encoder."""

    def marshal(self, aObject: AIcSmartDataObject, aRegistry: Any) -> str:
        """Serialize a registered SDO to an XML document."""
        if not isinstance(aObject, AIcSmartDataObject):
            raise TypeError('Expected an independent SmartDataObject instance')
        binding = aRegistry.getBinding(type(aObject))
        if binding is None:
            raise ValueError('Root SDO implementation is not registered')
        schema = _metadata(binding)
        root = ET.Element(_qualified(schema.namespace, schema.root_name))
        _encode(aObject, root, aRegistry, set(), 0)
        return ET.tostring(root, encoding='unicode', short_empty_elements=True)


class _Unmarshaller:
    """Format-specific XML decoder with DTD/external-entity rejection."""

    def unmarshal(self, aText: str, aReadContract: type, aRegistry: Any) -> AIcSmartDataObject:
        """Decode a rooted XML document via registered implementation bindings."""
        if not isinstance(aText, str) or len(aText) > _MAX_LENGTH:
            raise ValueError('Invalid XML input or size limit exceeded')
        if re.search(r'<!\s*(?:DOCTYPE|ENTITY)\b', aText, re.IGNORECASE):
            raise ValueError('XML DTD and entity declarations are forbidden')
        root = ET.fromstring(aText)
        binding = aRegistry.resolveBinding(aReadContract)
        schema = _metadata(binding)
        expected = _qualified(schema.namespace, schema.root_name)
        if root.tag != expected:
            raise ValueError('Unexpected XML root QName: ' + str(root.tag))
        return AIsSmartDataObjectFactory.fromData(
            aReadContract, _decode(root, binding, aRegistry, 0), aRegistry)


class AIcXmlFormatBindingProvider:
    """XML Schema annotation provider, independently discoverable via entry points."""

    def getFormatId(self) -> str:
        """Return the stable XML format code."""
        return 'XML'

    def getProviderId(self) -> str:
        """Return the stable XML provider code."""
        return 'algites-xml'

    def resolveMetadata(self, aMetadata: Any) -> _XmlMetadata | None:
        """Merge schema and per-field XML annotations across the full class MRO."""
        schema = None
        fields: dict[str, _XmlField] = {}
        for owner in reversed(aMetadata.getImplementationClass().__mro__):
            declared_schema = owner.__dict__.get('__xml_schema__')
            if declared_schema is not None:
                schema = declared_schema
            declared_fields = owner.__dict__.get('__xml_schema_fields__', {})
            if not isinstance(declared_fields, Mapping):
                raise TypeError('__xml_schema_fields__ must be a mapping')
            for semantic, definition in declared_fields.items():
                fields[semantic] = _XmlField(
                    definition.namespace, definition.namespace_local_name,
                    definition.value_kind is AInXmlValueKind.ATTRIBUTE)
        if schema is None and not fields:
            return None
        return _XmlMetadata(
            schema.namespace if schema else '',
            (schema.namespace_local_name if schema and schema.namespace_local_name
             else aMetadata.getImplementationClass().__name__),
            dict(fields))

    def createMarshaller(self) -> _Marshaller:
        """Create a stateless XML marshaller."""
        return _Marshaller()

    def createUnmarshaller(self) -> _Unmarshaller:
        """Create a stateless XML unmarshaller."""
        return _Unmarshaller()
