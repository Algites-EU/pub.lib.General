"""Smart Data Object runtime. Internal graph nodes are deliberately private."""
from __future__ import annotations

from collections.abc import Mapping
from dataclasses import dataclass, field
from decimal import Decimal
from enum import Enum
from json import dumps
from typing import Any
import xml.etree.ElementTree as ET

from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.smartdataobject.aix_smart_data_validation_exception import AIxSmartDataValidationException
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles
from eu.algites.lib.text.format.ain_text_format_type import AInTextFormatType
from eu.algites.lib.text.format.ain_text_reference_encoding import AInTextReferenceEncoding
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode as Mode



def _data_object_fields(cls: type) -> dict[str, Any]:
    """Collect normalized metadata without requiring methods on the data-object marker."""
    descriptors: dict[str, Any] = {}
    for base in reversed(cls.__mro__):
        for descriptor in base.__dict__.get('__data_object_fields__', ()):
            previous = descriptors.get(descriptor.name)
            if previous is not None and previous != descriptor:
                raise ValueError('Conflicting inherited field: ' + descriptor.name)
            descriptors[descriptor.name] = descriptor
    return descriptors

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


@dataclass(eq=False)
class _Node:
    kind: str
    obj: Any
    value: Any = None
    fields: list[_Field] = field(default_factory=list)
    children: list[_Node] = field(default_factory=list)
    entries: list[tuple[_Node, _Node]] = field(default_factory=list)


