"""Smart Data Object runtime. Internal graph nodes are deliberately private."""
from __future__ import annotations
from eu.algites.lib.data.smartdataobject.ain_sdo_field_value_loading_mode import AInSdoFieldValueLoadingMode

from collections.abc import Mapping, MutableMapping, MutableSequence
from eu.algites.lib.common.validation.aii_validatable import AIiValidatable
from eu.algites.lib.common.enums.aii_enum_item import AIiEnumItem
from dataclasses import dataclass, field, replace
from decimal import Decimal
from enum import Enum
from json import dumps
from typing import Any
import xml.etree.ElementTree as ET

from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject
from eu.algites.lib.data.smartdataobject.aiid_virtual_field_value import AIidVirtualFieldValue
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.xmldefs.aic_xml_wildcard_fragment import AIcXmlWildcardFragment
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.common.object.props.labels.ais_field_label_utils import AIsFieldLabelUtils
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel
from eu.algites.lib.data.dataobject.aia_field_default import AIaFieldDefault
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
import re
from eu.algites.lib.data.dataobject.aii_field_default_value_resolver_factory import AIiFieldDefaultValueResolverFactory
from eu.algites.lib.factory.cpdiscovery.ais_category_provider_discovery import AIsCategoryProviderDiscovery
from eu.algites.lib.data.smartdataobject.aix_smart_data_validation_exception import AIxSmartDataValidationException
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles
from eu.algites.lib.text.format.ain_text_format_type import AInTextFormatType
from eu.algites.lib.text.format.ain_text_reference_encoding import AInTextReferenceEncoding
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode as Mode



def _canonical_getter_identity(getter_name: str) -> str:
    """Obtain the canonical Java/Python contract identity from a standard getter.

    Custom source naming must be explicitly resolved by a codegen naming
    catalog; the SmartData runtime never infers naming from field_name.
    """
    import re
    match = re.fullmatch(r'get([A-Z][A-Za-z0-9]*)', getter_name)
    if match is None:
        raise ValueError('Getter needs an explicitly resolved semantic identity: ' + getter_name)
    return match.group(1)


def _declared_fields(owner: type):
    """Collect direct method decorators and supported legacy field descriptors."""
    for declared in owner.__dict__.get('__data_object_fields__', ()):
        yield declared
    for getter_name, method in owner.__dict__.items():
        descriptor = getattr(method, '__data_object_field__', None)
        if descriptor is not None:
            if descriptor.getter_name != getter_name:
                raise ValueError('Mismatched annotated getter: ' + getter_name)
            yield descriptor


def _resolve_field_identity(owner: type, descriptor: Any, inherited: Any = None, identity_provider=None):
    """Bind semantic identity to the getter and inherit omitted wire names."""
    getter_name = descriptor.getter_name
    if not getter_name:
        # Compatibility for historical class-level descriptors. These lack a
        # bound getter, so their previously persisted key remains unchanged.
        # New contracts must use the method-bound AIaDataObjectField decorator.
        semantic_id = descriptor.semantic_id or descriptor.field_name
        return replace(descriptor, field_name=('' if descriptor.embedded else
                       descriptor.field_name or semantic_id), semantic_id=semantic_id)
    resolved_id = (identity_provider(owner, getter_name) if identity_provider is not None
                   else _canonical_getter_identity(getter_name))
    semantic_id = descriptor.semantic_id or resolved_id
    if descriptor.semantic_id and descriptor.semantic_id != resolved_id:
        raise ValueError('Getter and supplied semantic identity disagree: ' + getter_name)
    if inherited is not None and descriptor.field_name and descriptor.field_name != inherited.field_name:
        raise ValueError('Overridden getter changes fieldName: ' + semantic_id)
    if inherited is not None and descriptor.embedded != inherited.embedded:
        raise ValueError('Inherited field changes embedded serialization: ' + semantic_id)
    wire_name = ('' if descriptor.embedded else
                 descriptor.field_name or (inherited.field_name if inherited is not None else semantic_id))
    return replace(descriptor, field_name=wire_name, getter_name=getter_name, semantic_id=semantic_id)

def _lexical_default(lexical: str, value_type: type | None) -> Any:
    """Convert a schema-lexical default to its declared scalar Python type."""
    if value_type is bool:
        if lexical not in ('true', 'false', '1', '0'):
            raise ValueError('Invalid schema boolean default: ' + lexical)
        return lexical in ('true', '1')
    if value_type in (int, float, Decimal):
        return value_type(lexical)
    if isinstance(value_type, type) and issubclass(value_type, Enum):
        for member in value_type:
            if member.name == lexical or str(_enum_code(member)) == lexical:
                return member
        raise ValueError('Unknown schema enum default: ' + lexical)
    return lexical


def _resolve_field_descriptor_default(owner: type, descriptor: Any) -> Any:
    """Attach only an explicit, typed CPDiscovery default resolver to this field.

    Annotation-backed defaults and an inline schema descriptor default are mutually
    exclusive, just as explicit raw presence and an effective default are distinct.
    """
    getter_name = descriptor.getter_name or ('get' + descriptor.name)
    getter = owner.__dict__.get(getter_name)
    annotation = getattr(getter, '__field_default__', None)
    if annotation is None:
        annotation = owner.__dict__.get('__field_defaults__', {}).get(descriptor.name)
    for kind in ('string', 'numeric', 'list', 'object'):
        constraint = getattr(getter, '__data_object_field_' + kind + '_constraints__', None)
        if constraint is not None:
            descriptor = replace(descriptor, **{kind + '_constraints': constraint})
    field_default = descriptor.default_value
    if field_default.defined:
        if descriptor.default_value_resolver is not None or annotation is not None:
            raise ValueError('Conflicting inline and annotation defaults for ' + descriptor.name)
        if field_default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT:
            return replace(descriptor, default_value_resolver=lambda:
                           _lexical_default(field_default.value, descriptor.expected_type))
        return descriptor
    if annotation is None:
        return descriptor
    if not isinstance(annotation, AIaFieldDefault):
        raise TypeError('Invalid field default annotation for ' + descriptor.name)
    if descriptor.default_value_resolver is not None:
        raise ValueError('Conflicting inline and provider field defaults for ' + descriptor.name)
    resolver = AIsCategoryProviderDiscovery.get_instance(
        annotation.default_value_resolver, AIiFieldDefaultValueResolverFactory, 'default')
    return replace(descriptor, default_value_resolver=lambda: resolver.resolve_default(owner, getter_name))


