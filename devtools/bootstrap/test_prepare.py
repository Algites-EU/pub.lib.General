"""Offline regression coverage for standalone Modustro library publication."""
from __future__ import annotations

import importlib.util
import json
from pathlib import Path
import tempfile
import zipfile
import xml.etree.ElementTree as ET

import pytest
import yaml

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location('general_bootstrap_prepare', Path(__file__).with_name('prepare.py'))
BOOT = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(BOOT)


def test_declared_module_graph_is_complete():
    records = BOOT.module_records(ROOT, '1.1-SNAPSHOT', '1.1.dev20261024000000')
    assert sum(item['java'] for item in records) == 31
    assert sum(item['python'] for item in records) == 9
    assert any(item['groupId'] == 'eu.algites.lib.naming' for item in records)
    assert any(item['groupId'] == 'eu.algites.lib.data' for item in records)
    for module in records:
        assert all(d['kind'] != 'external' or 'testng' not in d['coordinate'] for d in module['dependencies'])
        assert all(not d.startswith('pytest') for d in module['pythonDependencies'])


def test_full_gradle_graph_has_all_native_products(tmp_path):
    records = BOOT.module_records(ROOT, '1.1-SNAPSHOT', '1.1.dev20261024000000')
    BOOT.stage_gradle(ROOT, tmp_path, records, '1.1-SNAPSHOT')
    modules = json.loads((tmp_path / 'gradle' / 'modules.json').read_text())
    assert len(modules) == 31
    assert 'maven-repository' in (tmp_path / 'gradle' / 'build.gradle').read_text()
    assert 'java.gen' in (tmp_path / 'gradle' / 'build.gradle').read_text()
    assert 'xmldefs' in (tmp_path / 'gradle' / 'build.gradle').read_text()
    assert any(any(d['kind'] == 'project' for d in module['dependencies']) for module in modules)


def test_generated_gradle_respects_shared_dependency_precedence(tmp_path):
    records = BOOT.module_records(ROOT, '1.1-SNAPSHOT', '1.1.dev20261024000000')
    BOOT.stage_gradle(ROOT, tmp_path, records, '1.1-SNAPSHOT')
    gradle = (tmp_path / 'gradle/build.gradle').read_text()
    assert "MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE" in gradle
    assert "if (bootstrapMode != 'remote') mavenLocal()" in gradle
    assert "if (bootstrapMode == 'remote') mavenLocal()" in gradle
    assert "includeGroupByRegex" in gradle


def test_python_stage_preserves_shared_source_configuration_and_schemas(tmp_path):
    records = BOOT.module_records(ROOT, '1.1-SNAPSHOT', '1.1.dev20261024000000')
    module = next(item for item in records if item['path'] == 'naming/convention/algites')
    package = BOOT.stage_one_python(ROOT, tmp_path, module, '1.1.dev20261024000000')
    assert any((package / 'src').rglob('*.yml'))
    assert any((package / 'src').rglob('*.py'))
    assert (package / 'pyproject.toml').exists()
    assert '.dev20261024000000' in (package / 'pyproject.toml').read_text()


def test_xsd_new_naming_definitions_and_publish_metadata():
    expected = [
        ROOT / 'naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd',
        ROOT / 'naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/source-structure-name-conventions_1.xsd',
    ]
    for xsd in expected:
        assert xsd.is_file()
        assert xsd.with_suffix('.xsd.meta.yml').exists()
        parsed = ET.parse(xsd)
        assert parsed.getroot().tag.endswith('schema')
        publication = yaml.safe_load(xsd.with_suffix('.xsd.meta.yml').read_text())
        assert publication['GlobalPublicationPathId'].endswith(xsd.name)
    assert 'SourceStructureNameConventions' in expected[1].read_text()
    assert 'common-naming-conventions_1.xsd' in expected[1].read_text()


def test_python_snapshot_requires_fresh_version():
    result = BOOT.python_version('1.1-SNAPSHOT', None)
    assert result.startswith('1.1.dev')
    assert result != '1.1'
    assert BOOT.python_version('1.1-SNAPSHOT', '1.1.dev12') == '1.1.dev12'
    with pytest.raises(ValueError):
        BOOT.python_version('1.1-SNAPSHOT', 'invalid')