@dataclass(eq=False)
class _Field:
    descriptor: Any
    present: bool
    raw: _Node
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
        key = id(obj)
        if key in self.registry:
            assert self.registry[key].obj is obj
            return self.registry[key]
        if len(self.registry) >= 50000:
            raise ValueError('Snapshot too large')
        kind = ('smart' if isinstance(obj, AIiDataObject) else
                'map' if isinstance(obj, Mapping) else
                'list' if isinstance(obj, (tuple, list, set)) else
                'scalar' if isinstance(obj, (str, int, float, bool, Decimal, Enum)) else 'opaque')
        node = _Node(kind, obj, str(obj) if kind == 'opaque' else obj)
        self.registry[key] = node
        if kind == 'smart':
            for desc in _data_object_fields(type(obj)).values():
                present = desc.name in obj._present if isinstance(obj, AIcSmartDataObject) else True
                if present:
                    if isinstance(obj, AIcSmartDataObject):
                        value = obj._values[desc.name]
                    else:
                        getter = desc.getter_name or 'get' + desc.name
                        value = getattr(obj, getter)()
                else:
                    value = None
                node.fields.append(_Field(desc, present, self.build(value) if present else self.nil))
        elif kind == 'map':
            node.entries = [(self.build(k), self.build(v)) for k, v in obj.items()]
        elif kind == 'list':
            node.children = [self.build(v) for v in obj]
        return node

    def value(self, fld: _Field, effective: bool) -> _Node:
        if not effective or fld.present:
            return fld.raw
        if not fld.resolved:
            fld.resolved = True
            fld.effective = self.build(fld.descriptor.default_factory()) if fld.descriptor.default_factory else self.nil
        return fld.effective

    def issues(self, effective: bool, deep: bool) -> list[AIcdSmartDataValidationIssue]:
        issues: list[AIcdSmartDataValidationIssue] = []
        visited: set[_Node] = set()
        def traverse(node: _Node, path: str) -> None:
            if node in visited:
                return
            visited.add(node)
            if node.kind == 'smart':
                for f in node.fields:
                    name = f'{path}.{f.descriptor.name}' if path else f.descriptor.name
                    if not f.present and f.descriptor.presence_required and (not effective or not f.descriptor.default_factory):
                        issues.append(AIcdSmartDataValidationIssue(name, 'REQUIRED_ABSENT', 'Required property is absent'))
                        continue
                    if not f.present and (not effective or not f.descriptor.default_factory):
                        continue
                    value = self.value(f, effective)
                    if value.kind == 'null' and not f.descriptor.allows_null:
                        issues.append(AIcdSmartDataValidationIssue(name, 'NULL_FORBIDDEN', 'Null is not allowed'))
                    elif value.kind != 'null' and f.descriptor.expected_type and not isinstance(value.obj, f.descriptor.expected_type):
                        issues.append(AIcdSmartDataValidationIssue(name, 'WRONG_TYPE', 'Type is incompatible'))
                    if deep:
                        traverse(value, name)
            elif deep and node.kind == 'list':
                for i, v in enumerate(node.children):
                    traverse(v, f'{path}[{i}]')
            elif deep and node.kind == 'map':
                for k, v in node.entries:
                    traverse(v, f'{path}[{k.value}]')
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
            return [self.snapshot.value(f, self.effective) for f in node.fields
                    if f.present or self.effective and f.descriptor.default_factory]
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
            val = node.value.value if isinstance(node.value, Enum) else node.value
            size = len(dumps(val, ensure_ascii=False, default=str))
        elif node.kind == 'smart':
            size = 2
            for f in node.fields:
                if not f.present and not self.profile.diagnostic and \
                        (not self.effective or not f.descriptor.default_factory):
                    continue
                size += len(dumps(f.descriptor.name)) + 2
                if f.present or self.effective and f.descriptor.default_factory:
                    size += self.approximate_size(self.snapshot.value(f, self.effective), path)
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
                data = node.value.value if isinstance(node.value, Enum) else node.value
            elif node.kind == 'list':
                data = [self.as_data(child) for child in node.children]
            elif node.kind == 'map':
                if any(not isinstance(k.value, str) for k, _ in node.entries):
                    raise ValueError('JSON/YAML map keys must be strings in this profile')
                data = {k.value: self.as_data(v) for k, v in node.entries}
            else:
                data = {}
                for fld in node.fields:
                    if not fld.present and not self.profile.diagnostic and (not self.effective or not fld.descriptor.default_factory):
                        continue
                    val = self.as_data(self.snapshot.value(fld, self.effective)) if \
                        fld.present or self.effective and fld.descriptor.default_factory else None
                    data[fld.descriptor.name] = ({'present': fld.present, **({'value': val} if fld.present else {})}
                                                 if self.profile.diagnostic else val)
                if self.profile.diagnostic:
                    meta = {'type': type(node.obj).__name__}
                    fingerprint = getattr(type(node.obj), '__schema_fingerprint__', '')
                    if fingerprint:
                        meta['schemaHash'] = fingerprint
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
        def visit(node: _Node, parent: ET.Element | None, name: str | None) -> ET.Element:
            tag = name or (getattr(type(node.obj), '__xml_local_name__', '') or
                           type(node.obj).__name__ if node.kind == 'smart' else 'Value')
            if parent is None and not self.profile.diagnostic and node.kind == 'smart':
                namespace = getattr(type(node.obj), '__xml_namespace__', '')
                if namespace:
                    tag = '{' + namespace + '}' + tag
            element = ET.SubElement(parent, tag) if parent is not None else ET.Element(tag)
            ref = self.ref(node)
            if ref is not None:
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
                    element.text = str(node.value)
                elif node.kind == 'smart':
                    if self.profile.diagnostic:
                        fingerprint = getattr(type(node.obj), '__schema_fingerprint__', '')
                        if fingerprint:
                            element.set('schemaHash', fingerprint)
                    for fld in node.fields:
                        if not fld.present and not self.profile.diagnostic and \
                                (not self.effective or not fld.descriptor.default_factory):
                            continue
                        name = fld.descriptor.xml_local_name or fld.descriptor.name
                        if not self.profile.diagnostic and fld.descriptor.xml_namespace:
                            name = '{' + fld.descriptor.xml_namespace + '}' + name
                        if fld.descriptor.xml_attribute and not self.profile.diagnostic:
                            value = self.snapshot.value(fld, self.effective)
                            if value.kind != 'null':
                                if value.kind not in ('scalar', 'opaque'):
                                    raise ValueError('XML attribute must be scalar')
                                element.set(name, str(value.value))
                        elif self.profile.diagnostic:
                            child = ET.SubElement(element, name, present=str(fld.present).lower())
                            if fld.present:
                                visit(self.snapshot.value(fld, self.effective), child, 'Value')
                        else:
                            visit(self.snapshot.value(fld, self.effective), element, name)
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
                value = node.value.value if isinstance(node.value, Enum) else node.value
                lines.append(prefix + (marker + ' ' if marker else '') + _json_text(value))
                return
            lines.append(prefix + marker if marker else prefix.rstrip())
            self.active.add(node)
            try:
                if node.kind == 'smart':
                    fields = []
                    for f in node.fields:
                        if not f.present and not self.profile.diagnostic and \
                                (not self.effective or not f.descriptor.default_factory):
                            continue
                        fields.append(f)
                    if self.profile.diagnostic:
                        meta = {'type': type(node.obj).__name__}
                        fingerprint = getattr(type(node.obj), '__schema_fingerprint__', '')
                        if fingerprint:
                            meta['schemaHash'] = fingerprint
                        if nr is not None:
                            meta['dNr'] = nr
                        lines.append(' ' * level + '"$meta": ' + dumps(meta))
                        lines.append(' ' * level + '"$fields":')
                        level += 2
                    if not fields:
                        lines.append(' ' * level + '{}')
                    for f in fields:
                        locKey = ' ' * level + dumps(f.descriptor.name) + ': '
                        if self.profile.diagnostic:
                            lines.append(locKey)
                            lines.append(' ' * (level + 2) + 'present: ' + str(f.present).lower())
                            if f.present:
                                emit(self.snapshot.value(f, self.effective), level + 4,
                                     ' ' * (level + 2) + 'value: ')
                        else:
                            emit(self.snapshot.value(f, self.effective), level + 2, locKey)
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


