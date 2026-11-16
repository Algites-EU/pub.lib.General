"""Regression tests for per-invocation bootstrap paths with strict bash nounset."""
from __future__ import annotations

import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / 'bootstrap/bootstrap.sh'


class BootstrapRunIdTests(unittest.TestCase):
    """Exercise both run-ID consumers without building or publishing artifacts."""

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repository = Path(self.temp.name) / 'repository'
        self.bootstrap = self.repository / 'devops/bootstrap'
        self.bootstrap.mkdir(parents=True)
        shutil.copy2(SCRIPT, self.bootstrap / 'bootstrap.sh')
        # The bootstrap's repository-neutral specialization helper is a
        # mandatory companion even when no specializations are configured.
        shutil.copy2(SCRIPT.with_name('selfhosting.py'),
                     self.bootstrap / 'selfhosting.py')
        self._write('clean_generated.py', '''\
from pathlib import Path
import sys
if sys.argv[1] == 'validate':
    print(Path(sys.argv[sys.argv.index('--repository') + 1]).resolve())
''')
        self._write('bootstrap-config.py', '''\
import sys
if '--nul' in sys.argv:
    sys.stdout.buffer.write(b'maven-target\\0python-target\\0')
''')
        self._write('schema-locations.py', '''\
from pathlib import Path
import sys
path = Path(sys.argv[sys.argv.index('--output') + 1])
path.parent.mkdir(parents=True, exist_ok=True)
path.write_text('# generated for this invocation\\n')
''')
        self._write('upload-prepared.py', '''\
from pathlib import Path
import sys
path = Path(sys.argv[sys.argv.index('--output') + 1])
path.mkdir(parents=True, exist_ok=True)
(path / 'packages.tsv').write_text('java manifest\\n')
(path / 'python-packages.tsv').write_text('python manifest\\n')
''')
        self._write('build.sh', '''\
#!/bin/bash
set -euo pipefail
test -f "$MODUSTRO_BOOTSTRAP_SCHEMA_LOCATIONS_PROPERTY_FILE"
printf 'BUILD_STUB_REACHED\\n'
''')
        self._write('upload-cloudsmith.sh', '''\
#!/bin/bash
set -euo pipefail
test -s "$2"
test -s "$4"
printf 'UPLOAD_STUB_REACHED\\n'
''')
        bin_dir = Path(self.temp.name) / 'bin'
        bin_dir.mkdir()
        cloudsmith = bin_dir / 'algites-cloudsmith'
        cloudsmith.write_text('#!/bin/bash\nset -euo pipefail\nshift 2\n"$@"\n')
        cloudsmith.chmod(0o755)
        self.env = os.environ.copy()
        self.env.update({
            'PATH': str(bin_dir) + os.pathsep + os.environ['PATH'],
            'PYTHONDONTWRITEBYTECODE': '1',
            'MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE': 'remote',
            'MODUSTRO_CODEGEN_CLEANUP_OUTPUT_FOLDER_BEFORE_GENERATION': 'false',
        })
        for key in ('MODUSTRO_BOOTSTRAP_VERSION', 'MODUSTRO_BOOTSTRAP_PYTHON_VERSION'):
            self.env.pop(key, None)
        (self.repository / 'modustro-source-repository.yml').write_text('''\
Version:
  ReleaseLineVersion: '1'
  Revision: '1'
  QualifierKind: snapshot
''')

    def _write(self, name: str, contents: str) -> None:
        (self.bootstrap / name).write_text(contents)

    def _run(self, action: str) -> subprocess.CompletedProcess[str]:
        return subprocess.run(
            ['bash', str(self.bootstrap / 'bootstrap.sh'), action],
            cwd=self.repository, env=self.env, capture_output=True,
            text=True, timeout=15, check=False,
        )

    def test_prepare_only_creates_run_scoped_schema_map(self):
        """Run UUID must exist before writing the strict offline schema map."""
        result = self._run('--prepare-only')
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn('BUILD_STUB_REACHED', result.stdout)
        run_id = re.search(r'Bootstrap run ID: ([0-9a-f-]{36})', result.stdout)
        self.assertIsNotNone(run_id)
        self.assertTrue((self.repository / 'build/run/schema-locations' /
                         (run_id.group(1) + '.properties')).is_file())

    def test_upload_prepared_creates_run_scoped_manifests_without_build(self):
        """Upload-only path must initialize UUID without invoking generation."""
        result = self._run('--upload-prepared')
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn('UPLOAD_STUB_REACHED', result.stdout)
        self.assertNotIn('BUILD_STUB_REACHED', result.stdout)
        run_id = re.search(r'Bootstrap run ID: ([0-9a-f-]{36})', result.stdout)
        self.assertIsNotNone(run_id)
        self.assertTrue((self.repository / 'build/run/upload-prepared' /
                         run_id.group(1) / 'packages.tsv').is_file())


if __name__ == '__main__':
    unittest.main()
