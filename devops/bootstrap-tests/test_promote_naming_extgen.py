"""Regression tests for the isolated naming bootstrap snapshot manager."""
from __future__ import annotations

import importlib.util
import shutil
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / 'bootstrap/promote-naming-extgen.py'
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
        self.old_product = module.PRODUCT
        module.PRODUCT = self.product
        self.addCleanup(self.restore)

    def restore(self):
        module.PRODUCT = self.old_product

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
        module.PRODUCT = self.old_product
        module.check()
        for lang, suffix in module.SUFFIXES.items():
            sources = module.snapshot(module.PRODUCT / (lang + '.extgen'), suffix)
            roles = module.roles(sources, lang)
            self.assertTrue(all(roles.values()))
        # Completeness is derived from actual source paths without metadata.

    def full_generated_naming_seed(self):
        """Construct a complete unified snapshot for both languages."""
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
                    output = (self.product / (lang + '.gen') / 'eu/algites/lib/naming/convention'
                              / directory / (symbol + ('.py' if lang == 'python' else '.java')))
                    output.parent.mkdir(parents=True, exist_ok=True)
                    output.write_text('generated', encoding='utf-8')

    def test_complete_unified_snapshot_needs_no_persisted_manifest(self):
        """A valid, already promoted snapshot needs no old bookkeeping file."""
        self.full_generated_naming_seed()
        for lang in ('java', 'python'):
            shutil.copytree(self.product / (lang + '.gen'), self.product / (lang + '.extgen'))
        module.check()
        legacy = self.product.parents[1] / 'naming-bootstrap-extgen.json'
        legacy.write_text('{\"old\":true}\n', encoding='utf-8')
        module.refresh()
        module.check()
        self.assertEqual(legacy.read_text(encoding='utf-8'), '{"old":true}\n',
                         'Promotion must not touch unrelated historical files')

    def test_incomplete_seed_is_rejected_without_manifest(self):
        """Never allow missing metadata to mask an incomplete ABI snapshot."""
        for lang in ('java', 'python'):
            for role in ('read', 'interface', 'implementation'):
                file = self.seed(lang, role)
                destination = self.product / (lang + '.extgen') / file.relative_to(
                    self.product / (lang + '.gen'))
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(file, destination)
        with self.assertRaisesRegex(ValueError, 'Missing generated java naming'):
            module.check()

    def test_obsolete_projection_is_rejected_without_manifest(self):
        """An old j/x/y interface must not be bootstrapped as a new API."""
        self.full_generated_naming_seed()
        for lang in ('java', 'python'):
            shutil.copytree(self.product / (lang + '.gen'), self.product / (lang + '.extgen'))
        legacy = (self.product / 'java.extgen/eu/algites/lib/naming/convention'
                  / 'AIig_xCommonNamingConventions_1.java')
        legacy.write_text('obsolete', encoding='utf-8')
        with self.assertRaisesRegex(ValueError, 'Obsolete technology-specific java'):
            module.check()

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
        module.refresh()
        module.check()

    def test_reject_partial_named_output_in_initial_seed_transition(self):
        """A few generated classes are not enough to replace the ABI seed."""
        for lang in ('java', 'python'):
            for role in ('read', 'interface', 'implementation'):
                self.seed(lang, role)
        with self.assertRaisesRegex(ValueError, 'Missing generated java naming'):
            module.refresh()
        self.assertFalse((self.product / 'java.extgen').exists())
        self.assertFalse(list(self.product.parent.parent.rglob('*bootstrap-extgen.json')))

    def test_reject_missing_java_sdo_before_modifying_seed(self):
        for lang in ('java', 'python'):
            self.seed(lang, 'read')
            if lang == 'python':
                self.seed(lang, 'interface')
                self.seed(lang, 'implementation')
        with self.assertRaisesRegex(ValueError, 'Incomplete java naming'):
            module.refresh()
        self.assertFalse((self.product / 'python.extgen').exists())

    def test_complete_snapshot_refresh_and_check(self):
        self.full_generated_naming_seed()
        source = self.product / 'python.gen/eu/algites/lib/naming/convention/aiigSample_1.py'
        module.refresh()
        module.check()
        self.assertTrue(source.is_file(), 'Generated sources remain intact for final compilation')
        (self.product / 'java.extgen/eu/algites/lib/naming/convention/impl/intf/AIigdNameFragments_1.java').unlink()
        with self.assertRaisesRegex(ValueError, 'Missing generated java naming'):
            module.check()


if __name__ == '__main__':
    unittest.main()
