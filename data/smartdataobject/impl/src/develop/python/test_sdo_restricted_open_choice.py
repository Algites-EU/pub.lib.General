"""Generated-contract restrictions and explicit OpenChoice participation."""
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind as Presence
from eu.algites.lib.data.dataobject.open_choice_types import allowed_heads, open_choice_type_of, permits
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class AIigBase:
    __data_object_fields__ = (AIcdDataObjectField('Reference'),)


class AIigRestricted(AIigBase):
    __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.FORBIDDEN),)


class AIcgdRestricted(AIcSmartDataObject, AIigRestricted):
    def getReference(self):
        return self.get_EffectiveField('Reference')

    def setReference(self, value):
        self.set_RawField('Reference', value)

    def get_Reference(self):
        return self.get_RawField('Reference')


def test_derived_field_restriction_is_effective_and_absence_is_valid():
    obj = AIcgdRestricted()
    assert obj.is_ByRawValuesValid()
    assert not obj.get_RawNotPresentRequiredFields()
    with pytest.raises(ValueError, match='forbidden'):
        obj.setReference('X')
    with pytest.raises(ValueError, match='forbidden'):
        obj.getReference()
    with pytest.raises(ValueError, match='forbidden'):
        obj.get_Reference()
    with pytest.raises(ValueError, match='forbidden'):
        AIcgdRestricted.from_mapping({'Reference': 'X'})


def test_invalid_presence_widening_rejected():
    class AIigRequired:
        __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.REQUIRED),)

    class AIigInvalid(AIigRequired):
        __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.FORBIDDEN),)

    with pytest.raises(ValueError, match='presence restriction'):
        class AIcgdInvalid(AIcSmartDataObject, AIigInvalid):
            pass
        AIcgdInvalid._fields()


class AIigPayment:
    pass


@open_choice_type_of(AIigPayment)
class AIigCryptoPayment(AIigPayment):
    pass


@open_choice_type_of(AIigCryptoPayment)
class AIigTokenPayment(AIigCryptoPayment):
    pass


class AIigPrivatePayment(AIigPayment):
    pass


def test_transitive_explicit_open_choice_only():
    assert allowed_heads(AIigTokenPayment) == {AIigCryptoPayment, AIigPayment}
    assert permits(AIigCryptoPayment(), AIigPayment)
    assert permits(AIigTokenPayment(), AIigPayment)
    assert permits(AIigTokenPayment(), AIigCryptoPayment)
    assert not permits(AIigPrivatePayment(), AIigPayment)
    assert not permits(AIigPayment(), AIigCryptoPayment)


class AIcgdPaymentOwner(AIcSmartDataObject):
    __data_object_fields__ = (AIcdDataObjectField('Payment', expected_type=AIigPayment, open_choice=True),)

    def setPayment(self, value):
        self.set_RawField('Payment', value)


def test_open_choice_sdo_assignment():
    owner = AIcgdPaymentOwner()
    owner.setPayment(AIigTokenPayment())
    with pytest.raises(ValueError, match='Unauthorized OpenChoice'):
        owner.setPayment(AIigPrivatePayment())


def test_unmarked_derived_domain_contract_does_not_inherit_participation():
    class AIigUnmarkedExtension(AIigTokenPayment):
        __data_object__ = object()
    assert not permits(AIigUnmarkedExtension(), AIigPayment)


def test_generated_mutable_contract_delegates_to_read_contract():
    from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
    class AIigdTokenPayment(AIigTokenPayment):
        __data_object__ = AIcdDataObject(source_contract=AIigTokenPayment)
    assert permits(AIigdTokenPayment(), AIigPayment)
