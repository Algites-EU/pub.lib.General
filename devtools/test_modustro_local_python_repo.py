"""Regression coverage for the file-backed Modustro Python package index."""
from __future__ import annotations

import importlib.util
from pathlib import Path
import zipfile

import pytest

HELPER = Path(__file__).with_name('modustro-local-python-repo.py')
SPEC = importlib.util.spec_from_file_location('modustro_local_python_repository', HELPER)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def make_distributions(directory: Path, version: str = '1.1.dev202610270001') -> tuple[Path, Path]:
    directory.mkdir(parents=True, exist_ok=True)
    wheel = directory / f'eu_algites_example-{version}-py3-none-any.whl'
    sdist = directory / f'eu_algites_example-{version}.tar.gz'
    with zipfile.ZipFile(wheel, 'w') as target:
        target.writestr(f'eu_algites_example-{version}.dist-info/METADATA',
                        f'Metadata-Version: 2.1\nName: eu-algites-example\nVersion: {version}\n')
        target.writestr(f'eu_algites_example-{version}.dist-info/WHEEL',
                        'Wheel-Version: 1.0\nGenerator: modustro-test\nRoot-Is-Purelib: true\nTag: py3-none-any\n')
        target.writestr(f'eu_algites_example-{version}.dist-info/RECORD', '')
        target.writestr('eu_algites_example/__init__.py', '')
    import tarfile
    with tarfile.open(sdist, 'w:gz') as target:
        import io
        data = b'name = "eu-algites-example"\n'
        info = tarfile.TarInfo(f'eu_algites_example-{version}/PKG-INFO')
        info.size = len(data)
        target.addfile(info, io.BytesIO(data))
    return wheel, sdist


def test_publication_creates_simple_index_with_checksums(tmp_path):
    wheel, sdist = make_distributions(tmp_path/'source')
    manifest = tmp_path/'manifest.tsv'
    manifest.write_text(f'{wheel}\t{sdist}\n')
    repo = tmp_path/'repository'
    assert MODULE.publish(manifest, repo) == 1
    page = (repo/'simple/eu-algites-example/index.html').read_text()
    assert wheel.name in page and sdist.name in page
    assert '#sha256=' in page
    assert 'eu-algites-example/' in (repo/'simple/index.html').read_text()
    assert MODULE.publish(manifest, repo) == 1


def test_republishing_changed_immutable_version_is_rejected(tmp_path):
    wheel, sdist = make_distributions(tmp_path/'source')
    manifest = tmp_path/'manifest.tsv'
    manifest.write_text(f'{wheel}\t{sdist}\n')
    repo = tmp_path/'repository'
    MODULE.publish(manifest, repo)
    with zipfile.ZipFile(wheel, 'a') as target:
        target.writestr('changed.txt', 'changed content')
    with pytest.raises(ValueError, match='different content'):
        MODULE.publish(manifest, repo)


def test_rejects_missing_wheel_metadata_and_version_mismatch(tmp_path):
    wheel, sdist = make_distributions(tmp_path/'source')
    with zipfile.ZipFile(wheel, 'a') as target:
        target.writestr(f'eu_algites_example-1.1.dev202610270001.dist-info/OTHER', 'x')
    bad_sdist = tmp_path/'source/eu_algites_example-1.1.dev202610270002.tar.gz'
    bad_sdist.write_bytes(sdist.read_bytes())
    manifest = tmp_path/'manifest.tsv'
    manifest.write_text(f'{wheel}\t{bad_sdist}\n')
    with pytest.raises(ValueError, match='different projects/versions'):
        MODULE.publish(manifest, tmp_path/'repository')