def _data_object_fields(cls: type, identity_provider=None) -> dict[str, Any]:
    """Resolve inherited domain fields while excluding configured document metadata.

    Metadata names are sourced from each declaring contract's DataObject annotation,
    not inferred from a hard-coded JSON or YAML key. An empty configured name does
    not reserve a field. The filter runs before semantic inheritance merging.
    """
    # Cache on the *concrete class*, never on a base (which would leak its
    # unmerged descriptors into derived contracts). Unlike a global strong-key
    # cache this does not retain dynamically generated classes after unloading.
    # An explicit identity provider can change naming rules, so bypass cache.
    if identity_provider is None:
        cached = cls.__dict__.get('__sdo_runtime_effective_fields_cache__')
        if cached is not None:
            return cached
    descriptors: dict[str, Any] = {}
    owners: dict[str, type] = {}
    for base in reversed(cls.__mro__):
        annotation = (base.__dict__.get('__json_schema__')
                      or base.__dict__.get('__json_data_object__')
                      or base.__dict__.get('__yaml_data_object__'))
        reserved = {
            name for name in (
                getattr(annotation, 'data_source_schema_url_field', ''),
                getattr(annotation, 'data_source_schema_identifier_field', ''),
                getattr(annotation, 'data_source_schema_version_field', ''),
            ) if isinstance(name, str) and name
        }
        if len(reserved) != sum(bool(name) for name in (
                getattr(annotation, 'data_source_schema_url_field', ''),
                getattr(annotation, 'data_source_schema_identifier_field', ''),
                getattr(annotation, 'data_source_schema_version_field', ''))):
            raise ValueError('Duplicate configured document metadata field in ' + base.__name__)
        for declared in _declared_fields(base):
            if declared.field_name and declared.field_name in reserved:
                continue
            identity = (declared.semantic_id or declared.field_name if not declared.getter_name
                    else declared.semantic_id or (identity_provider(base, declared.getter_name)
                         if identity_provider is not None else _canonical_getter_identity(declared.getter_name)))
            previous = descriptors.get(identity)
            if previous is not None and owners[identity] is base:
                raise ValueError('Two getters declare the same semantic field: ' + identity)
            if previous is not None and previous.getter_name and declared.getter_name \
                    and previous.getter_name != declared.getter_name:
                raise ValueError('Inherited field changes its source getter: ' + identity)
            descriptor = _resolve_field_identity(base, declared, previous, identity_provider)
            descriptor = _resolve_field_descriptor_default(base, descriptor)
            if previous is not None:
                # Format-specific and covariant getters need not redeclare
                # constraint decorators. Keep the already-resolved annotations.
                inherited_constraints = {
                    name: getattr(previous, name)
                    for name in ('string_constraints', 'numeric_constraints',
                                 'list_constraints', 'object_constraints')
                    if getattr(descriptor, name) is None
                }
                if inherited_constraints:
                    descriptor = replace(descriptor, **inherited_constraints)
            if previous is not None and previous != descriptor:
                owner = owners[identity]
                if not issubclass(base, owner):
                    if previous != descriptor:
                        raise ValueError('Conflicting unrelated inherited semantic field: ' + identity
                            + '; first owner=' + owner.__module__ + '.' + owner.__qualname__
                            + ', second owner=' + base.__module__ + '.' + base.__qualname__
                            + '; first descriptor=' + repr(previous)
                            + '; second descriptor=' + repr(descriptor))
                else:
                    _validate_field_restriction(previous, descriptor)
            descriptors[identity] = descriptor
            owners[identity] = base
    wire_owners: dict[str, str] = {}
    for identity, descriptor in descriptors.items():
        if descriptor.embedded:
            continue
        previous = wire_owners.setdefault(descriptor.field_name, identity)
        if previous != identity:
            raise ValueError('Duplicate fieldName for unrelated getters: '
                             + descriptor.field_name + ' (' + previous + ', ' + identity + ')')
    if identity_provider is None:
        setattr(cls, '__sdo_runtime_effective_fields_cache__', descriptors)
    return descriptors

def _validate_field_restriction(parent: Any, child: Any) -> None:
    """A derived field may narrow, but cannot widen, inherited presence or nullability."""
    if parent.presence == AInFieldPresenceKind.REQUIRED and child.presence != AInFieldPresenceKind.REQUIRED or \
       parent.presence == AInFieldPresenceKind.FORBIDDEN and child.presence != AInFieldPresenceKind.FORBIDDEN:
        raise ValueError('Invalid inherited presence restriction: ' + child.name)
    if not parent.allows_null and child.allows_null:
        raise ValueError('Restriction cannot introduce null: ' + child.name)


def _enum_code(value: Enum) -> Any:
    """Select the external code of an enum, retaining legacy enum values.

    Generated enums implement the structural AIiEnumItem protocol, allowing
    their member name and stored Python value to differ from the wire code.
    Unrelated handwritten Enum implementations retain their original value.

    Args:
        value: Enum member whose external value is requested.

    Returns:
        The schema-defined string code when available, otherwise its raw value.
    """
    return value.code() if isinstance(value, AIiEnumItem) else value.value


def _xml_lexical(value: Any) -> str:
    """Render XSD-compatible booleans and enumerated wire values.

    In particular, Python's ``str(True)`` and default ``str(Enum)`` are not
    valid XML Schema lexical representations of their underlying values.
    """
    if isinstance(value, Enum):
        value = _enum_code(value)
    if isinstance(value, bool):
        return 'true' if value else 'false'
    return str(value)


def _json_text(value: Any) -> str:
    """JSON encoder preserving Decimal/BigInteger numeric lexemes exactly."""
    if isinstance(value, Decimal):
        if not value.is_finite():
            raise ValueError('JSON cannot represent NaN or Infinity')
        return str(value)
    if isinstance(value, dict):
        return '{' + ','.join(dumps(str(k), ensure_ascii=False) + ':' + _json_text(v)
                              for k, v in value.items()) + '}'
    if isinstance(value, (tuple, list)):
        return '[' + ','.join(_json_text(v) for v in value) + ']'
    return dumps(value, ensure_ascii=False, separators=(',', ':'), allow_nan=False)


def _schema_wire_metadata(owner: type) -> dict[str, str | int]:
    """Resolve only explicitly enabled data-document schema metadata on this contract.

    Empty target field names are intentionally omitted, and absent values are
    never fabricated from an unrelated URL or a class name.
    """
    schema = next((base.__dict__.get('__json_schema__')
                   or base.__dict__.get('__json_data_object__')
                   or base.__dict__.get('__yaml_data_object__') for base in owner.__mro__
                   if any(key in base.__dict__ for key in ('__json_schema__', '__json_data_object__', '__yaml_data_object__'))), None)
    if schema is None:
        return {}
    values = ((schema.data_source_schema_url_field, schema.schema_url),
              (schema.data_source_schema_identifier_field, schema.schema_id),
              (schema.data_source_schema_version_field,
               schema.schema_version if schema.schema_version >= 0 else None))
    result = {}
    for key, value in values:
        if key and value is not None and value != '':
            if key in result:
                raise ValueError('Repeated data-document schema metadata field: ' + key)
            result[key] = value
    return result


@dataclass(eq=False)
class _Node:
    kind: str
    obj: Any
    value: Any = None
    fields: list[_Field] = field(default_factory=list)
    children: list[_Node] = field(default_factory=list)
    entries: list[tuple[_Node, _Node]] = field(default_factory=list)
    # Serialized members include declared fields and integrated map values in
    # their original shared-storage insertion order. The validation field list
    # remains separate so additional properties are not treated as declared fields.
    wire_members: list[tuple[str, _Field | _Node]] = field(default_factory=list)


@dataclass(eq=False)
class _Field:
    descriptor: Any
    xml: Any
    present: bool
    raw: _Node
    empty: bool = False
    effective: _Node | None = None
    resolved: bool = False


