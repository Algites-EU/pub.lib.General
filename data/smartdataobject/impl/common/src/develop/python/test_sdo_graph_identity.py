"""Symmetric Python regressions for semantic aliases and cyclic typed graphs."""
from __future__ import annotations

from enum import Enum
import pytest

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_factory import AIsSmartDataObjectFactory
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_data_tree import AIsSmartDataObjectDataTree


class Color(Enum):
    """External codes are stable independently of constant names."""
    RED = 'r-stable'
    BLUE = 'b-stable'

    def code(self) -> str:
        """Return the stable external value."""
        return self.value


class InternalColor(Enum):
    """An internal enum with no external code is not serializable."""
    RED = 'r-stable'


class Node(AIiDataObject):
    """Recursive read-only contract."""
    @AIaDataObjectField(dataFieldName='next')
    def getNext(self) -> Node: ...

    @AIaDataObjectField(dataFieldName='color')
    def getColor(self) -> Color: ...


@AIaSmartDataObjectImplementationBinding(readContracts=(Node,))
class NodeSDO(AIcSmartDataObject, Node):
    """Mutable graph implementation."""
    def getNext(self) -> Node: return self.get_EffectiveField('Next')
    def getColor(self) -> Color: return self.get_EffectiveField('Color')
    def setNext(self, other: Node) -> None: self.set_RawField('Next', other)
    def setColor(self, color: Color) -> None: self.set_RawField('Color', color)


def registry() -> AIcSmartDataObjectBindingRegistry:
    """Return a single-type graph binding registry."""
    return AIcSmartDataObjectBindingRegistry.builder().register(NodeSDO).build()


def test_enum_codes_and_no_name_fallback() -> None:
    """External enum codes, never Java/Python constant names, drive decoding."""
    item = NodeSDO()
    item.setColor(Color.RED)
    assert AIsSmartDataObjectDataTree.toData(item, registry())['color'] == 'r-stable'
    assert AIsSmartDataObjectFactory.fromData(Node, {'color': 'r-stable'}, registry()).getColor() is Color.RED
    with pytest.raises(ValueError, match='Unknown enumeration code'):
        AIsSmartDataObjectFactory.fromData(Node, {'color': 'RED'}, registry())
    with pytest.raises(ValueError, match='code'):
        AIsSmartDataObjectDataTree.toData(InternalColor.RED, registry())


def test_aliases_and_cycle_roundtrip() -> None:
    """Two names cannot address the same field; input graph identity is preserved."""
    with pytest.raises(ValueError, match='Duplicate aliases'):
        AIsSmartDataObjectFactory.fromData(Node, {'color': 'r-stable', 'Color': 'b-stable'}, registry())
    cycle = {}
    cycle['next'] = cycle
    linked = AIsSmartDataObjectFactory.fromData(Node, cycle, registry())
    assert linked.getNext() is linked
    with pytest.raises(ValueError, match='Cyclic'):
        AIsSmartDataObjectDataTree.toData(linked, registry())


def test_strict_decoding_for_builtin_types() -> None:
    """Booleans, fractions and invalid scalars must not silently become integers."""
    from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_factory import _convert
    for bad in (True, '1.2', 1.5, float('nan')):
        with pytest.raises((ValueError, TypeError)):
            _convert(bad, int, None, 'Age', registry(), {})
    assert _convert('42', int, None, 'Age', registry(), {}) == 42