def _maven_fixture(tmp_path: Path, filename_version: str, *, metadata: bool = False,
                   snapshot_versions: bool = True) -> tuple[Path, dict]:
    """Construct a tiny, structurally valid local Maven publication."""
    record = {'java': True, 'artifactId': 'pub.lib.General_action.execution.common.intf',
              'groupId': 'eu.algites.lib.common'}
    release = '1.1-SNAPSHOT'
    directory = (tmp_path / 'maven-repository/eu/algites/lib/common' /
                 record['artifactId'] / release)
    directory.mkdir(parents=True)
    stem = record['artifactId'] + '-' + filename_version
    with zipfile.ZipFile(directory / (stem + '.jar'), 'w') as archive:
        archive.writestr('eu/algites/lib/common/Sample.class', b'class')
    (directory / (stem + '.pom')).write_text(
        '<project><modelVersion>4.0.0</modelVersion>'
        '<groupId>eu.algites.lib.common</groupId>'
        f'<artifactId>{record["artifactId"]}</artifactId>'
        '<version>1.1-SNAPSHOT</version></project>', encoding='utf-8')
    (directory / (stem + '.module')).write_text('{}', encoding='utf-8')
    with zipfile.ZipFile(directory / (stem + '-sources.jar'), 'w') as archive:
        archive.writestr('eu/algites/lib/common/Sample.java', b'public class Sample {}')
    if metadata:
        snapshot = ('<snapshot><timestamp>20261025.094033</timestamp>'
                    '<buildNumber>1</buildNumber></snapshot>')
        versions = ''
        if snapshot_versions:
            versions = '<snapshotVersions>' + ''.join(
                '<snapshotVersion>' + (f'<classifier>{classifier}</classifier>' if classifier else '') +
                f'<extension>{extension}</extension><value>{filename_version}</value></snapshotVersion>'
                for extension, classifier in [('jar', ''), ('pom', ''), ('module', ''), ('jar', 'sources')]
            ) + '</snapshotVersions>'
        (directory / 'maven-metadata.xml').write_text(
            '<metadata><groupId>eu.algites.lib.common</groupId>'
            f'<artifactId>{record["artifactId"]}</artifactId>'
            '<version>1.1-SNAPSHOT</version><versioning>' + snapshot + versions +
            '</versioning></metadata>', encoding='utf-8')
    return directory, record


def test_verify_maven_accepts_literal_snapshot_filenames(tmp_path):
    directory, record = _maven_fixture(tmp_path, '1.1-SNAPSHOT')
    BOOT.verify_maven(tmp_path, [record], '1.1-SNAPSHOT')
    files = (tmp_path / 'packages.tsv').read_text().strip().split('\t')
    assert len(files) == 4
    assert all(Path(path).is_file() for path in files)
    assert files[0] == str(directory / (record['artifactId'] + '-1.1-SNAPSHOT.jar'))


@pytest.mark.parametrize('snapshot_versions', [True, False])
def test_verify_maven_accepts_timestamped_snapshot_filenames(tmp_path, snapshot_versions):
    actual_version = '1.1-20261025.094033-1'
    directory, record = _maven_fixture(tmp_path, actual_version, metadata=True,
                                       snapshot_versions=snapshot_versions)
    BOOT.verify_maven(tmp_path, [record], '1.1-SNAPSHOT')
    files = (tmp_path / 'packages.tsv').read_text().strip().split('\t')
    assert len(files) == 4
    assert all(Path(path).is_file() for path in files)
    assert files[0] == str(directory / (record['artifactId'] + '-' + actual_version + '.jar'))


def test_verify_maven_rejects_missing_timestamped_file(tmp_path):
    directory, record = _maven_fixture(tmp_path, '1.1-20261025.094033-1', metadata=True)
    (directory / (record['artifactId'] + '-1.1-20261025.094033-1.module')).unlink()
    with pytest.raises(FileNotFoundError, match='Incomplete timestamped Maven publication'):
        BOOT.verify_maven(tmp_path, [record], '1.1-SNAPSHOT')


def test_verify_maven_reports_missing_metadata_with_files(tmp_path):
    _directory, record = _maven_fixture(tmp_path, '1.1-20261025.094033-1')
    with pytest.raises(FileNotFoundError, match='maven-repository'):
        BOOT.verify_maven(tmp_path, [record], '1.1-SNAPSHOT')


def test_verify_maven_rejects_wrong_snapshot_metadata_identity(tmp_path):
    directory, record = _maven_fixture(tmp_path, '1.1-20261025.094033-1', metadata=True)
    metadata = directory / 'maven-metadata.xml'
    metadata.write_text(metadata.read_text().replace('<version>1.1-SNAPSHOT</version>',
                                                    '<version>9.9-SNAPSHOT</version>'))
    with pytest.raises(ValueError, match='snapshot metadata identity'):
        BOOT.verify_maven(tmp_path, [record], '1.1-SNAPSHOT')