class _Snapshot:
    def __init__(self, root: AIcSmartDataObject):
        self.registry: dict[int, _Node] = {}
        self.nil = _Node('null', None)
        self.root = self.build(root)

    def build(self, obj: Any) -> _Node:
        if obj is None:
            return self.nil
        if obj is EMPTY_VALUE:
            obj = '' 
        key = id(obj)
        if key in self.registry:
            assert self.registry[key].obj is obj
            return self.registry[key]
        if len(self.registry) >= 50000:
            raise ValueError('Snapshot too large')
        kind = ('scalar' if isinstance(obj, AIcXmlWildcardFragment) else
                'smart' if isinstance(obj, AIiDataObject) else
                'map' if isinstance(obj, Mapping) else
                'list' if isinstance(obj, (tuple, list, set, MutableSequence)) else
                'scalar' if isinstance(obj, (str, int, float, bool, Decimal, Enum)) else 'opaque')
        node = _Node(kind, obj, obj.xml_fragment if isinstance(obj, AIcXmlWildcardFragment)
                     else str(obj) if kind == 'opaque' else obj)
        self.registry[key] = node
        if kind == 'smart':
            xml_fields = {}
            for owner in reversed(type(obj).__mro__):
                declared_xml = owner.__dict__.get('__xml_data_object_fields__', {})
                if not isinstance(declared_xml, Mapping):
                    raise TypeError('__xml_data_object_fields__ must map field names to XML metadata')
                xml_fields.update(declared_xml)
                new_xml = owner.__dict__.get('__xml_schema_fields__', {})
                if not isinstance(new_xml, Mapping):
                    raise TypeError('__xml_schema_fields__ must map field names to XML metadata')
                xml_fields.update(new_xml)
            for desc in _data_object_fields(type(obj)).values():
                present = desc.name in obj._values if isinstance(obj, AIcSmartDataObject) else True
                explicit_empty = present and isinstance(obj, AIcSmartDataObject) \
                    and obj._values[desc.name] is EMPTY_VALUE
                if present:
                    if isinstance(obj, AIcSmartDataObject):
                        value = obj._values[desc.name]
                        if value is EMPTY_VALUE:
                            value = ''  # explicit empty serializes as empty lexical value
                    else:
                        getter = desc.getter_name or 'get' + desc.name
                        value = getattr(obj, getter)()
                else:
                    value = None
                node.fields.append(_Field(desc, xml_fields.get(desc.field_name, xml_fields.get(desc.name)), present,
                                          self.build(value) if present else self.nil,
                                          empty=explicit_empty))
            by_semantic_name = {member.descriptor.name: member for member in node.fields}
            seen: set[str] = set()
            if isinstance(obj, AIcSmartDataObject):
                for key, value in obj._values.items():
                    if isinstance(key, _AdditionalPropertyKey):
                        node.wire_members.append((key.name, self.build(value)))
                    elif key in by_semantic_name:
                        member = by_semantic_name[key]
                        seen.add(key)
                        node.wire_members.append((member.descriptor.field_name, member))
            for member in node.fields:
                if member.descriptor.name not in seen:
                    node.wire_members.append((member.descriptor.field_name, member))
        elif kind == 'map':
            node.entries = [(self.build(k), self.build(v)) for k, v in obj.items()]
        elif kind == 'list':
            node.children = [self.build(v) for v in obj]
        return node

    def value(self, fld: _Field, effective: bool) -> _Node:
        if not effective or fld.present and not fld.empty:
            return fld.raw
        if not fld.resolved:
            fld.resolved = True
            default = fld.descriptor.default_value
            if fld.empty and default.defined and default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY:
                fld.effective = self.build(_lexical_default(default.value, fld.descriptor.expected_type))
            elif not fld.present and fld.descriptor.default_value_resolver:
                fld.effective = self.build(fld.descriptor.default_value_resolver())
            else:
                fld.effective = self.nil
        return fld.effective

    def issues(self, effective: bool, deep: bool) -> list[AIcdSmartDataValidationIssue]:
        issues: list[AIcdSmartDataValidationIssue] = []
        visited: set[_Node] = set()
        def traverse(node: _Node, path: str, owner_type: type | None = None,
                     field_name: str | None = None, field_description: str | None = None) -> None:
            if node in visited:
                return
            visited.add(node)
            if node.kind == 'smart':
                for f in node.fields:
                    name = f'{path}.{f.descriptor.name}' if path else f.descriptor.name
                    if f.present and f.descriptor.presence == AInFieldPresenceKind.FORBIDDEN:
                        issues.append(AIcdSmartDataValidationIssue(name, 'FORBIDDEN_PRESENT', 'Forbidden property is present', type(node.obj), f.descriptor.name, AIsFieldLabelUtils.findLabel(type(node.obj), f.descriptor.name[:1].lower() + f.descriptor.name[1:],
                        aRoleCode=AIaFieldLabel.FIELD_DESCRIPTION)))
                        continue
                    if not f.present and f.descriptor.presence == AInFieldPresenceKind.REQUIRED and (not effective or not f.descriptor.default_value_resolver):
                        issues.append(AIcdSmartDataValidationIssue(name, 'REQUIRED_ABSENT', 'Required property is absent', type(node.obj), f.descriptor.name, AIsFieldLabelUtils.findLabel(type(node.obj), f.descriptor.name[:1].lower() + f.descriptor.name[1:],
                        aRoleCode=AIaFieldLabel.FIELD_DESCRIPTION)))
                        continue
                    if not f.present and (not effective or not f.descriptor.default_value_resolver):
                        continue
                    value = self.value(f, effective)
                    if value.kind == 'null' and not f.descriptor.allows_null:
                        issues.append(AIcdSmartDataValidationIssue(name, 'NULL_FORBIDDEN', 'Null is not allowed', type(node.obj), f.descriptor.name, AIsFieldLabelUtils.findLabel(type(node.obj), f.descriptor.name[:1].lower() + f.descriptor.name[1:],
                        aRoleCode=AIaFieldLabel.FIELD_DESCRIPTION)))
                    elif value.kind != 'null' and f.descriptor.expected_type and not isinstance(value.obj, f.descriptor.expected_type):
                        issues.append(AIcdSmartDataValidationIssue(name, 'WRONG_TYPE', 'Type is incompatible', type(node.obj), f.descriptor.name, AIsFieldLabelUtils.findLabel(type(node.obj), f.descriptor.name[:1].lower() + f.descriptor.name[1:],
                        aRoleCode=AIaFieldLabel.FIELD_DESCRIPTION)))
                    if deep:
                        traverse(value, name, type(node.obj), f.descriptor.name, AIsFieldLabelUtils.findLabel(type(node.obj), f.descriptor.name[:1].lower() + f.descriptor.name[1:],
                        aRoleCode=AIaFieldLabel.FIELD_DESCRIPTION))
            elif deep and node.kind == 'list':
                if isinstance(node.obj, AIiValidatable) and not node.obj.isValid():
                    for nested in node.obj.getValidationIssues():
                        issues.append(AIcdSmartDataValidationIssue(path, 'COLLECTION_INVALID',
                                                                  nested.getMessage(None), owner_type,
                                                                  field_name, field_description, nested))
                for i, v in enumerate(node.children):
                    traverse(v, f'{path}[{i}]', owner_type, field_name, field_description)
            elif deep and node.kind == 'map':
                for k, v in node.entries:
                    traverse(v, f'{path}[{k.value}]', owner_type, field_name, field_description)
        traverse(self.root, '')
        return issues


