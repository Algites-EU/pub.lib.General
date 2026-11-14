"""Reusable mutable foundation for XML Schema generated SmartDataObjects."""

from __future__ import annotations
from eu.algites.lib.data.smartdataobject.ain_sdo_field_value_loading_mode import AInSdoFieldValueLoadingMode

from collections.abc import Mapping
from decimal import Decimal
from enum import Enum
import xml.etree.ElementTree as ET

from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.dataobject.xmldefs.aic_xml_wildcard_fragment import AIcXmlWildcardFragment, xml_wildcard_class
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
from eu.algites.lib.data.smartdataobject.xmldefs.aii_xml_smart_data_object import AIiXmlSmartDataObject


_XML_SCHEMA_INSTANCE = 'http://www.w3.org/2001/XMLSchema-instance'
# Per-element parsing marker, never exposed as a public field value.
_XML_EMPTY = object()


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
        result.update(base.__dict__.get('__xml_schema_fields__', {}))
    return result


def _key_metadata(owner: type) -> dict[str, tuple[tuple[str, tuple[str, ...], bool], ...]]:
    """Merge inherited schema-generated XSD identity declarations per field."""
    result: dict[str, tuple[tuple[str, tuple[str, ...], bool], ...]] = {}
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_key_definitions__', {}))
        result.update(base.__dict__.get('__key_definitions__', {}))
    return result


def _scoped_key_metadata(owner: type) -> dict[str, tuple[tuple[str, str, tuple[str, ...], bool], ...]]:
    """Read occurrence-scoped indexes from the generated read-only contract.

    The enclosing field refers to a reusable nominal TYPE; its identity and
    class are never changed. Each selected child list acquires indexes only on
    the concrete object reached through this enclosing field.

    Args:
        owner: XML DataObject class with inherited read-only field metadata.

    Returns:
        Mapping of enclosing field to (selector, key name, paths, required).
    """
    result = {}
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__scoped_key_definitions__', {}))
    return result


def _apply_scoped_keys(nested: object, field_name: str,
                       declarations: tuple[tuple[str, str, tuple[str, ...], bool], ...]) -> None:
    """Apply local XSD indexes to the selected list of one nested object.

    Args:
        nested: Parsed instance of the original shared nominal TYPE.
        field_name: Enclosing field, for precise source diagnostics.
        declarations: Validated direct-list selectors and semantic key paths.

    Raises:
        ValueError: Selector missing/ambiguous, missing list, or duplicate keys.
        TypeError: Nested value is not a generated XML SmartDataObject.
    """
    if not isinstance(nested, AIcXmlSmartDataObject):
        raise TypeError('Scoped xs:key owner is not an XML DataObject: ' + field_name)
    fields = nested._fields()
    xml_fields = _field_metadata(type(nested))
    grouped = {}
    for selector, identifier, paths, required in declarations:
        if not selector or not identifier or not paths:
            raise ValueError('Incomplete scoped xs:key on ' + field_name)
        matches = [semantic for semantic, desc in fields.items()
                   if selector in (semantic, desc.field_name,
                       getattr(xml_fields.get(desc.field_name, xml_fields.get(semantic)),
                               'namespace_local_name', None))]
        if len(matches) != 1:
            raise ValueError('Unresolved scoped xs:key selector ' + selector
                             + ' on ' + field_name)
        grouped.setdefault(matches[0], []).append((identifier, paths, required))
    from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
    from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
    for target, entries in grouped.items():
        values = nested.get_RawField(target)
        if not isinstance(values, (list, AIiMultiMappedList)):
            raise ValueError('Scoped xs:key requires a repeated list: '
                             + field_name + '/' + target)
        definitions = [AIcKeyDefinition(
            name, lambda item, n=name, p=paths, r=required: _key_value(item, n, p, r),
            True, required) for name, paths, required in entries]
        nested.set_RawField(target, AIcMultiMappedList(definitions, values))


