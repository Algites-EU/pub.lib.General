"""Regression checks for normalized internal SDO field identity."""
import pytest

from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class Customer(AIcSmartDataObject):
    @AIaDataObjectField(field_name='external_customer_id')
    def getCustomerId(self):
        return self.get_EffectiveField('CustomerId')


class CustomerChild(Customer):
    @AIaDataObjectField(presence=AInFieldPresenceKind.REQUIRED)
    def getCustomerId(self):
        return self.get_EffectiveField('CustomerId')


def test_runtime_uses_semantic_id_but_serializes_original_data_name():
    obj = Customer()
    obj.set_RawField('CustomerId', 123)
    assert obj.getCustomerId() == 123
    assert obj.to_mapping() == {'external_customer_id': 123}
    restored = Customer.from_mapping({'external_customer_id': 456})
    assert restored.getCustomerId() == 456
    with pytest.raises(ValueError, match='Unknown'):
        restored.set_RawField('external_customer_id', 999)


def test_inherited_wire_name_and_restriction():
    descriptors = CustomerChild._fields()
    assert list(descriptors) == ['CustomerId']
    assert descriptors['CustomerId'].field_name == 'external_customer_id'
    assert descriptors['CustomerId'].presence is AInFieldPresenceKind.REQUIRED


def test_override_cannot_change_data_name():
    class Bad(Customer):
        @AIaDataObjectField(field_name='different')
        def getCustomerId(self): ...
    with pytest.raises(ValueError, match='fieldName'):
        Bad._fields()


def test_different_getters_cannot_share_data_name():
    class Bad(Customer):
        @AIaDataObjectField(field_name='external_customer_id')
        def getOtherId(self): ...
    with pytest.raises(ValueError, match='Duplicate fieldName'):
        Bad._fields()


def test_xml_qname_mapping_does_not_use_semantic_id_as_wire_name():
    """XML QName metadata stays keyed by its external data name while raw state uses the getter ID."""
    from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
    from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
    from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
    from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles

    class Order(AIcXmlSmartDataObject):
        __xml_data_object__ = AIaXmlDataObject(namespace='urn:semantic', namespace_local_name='Order')
        __xml_data_object_fields__ = {
            'order_code': AIaXmlDataObjectField(namespace='urn:semantic', namespace_local_name='Code')
        }

        @AIaDataObjectField(field_name='order_code', expected_type=str)
        def getOrderCode(self):
            return self.get_EffectiveField('OrderCode')

    instance = Order()
    instance.set_RawField('OrderCode', 'A-42')
    xml = instance.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert '<' in xml and 'Code>A-42</' in xml
    restored = Order.from_xml(xml)
    assert restored.get_RawField('OrderCode') == 'A-42'
    assert restored.to_mapping() == {'order_code': 'A-42'}