class _Renderer:
    def __init__(self, snapshot: _Snapshot, profile: Any, mode: Mode, effective: bool):
        if not profile.is_reference_mode_supported(mode):
            raise ValueError('Unsupported reference mode')
        self.snapshot, self.profile, self.mode, self.effective = snapshot, profile, mode, effective
        self.count: dict[_Node, int] = {}
        self.cycle: set[_Node] = set()
        self.expanded: set[_Node] = set()
        self.active: set[_Node] = set()
        self.ids: dict[_Node, int] = {}
        self.approximate_sizes: dict[_Node, int] = {}
        self.scan(snapshot.root, set())

    def kids(self, node: _Node) -> list[_Node]:
        if node.kind == 'smart':
            return [self.snapshot.value(member, self.effective)
                    if isinstance(member, _Field) else member
                    for _, member in node.wire_members
                    if not isinstance(member, _Field) or member.present or
                       self.effective and member.descriptor.default_value_resolver]
        if node.kind == 'list':
            return node.children
        if node.kind == 'map':
            return [x for pair in node.entries for x in pair]
        return []

    def scan(self, node: _Node, active: set[_Node]) -> None:
        self.count[node] = self.count.get(node, 0) + 1
        if node in active:
            self.cycle.add(node)
            return
        if node in self.expanded:
            return
        self.expanded.add(node)
        active.add(node)
        for child in self.kids(node):
            self.scan(child, active)
        active.remove(node)

    def eligible(self, node: _Node) -> bool:
        enc = self.profile.reference_encoding
        return node.kind != 'null' and enc not in (AInTextReferenceEncoding.NONE, AInTextReferenceEncoding.XML_SCHEMA) and \
            (enc != AInTextReferenceEncoding.JSON_DOTNET or node.kind in ('smart', 'list', 'map'))

    def approximate_size(self, node: _Node, path: set[_Node] | None = None) -> int:
        if node in self.approximate_sizes:
            return self.approximate_sizes[node]
        if path is None:
            path = set()
        if node in path:
            return 16
        path.add(node)
        if node.kind == 'null':
            size = 4
        elif node.kind in ('scalar', 'opaque'):
            val = _enum_code(node.value) if isinstance(node.value, Enum) else node.value
            size = len(dumps(val, ensure_ascii=False, default=str))
        elif node.kind == 'smart':
            size = 2
            for name, member in node.wire_members:
                if isinstance(member, _Field) and not member.present and not self.profile.diagnostic and \
                        (not self.effective or not member.descriptor.default_value_resolver):
                    continue
                size += len(dumps(name)) + 2
                if not isinstance(member, _Field):
                    size += self.approximate_size(member, path)
                elif member.present or self.effective and member.descriptor.default_value_resolver:
                    size += self.approximate_size(self.snapshot.value(member, self.effective), path)
                if self.profile.diagnostic:
                    size += 20
        else:
            size = 2 + sum(self.approximate_size(c, path) + 2 for c in self.kids(node))
        path.remove(node)
        self.approximate_sizes[node] = size
        return size

    def size_optimized(self, node: _Node) -> bool:
        if node in self.cycle:
            return True
        times = self.count.get(node, 0)
        if times <= 1:
            return False
        estimated = self.approximate_size(node)
        ref_cost = 5 if self.profile.format_type == AInTextFormatType.YAML else \
            23 if self.profile.format_type == AInTextFormatType.XML else 18
        id_cost = 5 if self.profile.format_type == AInTextFormatType.YAML else \
            12 if self.profile.format_type == AInTextFormatType.XML else 20
        return (times - 1) * (estimated - ref_cost) > id_cost

    def numbered(self, node: _Node) -> bool:
        if not self.eligible(node) or self.mode == Mode.NO_REFERENCES:
            return False
        if self.mode == Mode.ALL_OBJECTS:
            return True
        if self.mode == Mode.CYCLIC_REFERENCES_ONLY:
            return node in self.cycle
        if self.mode == Mode.REPEATED_OBJECTS:
            return self.count.get(node, 0) > 1
        return self.size_optimized(node)

    def ref(self, node: _Node) -> int | None:
        if node in self.active:
            if not self.eligible(node) or self.mode == Mode.NO_REFERENCES:
                raise ValueError('Unsupported cyclic graph')
            return self.ids.setdefault(node, len(self.ids) + 1)
        if self.mode != Mode.CYCLIC_REFERENCES_ONLY and self.numbered(node) and node in self.ids:
            return self.ids[node]
        if self.numbered(node):
            self.ids.setdefault(node, len(self.ids) + 1)
        return None

    def as_data(self, node: _Node) -> Any:
        ref = self.ref(node)
        if ref is not None:
            return {('dNrRef' if self.profile.diagnostic else '$ref'): str(ref)}
        nr = self.ids.get(node) if self.numbered(node) else None
        self.active.add(node)
        try:
            if node.kind == 'null':
                data = None
            elif node.kind in ('scalar', 'opaque'):
                if node.kind == 'opaque' and not self.profile.diagnostic:
                    raise TypeError('Unsupported opaque canonical text value')
                data = _enum_code(node.value) if isinstance(node.value, Enum) else node.value
            elif node.kind == 'list':
                data = [self.as_data(child) for child in node.children]
            elif node.kind == 'map':
                if any(not isinstance(k.value, str) for k, _ in node.entries):
                    raise ValueError('JSON/YAML map keys must be strings in this profile')
                data = {k.value: self.as_data(v) for k, v in node.entries}
            else:
                data = {} if self.profile.diagnostic else _schema_wire_metadata(type(node.obj))
                for name, member in node.wire_members:
                    if isinstance(member, _Field):
                        if name in data:
                            if member.present:
                                actual = self.as_data(self.snapshot.value(member, False))
                                if actual != data[name]:
                                    raise ValueError('Schema metadata conflicts with a data property: '
                                                     + member.descriptor.name)
                            continue
                        if not member.present and not self.profile.diagnostic and \
                                (not self.effective or not member.descriptor.default_value_resolver):
                            continue
                        raw = (self.as_data(self.snapshot.value(member, self.effective)) if
                               member.present or self.effective and member.descriptor.default_value_resolver else None)
                        data[name] = ({'present': member.present,
                                       **({'value': raw} if member.present else {})}
                                      if self.profile.diagnostic else raw)
                    else:
                        if name in data:
                            raise ValueError('Additional property collides with schema metadata: ' + name)
                        raw = self.as_data(member)
                        data[name] = {'present': True, 'value': raw} if self.profile.diagnostic else raw
                if self.profile.diagnostic:
                    meta = {'type': type(node.obj).__name__}
                    if nr is not None:
                        meta['dNr'] = nr
                    return {'$meta': meta, '$fields': data}
            if nr is None:
                return data
            if self.profile.diagnostic:
                return {'$meta': {'dNr': nr}, '$value': data}
            if isinstance(data, dict):
                if any(k in data for k in ('$id', '$ref', '$values')):
                    raise ValueError('Reserved JSON reference metadata collides with a data property')
                return {'$id': str(nr), **data}
            return {'$id': str(nr), '$values': data}
        finally:
            self.active.remove(node)

    def as_xml(self) -> str:
        def visit(node: _Node, parent: ET.Element | None, name: str | None,
                  embedded: bool = False) -> ET.Element:
            if (embedded and parent is not None and node.kind == 'opaque'
                    and getattr(type(node.obj), '__xml_choice_component_names__', None)):
                # CHOICE alternatives are immutable typed dataclasses, not
                # AIiDataObjects. Their wire member metadata is attached by
                # Defs Codegen; no wrapper element is introduced for the choice.
                from dataclasses import fields as dataclass_fields, is_dataclass
                variant = type(node.obj)
                if not is_dataclass(node.obj):
                    raise TypeError('XML CHOICE variant is not a dataclass: ' + variant.__name__)
                names = variant.__xml_choice_component_names__
                members = dataclass_fields(node.obj)
                if len(names) != len(members):
                    raise ValueError('XML CHOICE component arity mismatch: ' + variant.__name__)
                metadata = dict(variant.__dict__.get('__xml_data_object_fields__', {}))
                metadata.update(variant.__dict__.get('__xml_schema_fields__', {}))
                for semantic, member in zip(names, members):
                    raw = getattr(node.obj, member.name)
                    if raw is None:
                        raise ValueError('XML CHOICE alternative cannot omit member: ' + semantic)
                    xml = metadata.get(semantic)
                    if xml is None:
                        if not isinstance(raw, AIiDataObject):
                            raise TypeError('Transparent XML CHOICE member must be a DataObject: '
                                            + semantic)
                        visit(self.snapshot.build(raw), parent, None, embedded=True)
                    else:
                        if xml.value_kind != AInXmlValueKind.ELEMENT:
                            raise ValueError('XML CHOICE supports only element alternatives: ' + semantic)
                        qname = ('{' + xml.namespace + '}' if xml.namespace else '') + xml.namespace_local_name
                        if isinstance(raw, AIiDataObject):
                            visit(self.snapshot.build(raw), parent, qname)
                        elif isinstance(raw, (str, int, float, bool, Decimal, Enum)):
                            ET.SubElement(parent, qname).text = _xml_lexical(raw)
                        else:
                            raise TypeError('Unsupported XML CHOICE alternative value: ' + semantic)
                return parent
            if node.kind == 'smart' and any(not isinstance(member, _Field)
                                            for _, member in node.wire_members):
                raise NotImplementedError('XMLDEFS-to-integrated-MAP serialization is intentionally unsupported')
            xml_schema = (getattr(type(node.obj), '__xml_schema__', None)
                          or getattr(type(node.obj), '__xml_data_object__', None))
            tag = name or (xml_schema.namespace_local_name if xml_schema and
                           xml_schema.namespace_local_name else type(node.obj).__name__)
            if xml_schema and xml_schema.namespace and not self.profile.diagnostic and name is None:
                tag = '{' + xml_schema.namespace + '}' + tag
            if embedded and (parent is None or node.kind != 'smart'):
                raise TypeError('Embedded XML group must be a DataObject nested in a parent')
            element = parent if embedded else (ET.SubElement(parent, tag) if parent is not None else ET.Element(tag))
            ref = self.ref(node)
            if ref is not None:
                if embedded:
                    raise ValueError('Embedded XML group cannot be emitted as an object reference')
                element.set('dNrRef', str(ref))
                return element
            number = self.ids.get(node) if self.numbered(node) else None
            if number is not None and self.profile.diagnostic:
                element.set('dNr', str(number))
            if node.kind == 'null':
                element.set('{http://www.w3.org/2001/XMLSchema-instance}nil', 'true')
                return element
            self.active.add(node)
            try:
                if node.kind in ('scalar', 'opaque'):
                    if node.kind == 'opaque' and not self.profile.diagnostic:
                        raise TypeError('Unsupported opaque canonical XML value')
                    element.text = _xml_lexical(node.value)
                elif node.kind == 'smart':
                    for fld in node.fields:
                        if not fld.present and not self.profile.diagnostic and \
                                (not self.effective or not fld.descriptor.default_value_resolver):
                            continue
                        if fld.descriptor.embedded and not self.profile.diagnostic:
                            value = self.snapshot.value(fld, self.effective)
                            if value.kind == 'list':
                                for occurrence in value.children:
                                    visit(occurrence, element, None, embedded=True)
                            else:
                                visit(value, element, None, embedded=True)
                            continue
                        name = (fld.xml.namespace_local_name if fld.xml and fld.xml.namespace_local_name
                                else fld.descriptor.name)
                        if not self.profile.diagnostic and fld.xml and fld.xml.namespace:
                            name = '{' + fld.xml.namespace + '}' + name
                        if fld.xml and fld.xml.value_kind == AInXmlValueKind.ATTRIBUTE and not self.profile.diagnostic:
                            value = self.snapshot.value(fld, self.effective)
                            if value.kind != 'null':
                                if value.kind not in ('scalar', 'opaque'):
                                    raise ValueError('XML attribute must be scalar')
                                element.set(name, _xml_lexical(value.value))
                        elif self.profile.diagnostic:
                            child = ET.SubElement(element, name, present=str(fld.present).lower())
                            if fld.present:
                                visit(self.snapshot.value(fld, self.effective), child, 'Value')
                        else:
                            value = self.snapshot.value(fld, self.effective)
                            if fld.xml and bool(fld.xml.wildcard_namespace):
                                fragments = (value.children if value.kind == 'list' else (value,))
                                for fragment in fragments:
                                    if fragment.kind != 'scalar' or not isinstance(fragment.value, str):
                                        raise TypeError('XML wildcard requires a serialized XML element string')
                                    if ('<!DOCTYPE' in fragment.value.upper()
                                            or '<!ENTITY' in fragment.value.upper()):
                                        raise ValueError('DTD or entity in XML wildcard fragment')
                                    parsed = ET.fromstring(fragment.value)
                                    constraint = fld.xml.wildcard_namespace.split()
                                    namespace = parsed.tag[1:].split('}', 1)[0] if parsed.tag.startswith('{') else ''
                                    accepted = any(token == '##any' or
                                        token == '##other' and bool(namespace) and namespace != fld.xml.namespace or
                                        token == '##local' and not namespace or
                                        token == '##targetNamespace' and namespace == fld.xml.namespace or
                                        token == namespace for token in constraint)
                                    if not accepted:
                                        raise ValueError('XML wildcard namespace is not permitted: ' + parsed.tag)
                                    element.append(parsed)
                                continue
                            # XSD maxOccurs creates sibling elements with the same QName.
                            # The Item wrapper is reserved for diagnostic collection output.
                            if value.kind == 'list':
                                for item in value.children:
                                    visit(item, element, name)
                            else:
                                visit(value, element, name)
                elif node.kind == 'list':
                    for child in node.children:
                        visit(child, element, 'Item')
                else:
                    for k, v in node.entries:
                        entry = ET.SubElement(element, 'Entry')
                        visit(k, entry, 'Key')
                        visit(v, entry, 'Value')
                return element
            finally:
                self.active.remove(node)
        return ET.tostring(visit(self.snapshot.root, None, None), encoding='unicode') + '\n'

    def as_yaml(self) -> str:
        """Render flow scalars, mappings and sequences with native YAML graph aliases."""
        lines: list[str] = []

        def emit(node: _Node, level: int, prefix: str = '') -> None:
            ref = self.ref(node)
            if ref is not None:
                lines.append(prefix + '*o' + str(ref))
                return
            nr = self.ids.get(node) if self.numbered(node) else None
            marker = '&o' + str(nr) if nr is not None else ''
            if node.kind in ('scalar', 'opaque', 'null'):
                if node.kind == 'opaque' and not self.profile.diagnostic:
                    raise TypeError('Unsupported opaque canonical YAML value')
                value = _enum_code(node.value) if isinstance(node.value, Enum) else node.value
                lines.append(prefix + (marker + ' ' if marker else '') + _json_text(value))
                return
            lines.append(prefix + marker if marker else prefix.rstrip())
            self.active.add(node)
            try:
                if node.kind == 'smart':
                    schema_fields = {} if self.profile.diagnostic else _schema_wire_metadata(type(node.obj))
                    for f in node.fields:
                        if f.descriptor.field_name in schema_fields and f.present:
                            actual = self.as_data(self.snapshot.value(f, False))
                            if actual != schema_fields[f.descriptor.field_name]:
                                raise ValueError('Schema metadata conflicts with a data property: ' + f.descriptor.name)
                    for schema_key, schema_value in schema_fields.items():
                        lines.append(' ' * level + dumps(schema_key) + ': ' + _json_text(schema_value))
                    members = []
                    for name, member in node.wire_members:
                        if isinstance(member, _Field) and not member.present and not self.profile.diagnostic and \
                                (not self.effective or not member.descriptor.default_value_resolver):
                            continue
                        if name not in schema_fields:
                            members.append((name, member))
                        elif not isinstance(member, _Field):
                            raise ValueError('Additional property collides with schema metadata: ' + name)
                    if self.profile.diagnostic:
                        meta = {'type': type(node.obj).__name__}
                        if nr is not None:
                            meta['dNr'] = nr
                        lines.append(' ' * level + '"$meta": ' + dumps(meta))
                        lines.append(' ' * level + '"$fields":')
                        level += 2
                    if not members and not schema_fields:
                        lines.append(' ' * level + '{}')
                    for name, member in members:
                        locKey = ' ' * level + dumps(name) + ': '
                        if self.profile.diagnostic:
                            present = member.present if isinstance(member, _Field) else True
                            lines.append(locKey)
                            lines.append(' ' * (level + 2) + 'present: ' + str(present).lower())
                            if present:
                                value = (self.snapshot.value(member, self.effective)
                                         if isinstance(member, _Field) else member)
                                emit(value, level + 4, ' ' * (level + 2) + 'value: ')
                        else:
                            value = (self.snapshot.value(member, self.effective)
                                     if isinstance(member, _Field) else member)
                            emit(value, level + 2, locKey)
                elif node.kind == 'list':
                    if not node.children:
                        lines.append(' ' * level + '[]')
                    for child in node.children:
                        emit(child, level + 2, ' ' * level + '- ')
                else:
                    if not node.entries:
                        lines.append(' ' * level + '{}')
                    for key, child in node.entries:
                        if not isinstance(key.value, (str, int, float, bool)):
                            raise ValueError('Unsupported YAML mapping key type')
                        emit(child, level + 2, ' ' * level + dumps(str(key.value)) + ': ')
            finally:
                self.active.remove(node)

        emit(self.snapshot.root, 0)
        return '\n'.join(lines) + '\n'

    def render(self) -> str:
        if self.profile.format_type == AInTextFormatType.XML:
            return self.as_xml()
        if self.profile.format_type == AInTextFormatType.YAML and \
                self.profile.reference_encoding == AInTextReferenceEncoding.YAML_NATIVE:
            return self.as_yaml()
        return _json_text(self.as_data(self.snapshot.root)) + '\n'