def _key_value(item: object, name: str, paths: tuple[str, ...], required: bool = True) -> object | None:
    """Extract a typed scalar or composite tuple using semantic SDO field metadata.

    A missing value is invalid for xs:key and grouped under None for xs:unique.
    Empty text is a present, indexable string and is not treated as missing.
    """
    values = []
    for path in paths:
        current = item
        for segment in path.split('.'):
            if current is None and not required:
                return None
            if not segment or current is None or not isinstance(current, AIcSmartDataObject):
                raise ValueError(f'Missing XML identity key {name}: {path}')
            fields = current._fields()
            xml_names = _field_metadata(type(current))
            candidates = [semantic for semantic, desc in fields.items()
                          if segment in (semantic, desc.field_name,
                              getattr(xml_names.get(desc.field_name, xml_names.get(semantic)),
                                      'namespace_local_name', None))]
            if len(candidates) != 1:
                raise ValueError(f'Unresolved xs:key {name}: {path}')
            current = current.get_EffectiveField(candidates[0])
        if current is None and not required:
            return None
        if current is None or isinstance(current, (list, dict, tuple)):
            raise ValueError(f'Missing or non-scalar XML identity key {name}: {path}')
        values.append(current)
    return values[0] if len(values) == 1 else tuple(values)


def _nested_metadata(owner: type) -> dict[str, type]:
    """Combine implementation classes explicitly registered for nested fields."""
    result: dict[str, type] = {}
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_field_types__', {}))
    return result


def _xml_scalar_types(owner: type) -> dict[str, type]:
    """Resolve generated enum types without requiring eager interface imports.

    Args:
        owner: A concrete XML SDO or immutable CHOICE variant with generated
            exact module/class references for nominal scalar types.

    Returns:
        The semantic field-to-enum mapping inherited from its contracts.

    Raises:
        ValueError: A generated symbol reference is malformed.
        TypeError: A declared scalar type is not an enumeration.
    """
    import importlib
    result: dict[str, type] = {}
    for base in reversed(owner.__mro__):
        for semantic, qualified in base.__dict__.get('__xml_scalar_type_imports__', {}).items():
            module, sep, name = qualified.partition(':')
            if not sep or not module or not name:
                raise ValueError('Invalid generated XML enum reference: ' + str(semantic))
            candidate = getattr(importlib.import_module(module), name)
            if not isinstance(candidate, type) or not issubclass(candidate, Enum):
                raise TypeError('Generated XML scalar reference is not an enum: ' + qualified)
            result[semantic] = candidate
    return result


def _repeated_metadata(owner: type) -> set[str]:
    """Read repetition declarations, including inherited generated contract fields."""
    result: set[str] = set()
    for base in reversed(owner.__mro__):
        result.update(base.__dict__.get('__xml_repeated_fields__', ()))
    return result


def _embedded_targets(owner: type, overrides: Mapping[str, type] | None = None) -> dict[str, type]:
    """Resolve generated concrete types by logical getter rather than XML wire name.

    The group has no serialized name; the caller's registration must therefore
    identify its semantic field (or its full logical path).
    """
    import importlib
    result = _nested_metadata(owner)
    for base in reversed(owner.__mro__):
        for name, qualified in base.__dict__.get('__xml_field_type_imports__', {}).items():
            module, separator, type_name = qualified.partition(':')
            if not separator:
                raise ValueError('Invalid generated XML nested implementation mapping: ' + name)
            result[name] = getattr(importlib.import_module(module), type_name)
    if overrides:
        result.update(overrides)
    fields = owner._fields()
    wire_names = {descriptor.field_name: semantic for semantic, descriptor in fields.items()
                  if not descriptor.embedded}
    return {wire_names.get(name, name): target for name, target in result.items()}


def _embedded_read_type(owner: type, semantic: str) -> type | None:
    """Find a generated read-only CHOICE/group without guessing naming profiles.

    Defs emits the import identity, not a concrete SDO implementation. The
    latter remains the sole responsibility of SDO Codegen or a caller-supplied
    nested-type registry.
    """
    import importlib
    for base in owner.__mro__:
        target = base.__dict__.get('__xml_embedded_read_types__', {}).get(semantic)
        if target:
            module, marker, symbol = target.partition(':')
            if not marker:
                raise ValueError('Invalid generated embedded reference: ' + semantic)
            return getattr(importlib.import_module(module), symbol)
    return None


