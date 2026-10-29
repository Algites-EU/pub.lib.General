"""Standard-library regression tests for common Python/Java API parity."""
import importlib
from pathlib import Path
import unittest

from eu.algites.lib.common.interval.aic_interval import AIcInterval
from eu.algites.lib.common.interval.ain_interval_boundary import AInIntervalBoundary as B
from eu.algites.lib.common.interval.ais_interval_utils import AIsIntervalUtils as I
from eu.algites.lib.common.list.aic_bounded_list import AIcBoundedList
from eu.algites.lib.common.list.ain_bounds_mutation_policy import AInBoundsMutationPolicy as P
from eu.algites.lib.common.enums.uiddata.ain_uid_enum_data_origin import AInUidEnumDataOrigin as O
from eu.algites.lib.common.enums.uiddata.air_uid_part_metadata import AIrUidPartMetadata
from eu.algites.lib.common.enums.uiddata.ais_uid_enum_data_utils import AIsUidEnumDataUtils as U
from eu.algites.lib.common.enums.uiddata.aic_uid_enum_data_registry import AIcUidEnumDataRegistry
from eu.algites.lib.common.object.rendering.ais_rendering_output_utils import AIsRenderingOutputUtils as R
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose as Purpose
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel
from eu.algites.lib.common.object.props.labels.ais_field_label_utils import AIsFieldLabelUtils