@dataclass(frozen=True)
class _AdditionalPropertyKey:
    """Typed namespace for additional property keys in the single SDO store."""
    name: str


class _IntegratedAdditionalPropertiesMap(MutableMapping[str, Any]):
    """Stable, live, typed view of only the additional entries in SDO storage."""

    def __init__(self, owner: AIcSmartDataObject, value_type: type) -> None:
        self._owner = owner
        self._value_type = value_type

    def _validate(self, name: str, value: Any) -> None:
        if not isinstance(name, str):
            raise TypeError('Additional property names must be strings')
        if name in self._owner._reserved_wire_names():
            raise ValueError('Reserved SDO property name: ' + name)
        if value is not None and not isinstance(value, self._value_type):
            raise TypeError('Invalid additional property value class for ' + name)

    def __getitem__(self, name: str) -> Any:
        return self._owner._values[_AdditionalPropertyKey(name)]

    def __setitem__(self, name: str, value: Any) -> None:
        self._validate(name, value)
        self._owner._values[_AdditionalPropertyKey(name)] = value

    def __delitem__(self, name: str) -> None:
        del self._owner._values[_AdditionalPropertyKey(name)]

    def __iter__(self):
        return (key.name for key in list(self._owner._values)
                if isinstance(key, _AdditionalPropertyKey)
                and key in self._owner._values)

    def __len__(self) -> int:
        return sum(isinstance(key, _AdditionalPropertyKey) for key in self._owner._values)

    def update(self, other=(), /, **kwargs) -> None:
        """Preflight all assignments to avoid partial commit on invalid entries."""
        incoming = dict(other, **kwargs)
        for key, value in incoming.items():
            self._validate(key, value)
        for key, value in incoming.items():
            self._owner._values[_AdditionalPropertyKey(key)] = value