def _choice_variant_parse(target: type, elements: list[ET.Element], start: int,
                          registrations: Mapping[str, type], depth: int,
                          path: str = '') -> tuple[int, object | None]:
    """Match and instantiate exactly one immutable CHOICE variant.

    Generated choice bases explicitly enumerate all variants; simply querying
    __subclasses__() would depend on arbitrary module import order. Named XML
    members are compared by QName. A nested, transparent sequence is parsed
    through its registered concrete SDO rather than inventing an XML wrapper.
    """
    from dataclasses import fields, is_dataclass
    import importlib
    if depth >= 128:
        raise ValueError('Embedded XML choice nesting limit exceeded')
    candidates = getattr(target, '__xml_choice_variant_imports__', ())
    if not isinstance(candidates, tuple) or not candidates:
        raise TypeError('Embedded CHOICE lacks generated variant declarations: ' + repr(target))
    matches = []
    missing_nested = set()
    for qualified in candidates:
        module, separator, type_name = qualified.partition(':')
        if not separator:
            raise ValueError('Invalid generated CHOICE variant reference: ' + qualified)
        variant = getattr(importlib.import_module(module), type_name)
        if not issubclass(variant, target) or not is_dataclass(variant):
            raise TypeError('Generated CHOICE variant must be an immutable dataclass: ' + qualified)
        names = getattr(variant, '__xml_choice_component_names__', ())
        components = fields(variant)
        if len(names) != len(components):
            raise ValueError('CHOICE component names do not match dataclass fields: ' + qualified)
        # The generator provides the possible first XML QNames for every
        # alternative, including transparent nested sequences. Reject an
        # unselected alternative *before* asking for a concrete nested SDO.
        # Such implementations are only required for the chosen branch.
        first_qnames = variant.__dict__.get('__xml_choice_first_qnames__', ())
        if first_qnames and (start >= len(elements) or
                             elements[start].tag not in first_qnames):
            continue
        wire = dict(variant.__dict__.get('__xml_data_object_fields__', {}))
        wire.update(variant.__dict__.get('__xml_schema_fields__', {}))
        enum_types = _xml_scalar_types(variant)
        cursor = start
        decoded = []
        for name, component in zip(names, components):
            annotation = wire.get(name)
            if annotation is not None:
                if cursor >= len(elements) or elements[cursor].tag != _qname(
                        annotation.namespace, annotation.namespace_local_name):
                    break
                part = elements[cursor]
                if len(part) or part.attrib:
                    raise ValueError('CHOICE scalar XML member has unexpected nested content: '
                                     + path + '/' + name)
                raw_type = variant.__annotations__.get(component.name, '')
                lexical_type = (enum_types.get(name) or
                                (raw_type if raw_type in (str, int, float, bool, Decimal)
                                 else {'str': str, 'int': int, 'float': float,
                                       'bool': bool, 'Decimal': Decimal}.get(str(raw_type))))
                if lexical_type is None:
                    # A nested named value can only be reconstructed with a
                    # concrete SDO binding (the read-only interface is abstract).
                    nested_type = registrations.get(path + '.' + name) or registrations.get(name)
                    if nested_type is None:
                        missing_nested.add(path + '.' + name)
                        break
                    decoded.append(nested_type._from_xml_element(
                        part, registrations, depth + 1, part.tag, path + '.' + name))
                else:
                    decoded.append(_scalar(part.text or '', lexical_type))
                cursor += 1
            else:
                nested_type = (registrations.get(path + '.' + name)
                               or registrations.get(name))
                if nested_type is None:
                    missing_nested.add(path + '.' + name)
                    break
                prefix = _embedded_group_prefix(nested_type, elements, cursor,
                                                registrations, depth + 1)
                if prefix <= 0:
                    break
                virtual = ET.Element('{urn:algites:internal}embedded-choice')
                virtual.extend(elements[cursor:cursor + prefix])
                decoded.append(nested_type._from_xml_element(
                    virtual, registrations, depth + 1, virtual.tag, path + '.' + name))
                cursor += prefix
        else:
            if cursor > start:
                matches.append((cursor - start, variant(*decoded)))
    if len(matches) > 1:
        raise ValueError('Ambiguous XML CHOICE alternatives at ' + (path or target.__name__))
    if matches:
        return matches[0]
    if start < len(elements) and missing_nested:
        # Do not silently treat a potentially selected alternative as absent.
        # Callers may supply a concrete SDO via aNestedTypes for nested groups.
        raise TypeError('XML CHOICE needs concrete nested implementation for '
                        + ', '.join(sorted(missing_nested)))
    return 0, None


