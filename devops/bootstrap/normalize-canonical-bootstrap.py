"""Normalize generated canonical bootstrap contracts to current format annotations.

This adapter is confined to persistent bootstrap outputs originating in the
legacy first-stage compiler. It never modifies the historical compiler or
hand-edits disposable generated roots. The canonical schema remains the source of field names, while schema identity is
kept in format-local annotations. Internal canonical instances must not inject
source-document $schema metadata into nested nodes.
"""
from __future__ import annotations

import ast
import re

PY_KEYS = {'schema_id', 'schema_version', 'schema_url',
           'data_source_schema_url_field', 'data_source_schema_identifier_field',
           'data_source_schema_version_field'}
JAVA_KEYS = {'schemaId', 'schemaVersion', 'schemaUrl',
             'dataSourceSchemaUrlField', 'dataSourceSchemaIdentifierField',
             'dataSourceSchemaVersionField'}


def _python(text: str) -> str:
    """Move format-specific keyword arguments to their JSON descriptor."""
    lines = text.splitlines(keepends=True)
    result = []
    for line in lines:
        match = re.search(r'(?P<indent>\s*)__data_object__\s*=\s*AIcdDataObject\(', line)
        if match is None:
            result.append(line)
            continue
        start = line.index('AIcdDataObject(', match.start()) + len('AIcdDataObject(')
        end = line.rfind(')')
        if end < start:
            raise ValueError('Expected single-line generated DataObject metadata')
        source_expression = 'f(' + line[start:end] + ')'
        expression = ast.parse(source_expression, mode='eval').body
        neutral, format_local = [], []
        for kw in expression.keywords:
            if kw.arg in PY_KEYS:
                if not kw.arg.startswith('data_source_schema_'):
                    format_local.append(line_segment(source_expression, kw))
            elif kw.arg in {'title', 'description', 'setter_inheritance_policy'}:
                neutral.append(line_segment(source_expression, kw))
            elif kw.arg not in {'fingerprint', 'naming_convention'}:
                raise ValueError(f'Unexpected generated metadata key: {kw.arg}')
        newline = '\n' if line.endswith('\n') else ''
        indent = match.group('indent')
        result.append(indent + '__data_object__ = AIcdDataObject(' + ', '.join(neutral) + ')' + newline)
        if format_local:
            result.append(indent + '__json_data_object__ = AIaJsonDataObject(' + ', '.join(format_local) + ')' + newline)
    text = ''.join(result)
    if '__json_data_object__' in text and 'from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject' not in text:
        pos = text.index('from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject')
        text = (text[:pos] + 'from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject\n' + text[pos:])
    return text


def line_segment(text: str, kw: ast.keyword) -> str:
    """Copy keyword value source verbatim; avoid evaluating enum expressions."""
    value = ast.get_source_segment(text, kw.value)
    if value is None or kw.arg is None:
        raise ValueError('Missing metadata keyword source')
    return kw.arg + '=' + value


def _java(text: str) -> str:
    """Split generated Java type annotation into neutral and JSON metadata."""
    pattern = re.compile(r'@eu\.algites\.lib\.data\.dataobject\.AIaDataObject\(([^\n]*)\)')

    def replace(match: re.Match[str]) -> str:
        values = re.split(r', (?=[a-zA-Z][a-zA-Z0-9]* = )', match.group(1))
        neutral, specific = [], []
        for value in values:
            key = value.split(' = ', 1)[0]
            if key in JAVA_KEYS:
                if not key.startswith('dataSourceSchema'):
                    specific.append(value)
            elif key in {'title', 'description', 'setterInheritancePolicy'}:
                neutral.append(value)
            elif key not in {'fingerprint', 'namingConvention'}:
                raise ValueError('Unexpected generated Java metadata: ' + key)
        result = '@eu.algites.lib.data.dataobject.AIaDataObject(' + ', '.join(neutral) + ')'
        if specific:
            result += '\n@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(' + ', '.join(specific) + ')'
        return result

    return pattern.sub(replace, text)


def _java_element_sdo_seed(text: str) -> str:
    """Upgrade the schema-generated first-stage Java SDO seed for ElementKind.

    This is a temporary bootstrap adapter only. The independent Java SDO
    generator will produce the same accessors from the new Defs read contract
    during a complete source bootstrap. Never rewrite handwritten sources.
    """
    if 'getElementKind()' in text or 'AIigd_jElement_1' not in text:
        return text
    field_type = 'eu.algites.tool.codegen.defs.model.AIngElementKind_1'
    if 'public interface AIigd_jElement_1<' in text:
        methods = (f'    /** Canonical placement of this member within its ELEMENTLIST. */\n'
                   f'    @eu.algites.lib.data.dataobject.AIaDataObjectField(fieldName = "element_kind", '
                   f'presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false)\n'
                   f'    @Override {field_type} getElementKind();\n'
                   f'    void setElementKind({field_type} aElementKind);\n'
                   f'    {field_type} _getRaw_ElementKind();\n'
                   f'    boolean _isPresent_ElementKind();\n'
                   f'    void _unset_ElementKind();\n')
        return text[:text.rfind('}')] + methods + text[text.rfind('}'):]
    if 'public class AIcgd_jElement_1 ' in text:
        methods = (f'    @Override public {field_type} getElementKind() '
                   f'{{ return ({field_type}) get_EffectiveField("ElementKind"); }}\n'
                   f'    @Override public void setElementKind({field_type} aElementKind) '
                   f'{{ set_RawField("ElementKind", aElementKind); }}\n'
                   f'    @Override public {field_type} _getRaw_ElementKind() '
                   f'{{ return ({field_type}) get_RawField("ElementKind"); }}\n'
                   f'    @Override public boolean _isPresent_ElementKind() '
                   f'{{ return isPresent_Field("ElementKind"); }}\n'
                   f'    @Override public void _unset_ElementKind() {{ unset_Field("ElementKind"); }}\n')
        return text[:text.rfind('}')] + methods + text[text.rfind('}'):]
    return text


def normalize(text: str, language: str) -> str:
    """Normalize generated format metadata without mutating original sources."""
    # The immutable SDO bootstrap seed predates the canonical field-count rename.
    # Reconcile the seed to the schema-generated read contracts deterministically.
    # These replacements apply only to the extgen promotion input; never to
    # source schema parsers, tests, or the historical implementation.
    for old, new in (('min_occurs', 'min_item_count'),
                     ('max_occurs', 'max_item_count'),
                     ('MinOccurs', 'MinItemCount'),
                     ('MaxOccurs', 'MaxItemCount'),
                     ('MIN_OCCURS', 'MIN_ITEM_COUNT'),
                     ('MAX_OCCURS', 'MAX_ITEM_COUNT')):
        text = text.replace(old, new)
    if language == 'python':
        return _python(text)
    if language == 'java':
        return _java_element_sdo_seed(_java(text))
    raise ValueError('Unsupported language: ' + language)
