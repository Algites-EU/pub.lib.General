"""Offline execution tests of the public library's standalone bootstrap CLI.

The fake Gradle, packaging helper and Cloudsmith commands are deliberate:
these tests verify invocation semantics, not compilation or network upload.
"""
from __future__ import annotations

import os
from pathlib import Path
import shutil
import subprocess
import sys

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / 'bootstrap.sh'


@pytest.fixture
def fake_repository(tmp_path: Path) -> tuple[Path, dict[str, str]]:
    root = tmp_path / 'pub.lib.General'
    (root / 'devtools/bootstrap').mkdir(parents=True)
    (root / 'build').mkdir()
    shutil.copy2(SCRIPT, root / 'devtools/bootstrap.sh')
    (root / 'modustro-source-repository.yml').write_text(
        "Version:\n  ReleaseLineVersion: '1'\n  Revision: 1\n  QualifierKind: snapshot\n",
        encoding='utf-8',
    )
    (root / 'gradlew').write_text(
        '#!/usr/bin/env bash\n'
        'echo "gradle:$*" >> "$TEST_LOG"\n', encoding='utf-8'
    )
    (root / 'devtools/bootstrap/prepare.py').write_text(
        '''import argparse
import os
from pathlib import Path

p = argparse.ArgumentParser()
p.add_argument('action')
p.add_argument('--repository', required=True)
p.add_argument('--output', required=True)
p.add_argument('--version', required=True)
p.add_argument('--python-version')
p.add_argument('--python-executable')
a = p.parse_args()
root = Path(a.output)
root.mkdir(parents=True, exist_ok=True)
with open(os.environ['TEST_LOG'], 'a') as stream:
    stream.write('helper:' + a.action + '\\n')
if a.action == 'plan':
    (root / 'gradle').mkdir(exist_ok=True)
    (root / 'python-release.txt').write_text('1.1.dev20261024000000\\n')
elif a.action == 'verify-maven':
    files = []
    for i in range(34):
        base = root / f'maven-{i}'
        paths = [Path(str(base) + s) for s in ('.jar', '.pom', '.module', '-sources.jar')]
        for path in paths:
            path.write_bytes(b'test')
        files.append('\\t'.join(map(str, paths)))
    (root / 'packages.tsv').write_text('\\n'.join(files) + '\\n')
elif a.action == 'package-python':
    files = []
    for i in range(12):
        paths = [root / f'package-{i}.whl', root / f'package-{i}.tar.gz']
        for path in paths:
            path.write_bytes(b'test')
        files.append('\\t'.join(map(str, paths)))
    (root / 'python-packages.tsv').write_text('\\n'.join(files) + '\\n')
''', encoding='utf-8'
    )
    bin_dir = tmp_path / 'bin'
    bin_dir.mkdir()
    cloudsmith = bin_dir / 'cloudsmith'
    cloudsmith.write_text(
        '#!/usr/bin/env bash\n'
        'printf "cloudsmith:%s\\n" "$*" >> "$TEST_LOG"\n', encoding='utf-8'
    )
    cloudsmith.chmod(0o755)
    wrapper = bin_dir / 'algites-cloudsmith'
    wrapper.write_text(
        '#!/usr/bin/env bash\n'
        'printf "wrapper:%s\\n" "$*" >> "$TEST_LOG"\n'
        '[[ "$1" == --session && "$2" == -- ]] || exit 31\n'
        'shift 2\n'
        'exec "$@"\n', encoding='utf-8'
    )
    wrapper.chmod(0o755)
    log = tmp_path / 'bootstrap.log'
    env = dict(os.environ)
    env.update({
        'TEST_LOG': str(log),
        'PATH': str(bin_dir) + os.pathsep + env['PATH'],
        'ALGITES_PYTHON_EXECUTABLE': sys.executable,
    })
    for var in ('MODUSTRO_BOOTSTRAP_PREPARE_ONLY', 'MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE',
                'MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES', 'MODUSTRO_BOOTSTRAP_OUTPUT_DIR',
                'MODUSTRO_GENERAL_BOOTSTRAP_CLOUDSMITH_REPO',
                'MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_CLOUDSMITH_REPO',
                'MODUSTRO_GENERAL_BOOTSTRAP_JAVA_REPO',
                'MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_REPO'):
        env.pop(var, None)
    return root, env