def _embedded_group_prefix(target: type, elements: list[ET.Element], start: int,
                           registrations: Mapping[str, type], depth: int) -> int:
    """Recognize exactly one occurrence of a transparent SEQUENCE group.

    A match consumes a contiguous, schema-ordered run of XML children. An empty
    optional group does not consume a token. No wire wrapper is fabricated.
    Unsupported or ambiguous structures fail rather than silently reassigning
    XML children to the wrong logical group.
    """
    if depth >= 128:
        raise ValueError('XML embedded group nesting limit exceeded')
    if isinstance(target, type) and getattr(target, '__xml_choice_variant_imports__', None):
        return _choice_variant_parse(target, elements, start, registrations, depth)[0]
    if not isinstance(target, type) or not issubclass(target, AIcXmlSmartDataObject):
        raise TypeError('Embedded XML member needs a concrete XML DataObject type')
    definitions = target._fields()
    xml_fields = _field_metadata(target)
    nested = _embedded_targets(target, registrations)
    repeat = _repeated_metadata(target)
    cursor = start
    for semantic, descriptor in definitions.items():
        if descriptor.embedded:
            nested_type = nested.get(semantic) or descriptor.expected_type
            matches = 0
            while cursor < len(elements):
                count = _embedded_group_prefix(nested_type, elements, cursor, registrations, depth + 1)
                if count <= 0:
                    break
                cursor += count
                matches += 1
                if semantic not in repeat and descriptor.max_item_count <= 1:
                    break
                if descriptor.max_item_count >= 0 and matches >= descriptor.max_item_count:
                    break
            if descriptor.min_item_count > matches:
                return 0
            continue
        xml = xml_fields.get(descriptor.field_name, xml_fields.get(semantic))
        if xml is not None and xml.value_kind == AInXmlValueKind.ATTRIBUTE:
            raise ValueError('An embedded xs:group cannot contain XML attributes: ' + semantic)
        if xml is not None and xml.wildcard_namespace:
            raise NotImplementedError('Embedded wildcard groups need an explicit particle matcher')
        name = _qname(xml.namespace if xml else '',
                      xml.namespace_local_name if xml and xml.namespace_local_name
                      else descriptor.field_name)
        matches = 0
        while cursor < len(elements) and elements[cursor].tag == name:
            cursor += 1
            matches += 1
            if semantic not in repeat and descriptor.max_item_count <= 1:
                break
            if descriptor.max_item_count >= 0 and matches >= descriptor.max_item_count:
                break
        if descriptor.min_item_count > matches:
            return 0
    return cursor - start


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
        from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import _enum_code
        for choice in target:
            code = _enum_code(choice)
            lexical = ('true' if code else 'false') if isinstance(code, bool) else str(code)
            if lexical == text:
                return choice
        raise ValueError('Unrecognized XML enumeration code ' + repr(text) + ' for ' + target.__name__)
    raise TypeError('XML field needs a supported scalar or explicit nested SmartDataObject type: ' + repr(target))


