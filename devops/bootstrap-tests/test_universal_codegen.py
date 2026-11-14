"""Common Defs bootstrap discovery and temporary Algites profile regression tests."""
from __future__ import annotations

from pathlib import Path
import subprocess
import sys

import pytest

BOOTSTRAP = Path(__file__).resolve().parents[1] / 'bootstrap'
sys.path.insert(0, str(BOOTSTRAP))
import generate_sources as codegen  # noqa: E402


def _artifact(root: Path, path: str, languages=('java', 'python')) -> Path:
    target = root / path
    target.mkdir(parents=True)
    (target / 'modustro-artifact.yml').write_text('Artifact: {}\n')
    for language in languages:
        (target / 'src/product' / (language + '.gen')).mkdir(parents=True)
    return target


def test_empty_repository_requires_no_generator_or_dependencies(tmp_path, monkeypatch):
    artifact = _artifact(tmp_path, 'nonmatching/one')
    (artifact / 'src/product/jsondefs').mkdir()
    monkeypatch.setattr(codegen, '_load_codegen', lambda *_: pytest.fail('Unexpected import'))
    assert codegen.generate(tmp_path, tmp_path, tmp_path / 'tmp') == 0


def test_noop_even_if_generated_roots_contain_non_schemas(tmp_path, monkeypatch):
    artifact = _artifact(tmp_path, 'one')
    inputs = artifact / 'src/product/yamldefs'
    inputs.mkdir()
    (inputs / 'profile.meta.yml').write_text('metadata-only')
    monkeypatch.setattr(codegen, '_load_codegen', lambda *_: pytest.fail('Unexpected import'))
    assert codegen.generate(tmp_path, tmp_path, tmp_path / 'tmp') == 0


def test_json_encoded_yamldefs_is_discovered_and_scoped(tmp_path):
    a = _artifact(tmp_path, 'a/one')
    b = _artifact(tmp_path, 'b/two')
    for artifact in (a, b):
        source = artifact / 'src/product/yamldefs/example.yamldef.schema.json'
        source.parent.mkdir()
        source.write_text('{}')
    selected = codegen._applicable_artifacts(tmp_path, tmp_path / 'a')
    assert list(selected) == [(a, 'product')]
    assert selected[(a, 'product')] == (['java', 'python'], ['yamldefs'])


def test_defs_naming_profiles_are_forwarded_from_selected_settings(tmp_path, monkeypatch):
    """The common bootstrap forwards real CLI settings, not a legacy manifest."""
    artifact = _artifact(tmp_path, 'project')
    source = artifact / 'src/product'

    def fake_settings(owner, language):
        assert owner == tmp_path
        return {
            'input-schema-naming-convention:jsondefs':
                'algites-jsondef-schema-versioned_1',
            f'dointf-naming-convention:{language}':
                f'algites-{language}-gen-intf-versioned_1',
            f'common-codegen-naming-convention:{language}':
                f'algites-{language}-codegen-common_1',
            f'defs-codegen-naming-convention:{language}':
                f'algites-{language}-defscodegen-standard_1',
            'naming-conventions-file:codegen_common':
                f'classpath:algites-{language}-common-codegen-conventions.yml',
            'naming-conventions-file:codegen_defs':
                f'classpath:algites-{language}-defs-codegen-conventions.yml',
        }

    monkeypatch.setattr(codegen, 'defs_settings', fake_settings)
    monkeypatch.setattr(codegen, '_source_checkouts', lambda _: [tmp_path])
    options = codegen._qualified_defs_arguments(
        tmp_path, artifact, 'product', ['java', 'python'], ['jsondefs'], tmp_path)
    assert f'--defs-input-paths:java={source.relative_to(tmp_path)}/jsondefs' in options
    assert f'--defs-input-paths:python={source.relative_to(tmp_path)}/jsondefs' in options
    assert '--dointf-naming-convention:java=algites-java-gen-intf-versioned_1' in options
    assert '--dointf-naming-convention:python=algites-python-gen-intf-versioned_1' in options
    assert '--naming-conventions-file:codegen_common_java=classpath:algites-java-common-codegen-conventions.yml' in options
    assert '--naming-conventions-file:codegen_common_python=classpath:algites-python-common-codegen-conventions.yml' in options
    assert '--naming-conventions-file:codegen_defs_java=classpath:algites-java-defs-codegen-conventions.yml' in options
    assert '--naming-conventions-file:codegen_defs_python=classpath:algites-python-defs-codegen-conventions.yml' in options
    assert '--input-schema-naming-convention:jsondefs=algites-jsondef-schema-versioned_1' in options
    assert not any('_manifest' in option for option in options)


