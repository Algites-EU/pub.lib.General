"""Naming bootstrap must never mix real Python output with the compiler ABI."""
from __future__ import annotations

import importlib.util
from pathlib import Path

import pytest


SCRIPT = Path(__file__).resolve().parents[1] / 'bootstrap' / 'prepare.py'


def prepare():
    spec = importlib.util.spec_from_file_location('naming_bootstrap_prepare', SCRIPT)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


@pytest.fixture
def naming(tmp_path):
    artifact = tmp_path / 'naming/convention/intf'
    path = 'eu/algites/lib/naming/convention/aiig_common_naming_conventions_1.py'
    for root, data in [('python.extgen', 'SEED\n'), ('python.gen', 'GENERATED\n')]:
        target = artifact / 'src/product' / root / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(data, encoding='utf-8')
    (artifact / 'naming-bootstrap-extgen.json').write_text('{}\n', encoding='utf-8')
    record = {'artifactId':'pub.lib.General_naming.convention.intf',
              'path':'naming/convention/intf','pythonName':'test-naming-intf',
              'pythonRequires':'>=3.10','pythonDependencies':[], 'description':'test'}
    return tmp_path, artifact, record, path


def complete(naming):
    _, artifact, _, _ = naming
    root = artifact / 'src/product/python.gen/eu/algites/lib/naming/convention/impl'
    for path in ('intf/aiigd_sample.py', 'aicgd_sample.py'):
        target = root / path
        target.parent.mkdir(parents=True,exist_ok=True)
        target.write_text('# generated\n', encoding='utf-8')


def stage(naming, work):
    repository, _, record, path = naming
    project = prepare().stage_one_python(repository, work, record, '1.1.dev1')
    return (project / 'src' / path).read_text(encoding='utf-8')


def test_seed_selects_extgen(monkeypatch, naming, tmp_path):
    complete(naming)
    monkeypatch.setenv('MODUSTRO_BOOTSTRAP_SDO_ABI_SEED', '1')
    assert stage(naming, tmp_path / 'seed') == 'SEED\n'


def test_complete_generated_tree_selected(monkeypatch, naming, tmp_path):
    complete(naming)
    monkeypatch.delenv('MODUSTRO_BOOTSTRAP_SDO_ABI_SEED', raising=False)
    assert stage(naming, tmp_path / 'generated') == 'GENERATED\n'


def test_incomplete_generated_tree_falls_back_to_seed(monkeypatch, naming, tmp_path):
    monkeypatch.delenv('MODUSTRO_BOOTSTRAP_SDO_ABI_SEED', raising=False)
    assert stage(naming, tmp_path / 'fallback') == 'SEED\n'


def test_unrelated_sources_still_cannot_conflict(monkeypatch, naming, tmp_path):
    complete(naming)
    monkeypatch.delenv('MODUSTRO_BOOTSTRAP_SDO_ABI_SEED', raising=False)
    _, artifact, _, path = naming
    manual = artifact / 'src/product/python' / path
    manual.parent.mkdir(parents=True,exist_ok=True)
    manual.write_text('MANUAL\n', encoding='utf-8')
    with pytest.raises(ValueError, match='Conflicting resource'):
        stage(naming, tmp_path / 'conflict')


def test_other_artifact_conflicting_extgen_and_gen_still_fails(naming, tmp_path):
    repo, _, record, _ = naming
    record['artifactId'] = 'pub.lib.General_other'
    with pytest.raises(ValueError, match='Conflicting resource'):
        prepare().stage_one_python(repo,tmp_path / 'other',record,'1.1.dev1')
