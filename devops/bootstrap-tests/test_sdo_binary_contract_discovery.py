"""Regression for the XML namespace marker discovered in the Java SDO binary scan."""

from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[2]
JAVA_GENERATED = ROOT / 'generators/code/defscodegen/intf/src/product/java.gen/eu/algites/tool/codegen/defs'
CLI = ROOT / 'generators/code/sdocodegen/cli/src/product/java/eu/algites/tool/codegen/sdo/impl/cli/AIcSdoCodegenCli.java'
SERVICE = ROOT / 'generators/code/sdocodegen/impl/src/product/java/eu/algites/tool/codegen/sdo/impl/standard/AIcSdoCodegenService.java'
pytestmark = pytest.mark.skipif(not JAVA_GENERATED.exists(), reason='Tool repository only')


def test_common_dataobject_has_no_separate_xml_namespace_projection():
    """A QName belongs to metadata, never an additional nominal DataObject."""
    definition = (JAVA_GENERATED / 'AIigInputProfile_1.java').read_text()
    assert '@eu.algites.lib.data.dataobject.AIaDataObject(' in definition
    assert not list(JAVA_GENERATED.rglob('*XmlDataObjectUrn*.java'))


def test_binary_discovery_uses_same_contract_gate_as_sdo_service():
    cli = CLI.read_text()
    service = SERVICE.read_text()
    assert 'if (!AIcSdoCodegenService.isReadContract(locClass)) continue;' in cli
    assert 'public static boolean isReadContract(Class<?> aClass)' in service
    assert 'AIiXmlDataObjectNamespace.class.isAssignableFrom(aClass)' in service
    assert 'aClass.getDeclaredAnnotation(AIaDataObject.class) == null' in service