class AIcSmartDataObject(AIiSmartDataObject):
    """Mutable, presence-aware SmartDataObject foundation."""
    DEFAULT_DIAGNOSTIC_PROFILE = AIsTextFormatProfiles.XML_DIAGNOSTIC
    DEFAULT_DIAGNOSTIC_REFERENCE_MODE = Mode.REPEATED_OBJECTS
    __data_object_fields__: tuple[Any, ...] = ()
    __schema_fingerprint__ = ''

    @classmethod
    def _fields(cls) -> dict[str, Any]:
        data: dict[str, Any] = {}
        for base in reversed(cls.__mro__):
            for fld in base.__dict__.get('__data_object_fields__', ()):
                if fld.name in data and data[fld.name] != fld:
                    raise ValueError('Conflicting inherited field: ' + fld.name)
                data[fld.name] = fld
        return data

    def __init__(self):
        self._values: dict[str, Any] = {}
        self._present: set[str] = set()

    def _check(self, name: str) -> Any:
        try:
            return self._fields()[name]
        except KeyError:
            raise ValueError('Unknown SmartData property: ' + name) from None

    def set_RawField(self, name: str, value: Any) -> None:
        self._check(name)
        self._values[name] = value
        self._present.add(name)

    def get_RawField(self, name: str) -> Any:
        self._check(name)
        return self._values.get(name)

    def get_EffectiveField(self, name: str) -> Any:
        definition = self._check(name)
        if name in self._present:
            return self._values[name]
        if definition.default_factory:
            return definition.default_factory()
        if definition.presence_required:
            raise ValueError('Required property is absent: ' + name)
        return None

    def isPresent_Field(self, name: str) -> bool:
        self._check(name)
        return name in self._present

    def unset_Field(self, name: str) -> None:
        self._check(name)
        self._values.pop(name, None)
        self._present.discard(name)

    def _issues(self, effective: bool, deep: bool) -> list[AIcdSmartDataValidationIssue]:
        return _Snapshot(self).issues(effective, deep)

    def get_RawValuesValidationIssues(self): return self._issues(False, False)
    def get_EffectiveValuesValidationIssues(self): return self._issues(True, False)
    def get_DeeplyRawValuesValidationIssues(self): return self._issues(False, True)
    def get_DeeplyEffectiveValuesValidationIssues(self): return self._issues(True, True)
    def is_ByRawValuesValid(self): return not self.get_RawValuesValidationIssues()
    def is_ByEffectiveValuesValid(self): return not self.get_EffectiveValuesValidationIssues()
    def is_DeeplyByRawValuesValid(self): return not self.get_DeeplyRawValuesValidationIssues()
    def is_DeeplyByEffectiveValuesValid(self): return not self.get_DeeplyEffectiveValuesValidationIssues()
    def get_RawNotPresentRequiredFields(self):
        return {d.name for d in self._fields().values() if d.presence_required and d.name not in self._present}
    def get_EffectiveNotPresentRequiredFields(self):
        return {d.name for d in self._fields().values() if d.presence_required and d.name not in self._present and not d.default_factory}
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
