"""Reusable mutable foundation for XML Schema generated SmartDataObjects."""

from __future__ import annotations

from collections.abc import Mapping
from decimal import Decimal
from enum import Enum
import xml.etree.ElementTree as ET

from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.xmldefs.aii_xml_smart_data_object import AIiXmlSmartDataObject


_XML_SCHEMA_INSTANCE = 'http://www.w3.org/2001/XMLSchema-instance'


def _qname(namespace: str, local_name: str) -> str:
    """Construct the expanded QName used by ElementTree without prefix dependence."""
    if not local_name:
        raise ValueError('Missing XML local name')
    return '{' + namespace + '}' + local_name if namespace else local_name


def _field_metadata(owner: type) -> dict[str, object]:
    """Combine inherited field QName declarations in the contract's MRO."""
    result: dict[str, object] = {}
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_data_object_fields__', {}))
    return result


def _nested_metadata(owner: type) -> dict[str, type]:
    """Combine implementation classes explicitly registered for nested fields."""
    result: dict[str, type] = {}
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_field_types__', {}))
    return result


def _repeated_metadata(owner: type) -> set[str]:
    """Read repetition declarations, including inherited generated contract fields."""
    result: set[str] = set()
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_repeated_fields__', ()))
    return result


def _scalar(text: str, target: type | None) -> object:
    """Decode supported XML Schema lexical values against a field's declared type."""
    if target is None or target is str or target in (list, tuple):
        return text
    if target is bool:
        if text in ('true', '1'):
            return True
        if text in ('false', '0'):
            return False
        raise ValueError('Invalid XML boolean lexical value: ' + repr(text))
    if target in (int, float, Decimal):
        return target(text.strip())
    if isinstance(target, type) and issubclass(target, Enum):
        for choice in target:
            if str(choice.value) == text:
                return choice
        raise ValueError('Unrecognized XML enumeration value ' + repr(text) + ' for ' + target.__name__)
    raise TypeError('XML field needs a supported scalar or explicit nested SmartDataObject type: ' + repr(target))


