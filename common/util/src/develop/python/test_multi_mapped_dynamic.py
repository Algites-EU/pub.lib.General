"""Regression tests for runtime reconfiguration of key definitions."""
import pytest
from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition


def test_dynamic_definitions_atomic_views_and_copies():
    group = AIcKeyDefinition('GROUP', lambda item: item[0], False)
    unique = AIcKeyDefinition('UNIQUE', lambda item: item[1], True)
    collection = AIcMultiMappedList()
    assert collection.is_dynamic_key_defs_list()
    assert collection.get_key_count() == 0
    collection.extend([('a', 1), ('a', 2)])
    collection.add_key_definition(group)
    assert collection.is_one_key_list()
    view = collection.get_one_key_map()
    assert view['a'] == (('a', 1), ('a', 2))
    assert view is collection.map_by_key_id('GROUP')
    collection.add_key_definition(unique)
    assert collection.get_key_count() == 2
    with pytest.raises(ValueError):
        collection.get_one_key_map()
    with pytest.raises(ValueError):
        collection.add_key_definition(AIcKeyDefinition('DUP', lambda _: 0, True))
    assert collection.get_key_count() == 2
    assert collection.remove_key_definition('GROUP')
    with pytest.raises(ValueError):
        _ = view['a']
    assert collection.is_one_key_list()
    detached = collection.copy_range(0, 1)
    assert detached.is_dynamic_key_defs_list()
    assert detached.get_key_count() == 1
    collection.clear_key_definitions()
    assert collection.get_key_count() == 0
    assert len(collection) == 2


def test_fixed_definitions_reject_edits():
    group = AIcKeyDefinition('GROUP', lambda item: item, True)
    fixed = AIcMultiMappedList((group,), dynamic_key_defs=False)
    assert not fixed.is_dynamic_key_defs_list()
    with pytest.raises(ValueError):
        fixed.add_key_definition(AIcKeyDefinition('NEW', lambda item: item, False))
    with pytest.raises(ValueError):
        fixed.remove_key_definition('GROUP')
    with pytest.raises(ValueError):
        fixed.clear_key_definitions()