# One and only explicit-empty sentinel, intentionally distinct from "" and None.
EMPTY_VALUE = object()


def _check_constraints(definition: Any, value: Any) -> None:
    """Apply applicable typed field constraints without converting stored values."""
    if value is None:
        return
    string = definition.string_constraints
    if string is not None and isinstance(value, str):
        if string.min_length >= 0 and len(value) < string.min_length or \
                string.max_length >= 0 and len(value) > string.max_length or \
                string.pattern and re.fullmatch(string.pattern, value) is None:
            raise ValueError('String constraint violated for ' + definition.name)
    numeric = definition.numeric_constraints
    if numeric is not None and isinstance(value, (int, float, Decimal)) and not isinstance(value, bool):
        number = Decimal(str(value))
        if numeric.minimum:
            lower = Decimal(numeric.minimum)
            if number < lower or numeric.exclusive_minimum and number == lower:
                raise ValueError('Numeric minimum violated for ' + definition.name)
        if numeric.maximum:
            upper = Decimal(numeric.maximum)
            if number > upper or numeric.exclusive_maximum and number == upper:
                raise ValueError('Numeric maximum violated for ' + definition.name)
        if numeric.multiple_of and number % Decimal(numeric.multiple_of) != 0:
            raise ValueError('Numeric multipleOf violated for ' + definition.name)
    array = definition.list_constraints
    if array is not None and isinstance(value, (list, tuple, MutableSequence)):
        if array.min_items >= 0 and len(value) < array.min_items or \
                array.max_items >= 0 and len(value) > array.max_items or \
                array.unique_items and any(left == right for i, left in enumerate(value)
                                           for right in value[i + 1:]):
            raise ValueError('List constraint violated for ' + definition.name)
    obj = definition.object_constraints
    if obj is not None and isinstance(value, Mapping):
        if obj.min_properties >= 0 and len(value) < obj.min_properties or \
                obj.max_properties >= 0 and len(value) > obj.max_properties:
            raise ValueError('Object constraint violated for ' + definition.name)