class AIcXmlSmartDataObject(AIcSmartDataObject, AIiXmlSmartDataObject):
    """Mutable XML Schema object with namespace-aware, order-tolerant parsing.

    Generated types can declare ``__xml_field_types__`` mapping field names to
    concrete nested SmartDataObject classes, plus ``__xml_repeated_fields__`` for
    fields with ``maxOccurs`` greater than one. Both mappings are inherited.
    Unknown attributes/elements, duplicate non-repeating QNames, unexpected
    mixed content and unsupported nested types are rejected rather than lost.
    """

    __xml_field_types__: dict[str, type] = {}
    __xml_repeated_fields__: tuple[str, ...] = ()

    @classmethod
    def from_xml(cls, aXml: str, aNestedTypes: Mapping[str, type] | None = None):
        """Create a concrete typed object from XML without enforcing child order.

        XML element and attribute identities use expanded QNames. Repeated
        siblings preserve document order within their own field. Typed nested
        objects must be declared on the class or supplied through ``aNestedTypes``.
        The standard SmartDataObject validation runs after population.

        :param aXml: XML document containing exactly one document element
        :param aNestedTypes: optional field-to-concrete-class overrides for this level
        :return: the populated generated SmartDataObject subtype
        :raises ValueError: for unknown, duplicated, or structurally invalid content
        :raises TypeError: if no safe conversion exists for a field
        """
        if not isinstance(aXml, str):
            raise TypeError('XML source must be text')
        if len(aXml) > 8_000_000:
            raise ValueError('XML input size limit exceeded')
        if '<!DOCTYPE' in aXml.upper() or '<!ENTITY' in aXml.upper():
            raise ValueError('DTD and XML entities are forbidden in SmartDataObject input')
        root = ET.fromstring(aXml)
        return cls._from_xml_element(root, aNestedTypes, 0)

    @classmethod
    def _from_xml_element(cls, element: ET.Element, aNestedTypes: Mapping[str, type] | None,
                          depth: int, aExpectedQName: str | None = None, aPath: str = ''):
        """Materialize one typed subtree; the limit prevents unbounded recursion."""
        if depth >= 128:
            raise ValueError('XML SmartDataObject nesting limit exceeded')
        definition = getattr(cls, '__xml_data_object__', None)
        expected = aExpectedQName
        if expected is None and definition is not None and definition.namespace_local_name:
            expected = _qname(definition.namespace, definition.namespace_local_name)
        if expected is not None and element.tag != expected:
            raise ValueError('Unexpected XML document QName: ' + repr(element.tag))
        if element.attrib.get(_qname(_XML_SCHEMA_INSTANCE, 'nil')) in ('true', '1'):
            raise ValueError('A SmartDataObject document root cannot be xsi:nil')
        if element.text and element.text.strip():
            raise ValueError('Mixed XML character content is not a DataObject field')

        descriptors = cls._fields()
        xml_metadata = _field_metadata(cls)
        nested_types = _nested_metadata(cls)
        if aNestedTypes:
            nested_types.update(aNestedTypes)
        repeated = _repeated_metadata(cls)
        by_element: dict[str, str] = {}
        by_attribute: dict[str, str] = {}
        for name, descriptor in descriptors.items():
            xml = xml_metadata.get(name)
            qname = _qname(xml.namespace if xml else '',
                           (xml.namespace_local_name if xml and xml.namespace_local_name else name))
            lookup = by_attribute if xml and xml.value_kind == AInXmlValueKind.ATTRIBUTE else by_element
            if qname in lookup:
                raise ValueError('Ambiguous XML QName declared by fields ' + lookup[qname] + ' and ' + name)
            lookup[qname] = name
            if descriptor.expected_type is list:
                repeated.add(name)

        values: dict[str, object] = {}
        for name, raw in element.attrib.items():
            if name == _qname(_XML_SCHEMA_INSTANCE, 'nil'):
                if raw in ('false', '0'):
                    continue
                raise ValueError('A SmartDataObject document root cannot be xsi:nil')
            field_name = by_attribute.get(name)
            if field_name is None:
                raise ValueError('Unknown XML attribute: ' + repr(name))
            descriptor = descriptors[field_name]
            if field_name in nested_types or field_name in repeated:
                raise ValueError('XML attribute cannot contain nested/repeated values: ' + field_name)
            values[field_name] = _scalar(raw, descriptor.expected_type)

        for child in element:
            field_name = by_element.get(child.tag)
            if field_name is None:
                raise ValueError('Unknown XML element: ' + repr(child.tag))
            descriptor = descriptors[field_name]
            field_path = aPath + '.' + field_name if aPath else field_name
            target = nested_types.get(field_path) or nested_types.get(field_name)
            nil = child.attrib.get(_qname(_XML_SCHEMA_INSTANCE, 'nil'))
            if nil is not None and nil not in ('true', '1', 'false', '0'):
                raise ValueError('Invalid xsi:nil value: ' + repr(nil))
            if nil in ('true', '1'):
                if not descriptor.allows_null:
                    raise ValueError('Non-nillable XML element: ' + field_name)
                if len(child) or child.text and child.text.strip() or len(child.attrib) != 1:
                    raise ValueError('Nil XML element cannot have a value: ' + field_name)
                value = None
            elif target is not None:
                if not isinstance(target, type) or not issubclass(target, AIcXmlSmartDataObject):
                    raise TypeError('Nested XML target must be an AIcXmlSmartDataObject class: ' + field_name)
                value = target._from_xml_element(child, aNestedTypes, depth + 1, child.tag, field_path)
            else:
                if len(child) or any(key != _qname(_XML_SCHEMA_INSTANCE, 'nil')
                                     for key in child.attrib):
                    raise ValueError('Unexpected nested XML structure for scalar field: ' + field_name)
                value = _scalar(child.text or '', descriptor.expected_type)
            if child.tail and child.tail.strip():
                raise ValueError('Mixed XML character content is not a DataObject field')
            if field_name in repeated:
                values.setdefault(field_name, []).append(value)
            elif field_name in values:
                raise ValueError('Duplicate non-repeating XML element: ' + field_name)
            else:
                values[field_name] = value
        for name, descriptor in descriptors.items():
            xml = xml_metadata.get(name)
            if xml is None or xml.min_occurs < 0 and xml.max_occurs < 0:
                continue
            value = values.get(name)
            count = len(value) if name in repeated and isinstance(value, list) else int(name in values)
            if xml.min_occurs >= 0 and count < xml.min_occurs:
                raise ValueError(f'XML field {name!r} has {count} occurrences; minimum is {xml.min_occurs}')
            if xml.max_occurs >= 0 and count > xml.max_occurs:
                raise ValueError(f'XML field {name!r} has {count} occurrences; maximum is {xml.max_occurs}')
        for name in repeated:
            if name not in values:
                values[name] = []
        instance = cls()
        for name, value in values.items():
            instance.set_RawField(name, value)
        issues = instance.get_RawValuesValidationIssues()
        if issues:
            from eu.algites.lib.data.smartdataobject.aix_smart_data_validation_exception import AIxSmartDataValidationException
            raise AIxSmartDataValidationException(issues)
        return instance