def _wildcard_matches(qname: str, namespace_constraint: str, target_namespace: str) -> bool:
    """Apply an XSD xs:any namespace expression to an expanded element QName."""
    namespace = qname[1:].split('}', 1)[0] if qname.startswith('{') else ''
    for token in namespace_constraint.split():
        if (token == '##any' or token == '##local' and not namespace
                or token == '##targetNamespace' and namespace == target_namespace
                or token == '##other' and bool(namespace) and namespace != target_namespace
                or token == namespace):
            return True
    return False


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
    def _wildcard_contract(cls, field_name: str) -> type:
        """Resolve the inherited annotation of the generated xs:any getter."""
        from typing import get_args, get_origin, get_type_hints
        from eu.algites.lib.data.dataobject.xmldefs.aii_xml_data_object import AIiXmlDataObject
        from eu.algites.lib.data.dataobject.xmldefs.aii_xml_data_object_namespace import AIiXmlDataObjectNamespace
        for base in cls.__mro__:
            if field_name not in base.__dict__.get('__annotations__', {}):
                continue
            try:
                shape = get_type_hints(base).get(field_name)
            except (NameError, TypeError):
                continue
            if shape is None:
                continue
            args = get_args(shape)
            if args and get_origin(shape) in (tuple, list):
                shape = args[0]
            if isinstance(shape, type) and issubclass(shape, AIiXmlDataObject):
                return shape
        return AIiXmlDataObject

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
        definition = (getattr(cls, '__xml_schema__', None)
                      or getattr(cls, '__xml_data_object__', None))
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
        enum_types = _xml_scalar_types(cls)
        wire_to_semantic = {descriptor.field_name: name for name, descriptor in descriptors.items()}
        raw_xml_metadata = _field_metadata(cls)
        xml_metadata = {name: raw_xml_metadata.get(descriptor.field_name, raw_xml_metadata.get(name))
                        for name, descriptor in descriptors.items()}
        original_nested_types = _nested_metadata(cls)
        import importlib
        for base in reversed(cls.__mro__):
            for name, qualified in base.__dict__.get('__xml_field_type_imports__', {}).items():
                module, separator, type_name = qualified.partition(':')
                if not separator:
                    raise ValueError('Invalid generated XML nested implementation mapping')
                original_nested_types[name] = getattr(importlib.import_module(module), type_name)
        if aNestedTypes:
            original_nested_types.update(aNestedTypes)
        nested_types = {wire_to_semantic.get(name, name): target
                        for name, target in original_nested_types.items()}
        repeated = {wire_to_semantic.get(name, name) for name in _repeated_metadata(cls)}
        by_element: dict[str, str] = {}
        by_attribute: dict[str, str] = {}
        wildcards: list[tuple[str, object]] = []
        for name, descriptor in descriptors.items():
            if descriptor.embedded:
                if descriptor.field_name:
                    raise ValueError('Embedded XML field must not declare a serialized name: ' + name)
                continue
            xml = xml_metadata.get(name)
            if xml and bool(xml.wildcard_namespace):
                wildcards.append((name, xml))
                if descriptor.expected_type is list:
                    repeated.add(name)
                continue
            qname = _qname(xml.namespace if xml else '',
                           (xml.namespace_local_name if xml and xml.namespace_local_name
                            else descriptor.field_name))
            lookup = by_attribute if xml and xml.value_kind == AInXmlValueKind.ATTRIBUTE else by_element
            if qname in lookup:
                raise ValueError('Ambiguous XML QName declared by fields ' + lookup[qname] + ' and ' + name)
            lookup[qname] = name
            if descriptor.expected_type is list:
                repeated.add(name)

        # Unlike named members, an embedded group has no wire QName. Partition
        # its children by the ordered content model *before* the ordinary QName
        # dispatcher, retaining each occurrence as its own DataObject instance.
        embedded_values: dict[str, object] = {}
        embedded_indices: set[int] = set()
        if any(descriptor.embedded for descriptor in descriptors.values()):
            xml_children = list(element)
            cursor = 0
            for semantic, descriptor in descriptors.items():
                if not descriptor.embedded:
                    if semantic not in by_element.values():
                        continue
                    qname = next(name for name, field in by_element.items() if field == semantic)
                    while cursor < len(xml_children) and xml_children[cursor].tag == qname:
                        cursor += 1
                        if semantic not in repeated:
                            break
                    continue
                target = (nested_types.get(semantic) or descriptor.expected_type
                          or _embedded_read_type(cls, semantic))
                if not isinstance(target, type):
                    raise TypeError('Embedded XML member requires a nominal DataObject type: '
                                    + semantic)
                is_choice = bool(getattr(target, '__xml_choice_variant_imports__', None))
                if not is_choice and not issubclass(target, AIcXmlSmartDataObject):
                    raise TypeError('Embedded XML member requires concrete SmartDataObject type: '
                                    + semantic)
                occurrences = []
                while cursor < len(xml_children):
                    field_path = aPath + '.' + semantic if aPath else semantic
                    if is_choice:
                        count, nested_value = _choice_variant_parse(
                            target, xml_children, cursor, nested_types, depth + 1, field_path)
                    else:
                        count = _embedded_group_prefix(target, xml_children, cursor,
                                                       nested_types, depth + 1)
                    if count <= 0:
                        break
                    if not is_choice:
                        wrapper = ET.Element(element.tag)
                        wrapper.extend(xml_children[cursor:cursor + count])
                        nested_value = target._from_xml_element(
                            wrapper, aNestedTypes, depth + 1, element.tag, field_path)
                    occurrences.append(nested_value)
                    embedded_indices.update(range(cursor, cursor + count))
                    cursor += count
                    if semantic not in repeated and descriptor.max_item_count <= 1:
                        break
                    if descriptor.max_item_count >= 0 and len(occurrences) >= descriptor.max_item_count:
                        break
                if occurrences:
                    embedded_values[semantic] = (occurrences if semantic in repeated
                                                 else occurrences[0])
                elif semantic in repeated:
                    embedded_values[semantic] = []
        values: dict[str, object] = dict(embedded_values)
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

        for child_index, child in enumerate(element):
            if child_index in embedded_indices:
                continue
            field_name = by_element.get(child.tag)
            if field_name is None:
                matches = [(name, xml) for name, xml in wildcards
                    if _wildcard_matches(child.tag, xml.wildcard_namespace, xml.namespace)]
                if len(matches) > 1:
                    raise ValueError('Ambiguous XML wildcard match: ' + repr(child.tag))
                if not matches:
                    raise ValueError('Unknown XML element: ' + repr(child.tag))
                field_name = matches[0][0]
            descriptor = descriptors[field_name]
            field_path = aPath + '.' + field_name if aPath else field_name
            target = nested_types.get(field_path) or nested_types.get(field_name)
            wildcard = xml_metadata.get(field_name)
            if wildcard and bool(wildcard.wildcard_namespace):
                # Store an entire standalone XML subtree, including its attributes,
                # nested elements and namespace declarations, not merely text.
                fragment = ET.tostring(child, encoding='unicode')
                namespace_contract = cls._wildcard_contract(descriptor.field_name)
                value = xml_wildcard_class(namespace_contract)(fragment)
                if field_name in repeated:
                    values.setdefault(field_name, []).append(value)
                elif field_name in values:
                    raise ValueError('Duplicate non-repeating XML wildcard: ' + field_name)
                else:
                    values[field_name] = value
                continue
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
                # XML has an explicit empty element state, distinct from an
                # ordinary setXxxx("") call. Keep its raw lexical form in XML
                # output, but select an effective empty-default on reads.
                if (child.text is None or child.text == '') and descriptor.is_empty_allowed \
                        and field_name not in repeated and descriptor.expected_type is str:
                    value = _XML_EMPTY
                else:
                    value = _scalar(child.text or '', enum_types.get(field_name) or descriptor.expected_type)
            if child.tail and child.tail.strip():
                raise ValueError('Mixed XML character content is not a DataObject field')
            if field_name in repeated:
                values.setdefault(field_name, []).append(value)
            elif field_name in values:
                raise ValueError('Duplicate non-repeating XML element: ' + field_name)
            else:
                values[field_name] = value
        for name, descriptor in descriptors.items():
            if descriptor.min_item_count < 0 and descriptor.max_item_count < 0:
                continue
            value = values.get(name)
            count = len(value) if name in repeated and isinstance(value, list) else int(name in values)
            if descriptor.min_item_count >= 0 and count < descriptor.min_item_count:
                raise ValueError(f'XML field {name!r} has {count} occurrences; minimum is {descriptor.min_item_count}')
            if descriptor.max_item_count >= 0 and count > descriptor.max_item_count:
                raise ValueError(f'XML field {name!r} has {count} occurrences; maximum is {descriptor.max_item_count}')
        for name in repeated:
            if name not in values:
                values[name] = []
        indexed_fields = _key_metadata(cls)
        if indexed_fields:
            from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
            from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
            for external_name, keys in indexed_fields.items():
                semantic = wire_to_semantic.get(external_name, external_name)
                if semantic not in repeated:
                    raise ValueError(f'xs:key requires repeating XML field: {external_name}')
                # The third tuple slot is the required flag for xs:key versus
                # xs:unique. Legacy two-slot generated metadata means required.
                definitions = tuple(AIcKeyDefinition(key[0],
                    lambda item, n=key[0], fields=key[1], required=(key[2] if len(key) > 2 else True):
                        _key_value(item, n, fields, required),
                    True, key[2] if len(key) > 2 else True)
                    for key in keys)
                values[semantic] = AIcMultiMappedList(definitions, values[semantic])
        for outer_name, constraints in _scoped_key_metadata(cls).items():
            semantic = wire_to_semantic.get(outer_name, outer_name)
            nested_value = values.get(semantic)
            if nested_value is None:
                continue  # Optional container is absent, so there is no index scope.
            _apply_scoped_keys(nested_value, semantic, constraints)
        # A list type can be multi-map capable without owning any indexes: the
        # same named TYPE can also occur in a context with no xs:key at all.
        from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
        for name in repeated:
            descriptor = descriptors.get(name)
            if descriptor and name in values and \
                    isinstance(descriptor.expected_type, type) and \
                    issubclass(descriptor.expected_type, AIiMultiMappedList) and \
                    not isinstance(values[name], AIiMultiMappedList):
                values[name] = AIcMultiMappedList(values=values[name])
        instance = cls()
        for name, value in values.items():
            if value is _XML_EMPTY:
                instance.set_EmptyField(name)
            else:
                instance.set_RawField(name, value)
        issues = instance._getValidationIssues(AInSdoFieldValueLoadingMode.RAW_VALUES)
        if issues:
            from eu.algites.lib.data.smartdataobject.aix_smart_data_validation_exception import AIxSmartDataValidationException
            raise AIxSmartDataValidationException(issues)
        return instance