def run_bootstrap(fake_repository, *args: str, **updates: str) -> tuple[subprocess.CompletedProcess, str]:
    root, env = fake_repository
    env = {**env, **updates}
    completed = subprocess.run(
        ['bash', str(root / 'devtools/bootstrap.sh'), *args], env=env,
        text=True, capture_output=True, timeout=30, check=False,
    )
    log = Path(env['TEST_LOG'])
    return completed, log.read_text() if log.exists() else ''


def test_default_invocation_prepares_and_uploads_once(fake_repository):
    result, log = run_bootstrap(fake_repository)
    assert result.returncode == 0, result.stderr
    assert log.count('helper:plan') == 1
    assert log.count('helper:verify-maven') == 1
    assert log.count('helper:package-python') == 1
    assert log.count('gradle:') == 1
    assert '--refresh-dependencies' in log
    assert log.count('wrapper:--session -- ') == 1
    assert log.count('cloudsmith:push maven ') == 34
    assert log.count('cloudsmith:push python ') == 24


def test_prepare_only_env_skips_upload_in_single_call(fake_repository):
    result, log = run_bootstrap(fake_repository,
                                MODUSTRO_BOOTSTRAP_PREPARE_ONLY='1')
    assert result.returncode == 0, result.stderr
    assert log.count('helper:package-python') == 1
    assert 'cloudsmith:' not in log and 'wrapper:' not in log


def test_prepare_only_env_overrides_explicit_upload(fake_repository):
    result, log = run_bootstrap(fake_repository, '--upload',
                                MODUSTRO_BOOTSTRAP_PREPARE_ONLY='1')
    assert result.returncode == 0, result.stderr
    assert 'helper:verify-maven' in log
    assert 'wrapper:' not in log


def test_legacy_prepare_only_option_still_works(fake_repository):
    result, log = run_bootstrap(fake_repository, '--prepare-only')
    assert result.returncode == 0, result.stderr
    assert 'helper:verify-maven' in log
    assert 'wrapper:' not in log


def test_plan_only_does_not_build_or_upload(fake_repository):
    result, log = run_bootstrap(fake_repository, '--plan-only')
    assert result.returncode == 0, result.stderr
    assert log == 'helper:plan\n'


def test_local_mode_changes_default_refresh_and_custom_manifest_destination(fake_repository, tmp_path):
    manifest_output = tmp_path / 'manifests'
    result, log = run_bootstrap(fake_repository, '--prepare-only',
                                MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE='local',
                                MODUSTRO_BOOTSTRAP_OUTPUT_DIR=str(manifest_output))
    assert result.returncode == 0, result.stderr
    assert 'gradle:' in log and '--refresh-dependencies' not in log
    assert len((manifest_output / 'packages.tsv').read_text().splitlines()) == 34
    assert len((manifest_output / 'python-packages.tsv').read_text().splitlines()) == 12
    assert (manifest_output / 'python-release.txt').is_file()


def test_source_mode_refresh_can_be_forced_and_repo_overridden(fake_repository):
    result, log = run_bootstrap(
        fake_repository,
        MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE='source',
        MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES='1',
        MODUSTRO_GENERAL_BOOTSTRAP_CLOUDSMITH_REPO='my-org/java',
        MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_CLOUDSMITH_REPO='my-org/python',
    )
    assert result.returncode == 0, result.stderr
    assert '--refresh-dependencies' in log
    assert log.count('cloudsmith:push maven my-org/java ') == 34
    assert log.count('cloudsmith:push python my-org/python ') == 24


@pytest.mark.parametrize('name,value', [
    ('MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE', 'invalid'),
    ('MODUSTRO_BOOTSTRAP_PREPARE_ONLY', 'yes'),
    ('MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES', 'sometimes'),
])
def test_invalid_bootstrap_modes_rejected(fake_repository, name, value):
    result, log = run_bootstrap(fake_repository, **{name: value})
    assert result.returncode == 2
    assert 'Invalid MODUSTRO_BOOTSTRAP_' in result.stderr
    assert log == ''


def test_help_does_not_require_dependencies(fake_repository):
    result, log = run_bootstrap(fake_repository, '--help')
    assert result.returncode == 0
    assert 'MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1' in result.stdout
    assert log == ''