class CommonPythonParityTest(unittest.TestCase):
    def test_java_modules_present_for_all_python_modules(self):
        source = Path(__file__).resolve().parents[2] / 'product'
        java_root = source / 'java'
        python_root = source / 'python'
        for java_file in java_root.rglob('*.java'):
            class_name = java_file.stem
            import re
            prefix_length = 4 if class_name.startswith('AIcd') else 3
            stem = class_name[:prefix_length].lower() + '_' + re.sub(
                r'(?<=[a-z0-9])(?=[A-Z])', '_', class_name[prefix_length:]).lower()
            relative = java_file.parent.relative_to(java_root)
            self.assertTrue((python_root / relative / f'{stem}.py').is_file(), f'Missing Python mirror: {java_file}')

    def test_bounds_default_policy(self):
        values = AIcBoundedList(2, 5)
        self.assertFalse(values.isValid())
        self.assertIn('alespoň 2', values.getInvalidStateMessage('cs_CZ'))
        values.addAll([1,2,3,4,5])
        self.assertTrue(values.isValid())
        with self.assertRaises(ValueError): values.add(6)
        self.assertEqual(list(values),[1,2,3,4,5])
        values.pop(0)
        values.pop(0)
        values.pop(0)
        self.assertEqual(list(values),[4,5])
        with self.assertRaises(ValueError): values.pop()
        self.assertEqual(list(values),[4,5])

    def test_atomic_mutation(self):
        values = AIcBoundedList(2, 5)
        values.addAll([1,2,3,4,5])
        def adjust(data):
            data.extend([6,7,8])
            data[:] = [value for value in data if value not in (2,3,4)]
        values.modifyAtomically(adjust)
        self.assertEqual(list(values),[1,5,6,7,8])
        with self.assertRaises(ValueError): values.modifyAtomically(lambda data: data.clear())
        self.assertEqual(len(values),5)
        def fails(data):
            data.append(9)
            raise RuntimeError('rollback')
        with self.assertRaises(RuntimeError): values.modifyAtomically(fails)
        self.assertEqual(len(values),5)

    def test_allow_any_and_intervals(self):
        values = AIcBoundedList(2,5,P.ALLOW_ANY)
        values.addAll(range(9))
        self.assertFalse(values.isValid())
        self.assertIn('at most 5',values.getInvalidStateMessage('en'))
        values.removeAll([0,1,2,3])
        self.assertTrue(values.isValid())
        interval = AIcInterval(B.OPEN,1,B.CLOSED,5)
        by_interval=AIcBoundedList(interval)
        self.assertEqual((by_interval.getMinSize(), by_interval.getMaxSize()),(2,5))
        self.assertEqual(AIcBoundedList(None,None).getMaxSize(),None)
        self.assertEqual(I.tryIntersect(AIcInterval(B.CLOSED,1,B.CLOSED,3),
                                        AIcInterval(B.OPEN,2,B.CLOSED,5)).getLeftValue(),2)
        with self.assertRaises(ValueError): AIcBoundedList(AIcInterval(B.OPEN,2,B.OPEN,3))

    def test_render_scope_restoration(self):
        old = R.usedRenderingOutputPurpose()
        def supplier():
            self.assertEqual(R.usedRenderingOutputPurpose(),Purpose.SYSTEM)
            return 'ok'
        self.assertEqual(R.resolveStringOutput(supplier,Purpose.SYSTEM),'ok')
        self.assertEqual(R.usedRenderingOutputPurpose(),old)
        def failing(): raise ValueError('bad')
        with self.assertRaises(ValueError): R.resolveStringOutput(failing,Purpose.SYSTEM)
        self.assertEqual(R.usedRenderingOutputPurpose(),old)

    def test_enum_uid(self):
        metadata=[AIrUidPartMetadata(lambda: 'Name',True)]
        class Type:
            def getSpecificUidPartsMetadata(self): return metadata
            def getUidPartCount(self): return len(metadata)+2
            def getOriginGetter(self): return O.getByCodeOrThrow
            def getUidRecordFactory(self): return lambda uid, parts: (uid,parts)
        data_type=Type()
        value=U.createBuiltinUid(['abc'],metadata)
        self.assertEqual(value,'builtin::abc')
        self.assertEqual(U.parseUid(data_type,value)[1],["builtin","","abc"])
        registry=AIcUidEnumDataRegistry()
        class Item:
            def getDataType(self): return data_type
            def uid(self): return value
        item=Item()
        self.assertEqual(registry.registerData(False,False,False,item),[item])
        self.assertIs(registry.findByUid(data_type,value),item)
        self.assertEqual(registry.releaseData(item),[item])

    def test_label_resolver_via_category_provider_discovery(self):
        from eu.algites.lib.factory.cpdiscovery.impl.standard.aic_category_provider_registry import AIcCategoryProviderRegistry
        from eu.algites.lib.factory.cpdiscovery.ais_category_provider_discovery import AIsCategoryProviderDiscovery
        from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
        from eu.algites.lib.common.object.props.labels.aii_field_label_resolver_factory import AIiFieldLabelResolverFactory
        from eu.algites.lib.common.object.props.labels.aic_standard_field_label_resolver_factory import AIcStandardFieldLabelResolverFactory
        registry = AIcCategoryProviderRegistry()
        registry.register_factories('standard', (AIcStandardFieldLabelResolverFactory(),))
        AIsCategoryProviderDiscovery.set_registry(registry)
        try:
            class Contract:
                @AIaFieldLabel(label='Fallback', labelResolver=AIaCategoryProviderFactoryDefinition(
                    AIiFieldLabelResolverFactory, factory_provider_id='algites-standard'))
                def getName(self): return None
            self.assertEqual(AIsFieldLabelUtils.findLabel(Contract, 'name'), 'Fallback')
        finally:
            AIsCategoryProviderDiscovery.set_registry(None)

    def test_label_precedence(self):
        class Test:
            @AIaFieldLabel(labels=(AIaFieldLabel.Entry(label='Localized',purpose=Purpose.USER),))
            def getName(self): return 'ignored'
        self.assertEqual(AIsFieldLabelUtils.findLabel(Test,'name',Purpose.USER),'Localized')
        self.assertEqual(AIsFieldLabelUtils.findLabel(Test,'name',Purpose.SYSTEM),'name')

if __name__ == '__main__':
    unittest.main()