class AIcSmartDataObject(AIiSmartDataObject):
    """Mutable, presence-aware SmartDataObject foundation."""
    DEFAULT_DIAGNOSTIC_PROFILE = AIsTextFormatProfiles.XML_DIAGNOSTIC
    DEFAULT_DIAGNOSTIC_REFERENCE_MODE = Mode.REPEATED_OBJECTS
    __data_object_fields__: tuple[Any, ...] = ()

    @classmethod
    def _fields(cls) -> dict[str, Any]:
        """Resolve canonical getter identities and their external data names."""
        return _data_object_fields(cls)

    def __init__(self, aMapping: Mapping[str, AIidVirtualFieldValue] | None = None):
        # One insertion-ordered map stores both declared and additional entries.
        # Declared names are plain strings; additional names are namespaced keys.
        # Presence follows membership, including explicitly assigned null values.
        self._values: dict[str | _AdditionalPropertyKey, Any] = {}
        self._additional_properties_view: _IntegratedAdditionalPropertiesMap | None = None
        if aMapping is not None:
            from eu.algites.lib.data.smartdataobject.impl.standard.ais_smart_data_object_factory import AIsSmartDataObjectFactory
            AIsSmartDataObjectFactory.populate(self, aMapping)

    @classmethod
    def _reserved_wire_names(cls) -> set[str]:
        """Declared serialized names and enabled schema metadata names cannot be additional keys."""
        names = {descriptor.field_name for descriptor in cls._fields().values() if not descriptor.embedded}
        for parent in cls.__mro__:
            annotation = (parent.__dict__.get('__json_schema__')
                          or parent.__dict__.get('__json_data_object__')
                          or parent.__dict__.get('__yaml_data_object__'))
            if annotation is not None:
                names.update(name for name in (
                    getattr(annotation, 'data_source_schema_url_field', ''),
                    getattr(annotation, 'data_source_schema_identifier_field', ''),
                    getattr(annotation, 'data_source_schema_version_field', ''),
                ) if name)
        return names

    def get_IntegratedAdditionalPropertiesMap(self, value_type: type) -> MutableMapping[str, Any]:
        """Return a stable typed additional-properties view over this object's shared store.

        The first requested value type fixes the view type. Later incompatible
        requests are rejected; no independently ordered map is allocated.
        """
        if not isinstance(value_type, type):
            raise TypeError('Additional-property value type must be a class')
        if self._additional_properties_view is None:
            self._additional_properties_view = _IntegratedAdditionalPropertiesMap(self, value_type)
        elif self._additional_properties_view._value_type is not value_type:
            raise ValueError('Additional-properties map already has a different value type')
        return self._additional_properties_view

    @classmethod
    def from_mapping(cls, aValue: Mapping[str, Any]):
        """Create a generated SmartDataObject from canonical raw field names.

        This belongs to the common SDO runtime, not to individual framework
        binding generators. Unknown fields and absent required fields fail.
        """
        if not isinstance(aValue, Mapping):
            raise TypeError('DataObject input must be a mapping')
        fields = cls._fields()
        wire_metadata = _schema_wire_metadata(cls)
        for key, expected in wire_metadata.items():
            if key in aValue and aValue[key] != expected:
                raise ValueError('Schema metadata does not match declared contract: ' + key)
        wire_names = {descriptor.field_name: semantic_id
                      for semantic_id, descriptor in fields.items() if not descriptor.embedded}
        # A system-property field is injected using its declared external name.
        external = {key: value for key, value in aValue.items()
                    if key not in wire_metadata or key in wire_names}
        for key, expected in wire_metadata.items():
            if key in wire_names and key not in external:
                external[key] = expected
        unknown = set(external) - wire_names.keys()
        # Embedded members have no own wire key. Their flattened children
        # are reconstructed by the format-aware typed deserializers.
        if any(descriptor.embedded for descriptor in fields.values()):
            raise NotImplementedError('Embedded mapping input requires explicit nested type metadata')
        if unknown:
            raise ValueError('Unknown DataObject fields: ' + repr(sorted(unknown)))
        result = cls()
        for data_name, value in external.items():
            result.set_RawField(wire_names[data_name], value)
        problems = result._getValidationIssues(AInSdoFieldValueLoadingMode.RAW_VALUES)
        if problems:
            raise AIxSmartDataValidationException(problems)
        return result

    def to_mapping(self) -> Mapping[str, Any]:
        """Export raw canonical wire values without serializing the entire object graph."""
        visiting: set[int] = set()

        def convert(value):
            if isinstance(value, Enum):
                return _enum_code(value)
            if getattr(type(value), '__closed_choice_mode__', None) in {'oneOf', 'anyOf'}:
                # The generated, sealed choice wrapper owns exactly one named
                # schema variant. Its `value` is a generated DataObject with the
                # corresponding single field. The wrapper itself is not a field.
                return convert(value.value)
            # A named sealed CHOICE may be embedded as the value of an
            # ordinary DataObject field, not only as an XML-transparent member.
            # Its generated dataclass carries semantic field names separately
            # from its positional constructor names (field1, field2, ...).
            if hasattr(type(value), '__xml_choice_component_names__'):
                from dataclasses import fields as dataclass_fields, is_dataclass
                if not is_dataclass(value):
                    raise TypeError('Generated CHOICE alternative must be a dataclass')
                names = type(value).__xml_choice_component_names__
                members = dataclass_fields(value)
                if len(names) != len(members):
                    raise ValueError('CHOICE alternative member arity mismatch')
                return {name: convert(getattr(value, member.name))
                        for name, member in zip(names, members)
                        if getattr(value, member.name) is not None}
            if isinstance(value, AIcSmartDataObject):
                identity = id(value)
                if identity in visiting:
                    raise ValueError('Cannot convert a cyclic SmartDataObject to an inline mapping')
                visiting.add(identity)
                try:
                    result = _schema_wire_metadata(type(value))
                    fields = value._fields()
                    for storage_key, stored in value._values.items():
                        if stored is EMPTY_VALUE:
                            stored = '' 
                        if isinstance(storage_key, _AdditionalPropertyKey):
                            name = storage_key.name
                        elif storage_key in fields:
                            name = fields[storage_key].field_name
                        else:
                            continue
                        actual = convert(stored)
                        if not isinstance(storage_key, _AdditionalPropertyKey) and fields[storage_key].embedded:
                            if not isinstance(actual, Mapping):
                                raise TypeError('Embedded DataObject must contain object fields')
                            for embedded_name, embedded_value in actual.items():
                                if embedded_name in result:
                                    raise ValueError('Conflicting embedded DataObject field: ' + embedded_name)
                                result[embedded_name] = embedded_value
                            continue
                        if name in result:
                            if actual != result[name]:
                                raise ValueError('Schema metadata conflicts with a data property: ' + name)
                        else:
                            result[name] = actual
                    # JSON/YAML `additionalProperties` may be represented by a
                    # typed generated AIiMultiMappedList. Each item is a value
                    # object with *virtual* key/value getters; they must be
                    # projected onto the containing object, never nested.
                    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
                    if isinstance(value, AIiMultiMappedList):
                        for entry in value:
                            get_key = getattr(entry, '_getKey', None)
                            get_value = getattr(entry, '_getValue', None)
                            if not callable(get_key) or not callable(get_value):
                                raise TypeError('Mapped DataObject entry requires generated virtual key and value getters')
                            name = get_key()
                            if not isinstance(name, str) or not name:
                                raise ValueError('Mapped DataObject key must be nonempty text')
                            if name in result:
                                raise ValueError('Duplicate or reserved mapped DataObject key: ' + name)
                            result[name] = convert(get_value())
                    return result
                finally:
                    visiting.remove(identity)
            if isinstance(value, Mapping):
                return {key: convert(item) for key, item in value.items()}
            if isinstance(value, (tuple, list)):
                return [convert(item) for item in value]
            return value

        return convert(self)

    def _typed_graph(self, effective: bool) -> dict[str, AIidVirtualFieldValue]:
        """Create typed virtual nodes from the same snapshot used by validators."""
        snapshot = _Snapshot(self)
        known: dict[_Node, AIidVirtualFieldValue] = {}

        def convert(node: _Node) -> AIidVirtualFieldValue:
            if node in known:
                return known[node]
            wrapper = AIidVirtualFieldValue(type(node.obj) if node.obj is not None else type(None))
            known[node] = wrapper
            if node.kind == 'smart':
                fields: dict[str, AIidVirtualFieldValue] = {}
                wrapper.value_representation = fields
                for fld in node.fields:
                    if fld.present or (effective and fld.descriptor.default_value_resolver):
                        fields[fld.descriptor.name] = convert(snapshot.value(fld, effective))
            elif node.kind == 'list':
                contents: list[AIidVirtualFieldValue] = []
                wrapper.value_representation = contents
                contents.extend(convert(child) for child in node.children)
            elif node.kind == 'map':
                contents: dict[AIidVirtualFieldValue, AIidVirtualFieldValue] = {}
                wrapper.value_representation = contents
                for key, value in node.entries:
                    contents[convert(key)] = convert(value)
            else:
                wrapper.value_representation = node.obj
            return wrapper

        return convert(snapshot.root).getValueRepresentation()

    def _toMapping(self, aMode: AInSdoFieldValueLoadingMode) -> Mapping[str, AIidVirtualFieldValue]:
        if not isinstance(aMode, AInSdoFieldValueLoadingMode):
            raise TypeError('Expected AInSdoFieldValueLoadingMode')
        return self._typed_graph(aMode is AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES)


    def _check(self, name: str) -> Any:
        try:
            return self._fields()[name]
        except KeyError:
            raise ValueError('Unknown SmartData property: ' + name) from None

    def set_RawField(self, name: str, value: Any) -> None:
        definition = self._check(name)
        self._require_allowed(definition)
        if (value is not None and definition.expected_type is not None
                and (not isinstance(value, definition.expected_type)
                     # Python's bool subclasses int, unlike Java's boxed Boolean
                     # versus Integer. A fixed numeric value of 1 must not be
                     # satisfied by True (or 0 by False).
                     or (definition.expected_type is int and type(value) is bool))):
            raise ValueError('Incompatible field value type: ' + name)
        default = definition.default_value
        if default.defined and default.kind is AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT:
            if value != _lexical_default(default.value, definition.expected_type):
                raise ValueError('Fixed field value mismatch: ' + name)
        _check_constraints(definition, value)
        self._values[name] = value
        

    def get_OrCreateCollectionField(self, name: str, mapped: bool) -> MutableSequence:
        """Lazily attach a stable list, or indexed list using read-only DO metadata.

        The read-only getter's ``__key_definitions__`` provides index ids and
        semantic field paths; no schemas are imported by the SDO runtime.
        Calling unset_Field detaches the collection and the next getter creates
        a different instance.

        Args:
            name: Internal semantic collection identity.
            mapped: True to use AIcMultiMappedList, otherwise a builtin list.

        Returns:
            Stable mutable collection attached to this SmartDataObject.

        Raises:
            ValueError: Unknown fields or invalid index definitions/paths.
        """
        definition = self._check(name)
        self._require_allowed(definition)
        if name in self._values:
            return self._values[name]
        if mapped:
            from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
            indexes = self._collectionKeyDefinitions(name)
            created = (AIcMultiMappedList(indexes, dynamic_key_defs=False)
                       if indexes else AIcMultiMappedList())
        else:
            created = []
        self.set_RawField(name, created)
        return created

    def get_OrCreateNamedMappedCollectionField(self, name: str, factory: type) -> Any:
        """Attach a named indexed SDO collection using read-contract key metadata.

        Args:
            name: Semantic field name on the containing DataObject.
            factory: Generated SDO collection class with a ``definitions`` constructor.

        Returns:
            The stable named instance; an explicitly present None remains None.

        Raises:
            ValueError: For an unknown field, invalid keys, or a type mismatch.
            TypeError: If the supplied factory cannot construct the named collection.
        """
        definition = self._check(name)
        self._require_allowed(definition)
        if name in self._values:
            return self._values[name]
        instance = factory(definitions=self._collectionKeyDefinitions(name))
        self.set_RawField(name, instance)
        return instance

    def _collectionKeyDefinitions(self, name: str) -> list:
        """Resolve annotated index paths for ordinary and named indexed lists.

        Args:
            name: Semantic field whose read-only getter declares indexes.

        Returns:
            Stable list of AIcKeyDefinition objects, possibly empty.
        """
        from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
        declared = ()
        for ancestor in reversed(type(self).__mro__):
            definitions = ancestor.__dict__.get('__key_definitions__', {})
            declared = definitions.get(name, declared)
        indexes = []
        for index_id, field_paths, required in declared:
            def extract(item, paths=field_paths):
                parts = []
                for path in paths:
                    current = item
                    for part in path.split('.'):
                        if current is None:
                            break
                        desc = _data_object_fields(type(current)).get(part)
                        if desc is None:
                            raise ValueError('Unresolved DataObject key field ' + path)
                        getter = getattr(current, desc.getter_name)
                        current = getter()
                    parts.append(current)
                return None if any(part is None for part in parts) else \
                    parts[0] if len(parts) == 1 else tuple(parts)
            indexes.append(AIcKeyDefinition(index_id, extract, True, required))
        return indexes

    def set_EmptyField(self, name: str) -> None:
        """Assign explicit EMPTY_VALUE, rejecting fields without that capability."""
        definition = self._check(name)
        self._require_allowed(definition)
        if not definition.is_empty_allowed or definition.default_value.defined and \
                definition.default_value.kind is AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT:
            raise ValueError('Explicit empty value not permitted: ' + name)
        self._values[name] = EMPTY_VALUE

    def isEmpty_Field(self, name: str) -> bool:
        """Report whether a declared field is explicitly in its empty state."""
        self._check(name)
        return self._values.get(name) is EMPTY_VALUE

    def get_RawField(self, name: str) -> Any:
        self._require_allowed(self._check(name))
        value = self._values.get(name)
        return '' if value is EMPTY_VALUE else value

    def get_EffectiveField(self, name: str) -> Any:
        definition = self._check(name)
        self._require_allowed(definition)
        if name in self._values:
            value = self._values[name]
            if value is EMPTY_VALUE:
                default = definition.default_value
                if default.defined and default.kind is AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY:
                    return _lexical_default(default.value, definition.expected_type)
                return None
            return value
        if definition.default_value_resolver:
            return definition.default_value_resolver()
        if definition.presence == AInFieldPresenceKind.REQUIRED and not self._allow_non_present_read(name, definition):
            raise ValueError('Required property is absent: ' + name)
        return None

    @staticmethod
    def _require_allowed(definition: Any) -> None:
        if definition.presence == AInFieldPresenceKind.FORBIDDEN:
            raise ValueError('Field is forbidden by derived schema: ' + definition.name)

    def _allow_non_present_read(self, name: str, definition: Any) -> bool:
        getter_name = definition.getter_name
        for cls in type(self).__mro__:
            getter = cls.__dict__.get(getter_name)
            if getter is not None and hasattr(getter, '__smart_data_object_allow_non_present_read__'):
                return bool(getter.__smart_data_object_allow_non_present_read__)
        for cls in type(self).__mro__:
            if '__smart_data_object_allow_non_present_read__' in cls.__dict__:
                return bool(cls.__dict__['__smart_data_object_allow_non_present_read__'])
        return False

    def isPresent_Field(self, name: str) -> bool:
        self._check(name)
        return name in self._values

    def unset_Field(self, name: str) -> None:
        self._check(name)
        self._values.pop(name, None)
        

    def _issues(self, effective: bool, deep: bool) -> list[AIcdSmartDataValidationIssue]:
        return _Snapshot(self).issues(effective, deep)

    def _getValidationIssues(self, aMode: AInSdoFieldValueLoadingMode):
        if not isinstance(aMode, AInSdoFieldValueLoadingMode):
            raise TypeError('Expected AInSdoFieldValueLoadingMode')
        return self._issues(aMode is AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES, False)
    def get_DeeplyRawValuesValidationIssues(self): return self._issues(False, True)
    def get_DeeplyEffectiveValuesValidationIssues(self): return self._issues(True, True)
    def is_ByRawValuesValid(self): return not self._getValidationIssues(AInSdoFieldValueLoadingMode.RAW_VALUES)
    def is_ByEffectiveValuesValid(self): return not self._getValidationIssues(AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES)
    def is_DeeplyByRawValuesValid(self): return not self.get_DeeplyRawValuesValidationIssues()
    def is_DeeplyByEffectiveValuesValid(self): return not self.get_DeeplyEffectiveValuesValidationIssues()
    def get_RawNotPresentRequiredFields(self):
        return {d.name for d in self._fields().values() if d.presence == AInFieldPresenceKind.REQUIRED and d.name not in self._values}
    def get_EffectiveNotPresentRequiredFields(self):
        return {d.name for d in self._fields().values() if d.presence == AInFieldPresenceKind.REQUIRED and d.name not in self._values and not d.default_value_resolver}
    def get_DeeplyRawNotPresentRequiredFields(self):
        return {e.path for e in self.get_DeeplyRawValuesValidationIssues() if e.code == 'REQUIRED_ABSENT'}
    def get_DeeplyEffectiveNotPresentRequiredFields(self):
        return {e.path for e in self.get_DeeplyEffectiveValuesValidationIssues() if e.code == 'REQUIRED_ABSENT'}
    def toString_RawValues(self, aProfile, aMode=Mode.NO_REFERENCES):
        return _Renderer(_Snapshot(self), aProfile, aMode, False).render()
    def toString_EffectiveValues(self, aProfile, aMode=Mode.NO_REFERENCES):
        snapshot = _Snapshot(self)
        if not aProfile.diagnostic:
            issues = snapshot.issues(True, True)
            if issues:
                raise AIxSmartDataValidationException(issues)
        return _Renderer(snapshot, aProfile, aMode, True).render()
    def __str__(self):
        return self.toString_RawValues(self.DEFAULT_DIAGNOSTIC_PROFILE, self.DEFAULT_DIAGNOSTIC_REFERENCE_MODE)
