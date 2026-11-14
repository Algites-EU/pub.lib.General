"""Regression tests for the isolated naming bootstrap snapshot manager."""
from __future__ import annotations

import importlib.util
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).with_name('promote-naming-extgen.py')
spec = importlib.util.spec_from_file_location('promote_naming_extgen', SCRIPT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class NamingExtgenPromotionTests(unittest.TestCase):
    """Reject incomplete input and preserve atomic Java/Python snapshots."""

    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.product = Path(self.tmp.name) / 'naming/convention/intf/src/product'
        self.product.mkdir(parents=True)
        self.manifest = Path(self.tmp.name) / 'naming/convention/intf/naming-bootstrap-extgen.json'
        self.old_product, self.old_manifest = module.PRODUCT, module.MANIFEST
        module.PRODUCT, module.MANIFEST = self.product, self.manifest
        self.addCleanup(self.restore)

    def restore(self):
        module.PRODUCT, module.MANIFEST = self.old_product, self.old_manifest

    def seed(self, language, role, value='original'):
        suffix = '.java' if language == 'java' else '.py'
        prefix = {'read': 'AIig', 'interface': 'AIigd', 'implementation': 'AIcgd'}[role]
        if language == 'python':
            prefix = prefix.lower()
        directory = ('impl/intf/' if role == 'interface' else 'impl/' if role == 'implementation' else '')
        output = self.product / (language + '.gen') / ('eu/algites/lib/naming/convention/' + directory + prefix + 'Sample_1' + suffix)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(value, encoding='utf-8')
        return output

    def test_committed_seed_has_all_shared_contract_roles(self):
        """Legacy seed is accepted without making x/j/y part of the new API."""
        module.PRODUCT, module.MANIFEST = self.old_product, self.old_manifest
        module.check()
        for lang, suffix in module.SUFFIXES.items():
            sources = module.snapshot(module.PRODUCT / (lang + '.extgen'), suffix)
            roles = module.roles(sources, lang)
            self.assertTrue(all(roles.values()))
        self.assertTrue(module.MANIFEST.exists())

    def test_new_sdo_must_not_require_format_prefixed_contracts(self):
        """A complete new common snapshot is promotable without old prefixes."""
        for lang in ('java', 'python'):
            for role in ('read', 'interface', 'implementation'):
                self.seed(lang, role)
            for role in ('interface', 'implementation'):
                prefix = 'AIigd' if role == 'interface' else 'AIcgd'
                directory = 'impl/intf' if role == 'interface' else 'impl'
                for name in ('IdentifierConventions', 'NameCompositions',
                             'NameFragments', 'VersionConventions',
                             'CommonNamingConventions', 'StandardConventions'):
                    import re
                    symbol = prefix + name + '_1'
                    if lang == 'python':
                        symbol = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', symbol).lower()
                    file = self.product / (lang + '.gen') / 'eu/algites/lib/naming/convention' / directory / (
                        symbol + ('.py' if lang == 'python' else '.java'))
                    file.parent.mkdir(parents=True, exist_ok=True)
                    file.write_text('generated')
        import json
        self.manifest.write_text(json.dumps({'kind': 'bootstrap-abi-stubs'}))
        module.refresh()
        module.check()

    def test_reject_partial_named_output_in_initial_seed_transition(self):
        """A few generated classes are not enough to replace the ABI seed."""
        import json
        for lang in ('java', 'python'):
            for role in ('read', 'interface', 'implementation'):
                self.seed(lang, role)
        self.manifest.write_text(json.dumps({'kind': 'bootstrap-abi-stubs'}))
        with self.assertRaisesRegex(ValueError, 'Missing generated java naming'):
            module.refresh()
        self.assertFalse((self.product / 'java.extgen').exists())
        self.assertEqual('bootstrap-abi-stubs', json.loads(self.manifest.read_text())['kind'])

    def test_reject_missing_java_sdo_before_modifying_seed(self):
        for lang in ('java', 'python'):
            self.seed(lang, 'read')
            if lang == 'python':
                self.seed(lang, 'interface')
                self.seed(lang, 'implementation')
        with self.assertRaisesRegex(ValueError, 'Incomplete java naming'):
            module.refresh()
        self.assertFalse(self.manifest.exists())
        self.assertFalse((self.product / 'python.extgen').exists())

    def test_complete_snapshot_refresh_and_check(self):
        source = None
        for lang in ('java', 'python'):
            for role in ('read', 'interface', 'implementation'):
                source = self.seed(lang, role)
        module.refresh()
        module.check()
        self.assertTrue(source.is_file(), 'Generated sources remain intact for final compilation')
        (self.product / 'java.extgen/eu/algites/lib/naming/convention/AIigSample_1.java').write_text('tampered')
        with self.assertRaisesRegex(ValueError, 'Stale naming seed'):
            module.check()


if __name__ == '__main__':
    unittest.main()
