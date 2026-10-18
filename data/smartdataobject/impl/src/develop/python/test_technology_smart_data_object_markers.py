"""Contract hierarchy regression tests for generated JSON/YAML/XML data objects."""

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.xmldefs.aii_xml_data_object import AIiXmlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aii_json_data_object import AIiJsonDataObject
from eu.algites.lib.data.dataobject.yamldefs.aii_yaml_data_object import AIiYamlDataObject
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject
from eu.algites.lib.data.smartdataobject.xmldefs.aii_xml_smart_data_object import AIiXmlSmartDataObject
from eu.algites.lib.data.smartdataobject.jsondefs.aii_json_smart_data_object import AIiJsonSmartDataObject
from eu.algites.lib.data.smartdataobject.yamldefs.aii_yaml_smart_data_object import AIiYamlSmartDataObject


def test_read_only_technology_markers():
    for marker in (AIiXmlDataObject, AIiJsonDataObject, AIiYamlDataObject):
        assert issubclass(marker, AIiDataObject)


def test_mutable_technology_markers():
    for read_only, mutable in (
        (AIiXmlDataObject, AIiXmlSmartDataObject),
        (AIiJsonDataObject, AIiJsonSmartDataObject),
        (AIiYamlDataObject, AIiYamlSmartDataObject),
    ):
        assert issubclass(mutable, read_only)
        assert issubclass(mutable, AIiSmartDataObject)


def test_technology_contracts_are_distinct():
    assert not issubclass(AIiXmlSmartDataObject, AIiJsonDataObject)
    assert not issubclass(AIiYamlSmartDataObject, AIiXmlDataObject)
    assert not issubclass(AIiJsonSmartDataObject, AIiYamlDataObject)


from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.jsondefs.aic_json_smart_data_object import AIcJsonSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.yamldefs.aic_yaml_smart_data_object import AIcYamlSmartDataObject


def test_technology_implementation_bases():
    for source, implementer, marker in (
        (AIiXmlDataObject, AIcXmlSmartDataObject, AIiXmlSmartDataObject),
        (AIiJsonDataObject, AIcJsonSmartDataObject, AIiJsonSmartDataObject),
        (AIiYamlDataObject, AIcYamlSmartDataObject, AIiYamlSmartDataObject),
    ):
        assert issubclass(implementer, AIcSmartDataObject)
        assert issubclass(implementer, marker)
        assert issubclass(implementer, source)
        assert not implementer.__abstractmethods__
        assert implementer().to_mapping() == {}


def test_generated_mutable_objects_can_inherit_schema_contracts():
    class AIigXmlExample(AIiXmlDataObject):
        pass

    class AIigJsonExample(AIiJsonDataObject):
        pass

    class AIigYamlExample(AIiYamlDataObject):
        pass

    class AIcgdXmlExample(AIcXmlSmartDataObject, AIigXmlExample):
        pass

    class AIcgdJsonExample(AIcJsonSmartDataObject, AIigJsonExample):
        pass

    class AIcgdYamlExample(AIcYamlSmartDataObject, AIigYamlExample):
        pass

    for cls, contract, marker in (
        (AIcgdXmlExample, AIigXmlExample, AIiXmlSmartDataObject),
        (AIcgdJsonExample, AIigJsonExample, AIiJsonSmartDataObject),
        (AIcgdYamlExample, AIigYamlExample, AIiYamlSmartDataObject),
    ):
        instance = cls()
        assert isinstance(instance, contract)
        assert isinstance(instance, marker)
        assert instance.to_mapping() == {}