def test_bootstrap_configuration_never_requires_specific_generator_runner():
    script = BOOTSTRAP / 'bootstrap-config.py'
    root = BOOTSTRAP.parent.parent
    result = subprocess.run([sys.executable, str(script), '--repository', str(root), '--nul'],
                            capture_output=True)
    assert result.returncode == 0, result.stderr.decode()
    values = [item for item in result.stdout.decode().split('\x00') if item]
    assert len(values) == 4
    assert 'devops/codegen/run.sh' not in values
    assert not (root / 'devops/codegen/run.sh').exists()


def test_scalar_xsd_annotation_vocabulary_is_not_dataobject_input(tmp_path):
    """A published vocabulary with only built-in scalar elements has no TYPE."""
    artifact = _artifact(tmp_path, 'documentation/intf', languages=('java',))
    directory = artifact / 'src/product/xmldefs'
    directory.mkdir()
    (directory / 'algites-documentation_1.xsd').write_text('''<?xml version="1.0"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="urn:doc">
  <xs:annotation><xs:documentation>Annotation vocabulary</xs:documentation></xs:annotation>
  <xs:element name="title" type="xs:string"/>
  <xs:element name="enabled" type="xs:boolean"/>
</xs:schema>''', encoding='utf-8')
    assert codegen._applicable_artifacts(tmp_path, tmp_path) == {}


def test_mixed_xsd_vocabulary_and_datatype_remains_input(tmp_path):
    """Do not skip schemas containing actual named DataObject types."""
    artifact = _artifact(tmp_path, 'example/intf', languages=('java',))
    directory = artifact / 'src/product/xmldefs'
    directory.mkdir()
    (directory / 'example_1.xsd').write_text('''<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xs:element name="title" type="xs:string"/>
<xs:complexType name="Person"><xs:sequence>
<xs:element name="name" type="xs:string"/>
</xs:sequence></xs:complexType></xs:schema>''', encoding='utf-8')
    assert codegen._applicable_artifacts(tmp_path, tmp_path) == {
        (artifact, 'product'): (['java'], ['xmldefs'])}


def test_unsupported_global_complex_xsd_is_not_silently_skipped(tmp_path):
    """Inline complex global elements still reach frontend validation."""
    artifact = _artifact(tmp_path, 'example/intf', languages=('java',))
    directory = artifact / 'src/product/xmldefs'
    directory.mkdir()
    (directory / 'example_1.xsd').write_text('''<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
<xs:element name="person"><xs:complexType><xs:sequence>
<xs:element name="name" type="xs:string"/>
</xs:sequence></xs:complexType></xs:element></xs:schema>''', encoding='utf-8')
    assert codegen._applicable_artifacts(tmp_path, tmp_path) == {
        (artifact, 'product'): (['java'], ['xmldefs'])}


def test_malformed_xsd_still_reaches_generator_diagnostics(tmp_path):
    """Malformed input is never treated as an annotation-only vocabulary."""
    artifact = _artifact(tmp_path, 'example/intf', languages=('java',))
    directory = artifact / 'src/product/xmldefs'
    directory.mkdir()
    (directory / 'broken_1.xsd').write_text('<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">', encoding='utf-8')
    assert codegen._applicable_artifacts(tmp_path, tmp_path) == {
        (artifact, 'product'): (['java'], ['xmldefs'])}


def test_real_documentation_vocabulary_is_not_generated_input():
    """Regression for Algites' metadata-only algites-documentation_1.xsd."""
    repository = BOOTSTRAP.parents[1]
    assert codegen._xsd_is_scalar_vocabulary(
        repository / 'documentation/intf/src/product/xmldefs/eu/algites/lib/common/'
        'documentation/algites-documentation_1.xsd')
    assert all(artifact.relative_to(repository).as_posix() != 'documentation/intf'
               for artifact, branch in codegen._applicable_artifacts(repository, repository))
