"""Regressions for list-backed multi-map behavior and weakly tracked views.

Author: linhart1.
"""

from dataclasses import dataclass

import pytest

from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList


@dataclass(frozen=True)
class Employee:
    id: int
    email: str
    city: str


A = Employee(1, "a@example.org", "Prague")
B = Employee(2, "b@example.org", "Brno")
C = Employee(3, "c@example.org", "Prague")
D = Employee(4, "d@example.org", "Prague")


def _fixture():
    by_id = AIcKeyDefinition("BY_ID", lambda e: e.id, True)
    by_email = AIcKeyDefinition("BY_EMAIL", lambda e: e.email, True)
    by_city = AIcKeyDefinition("BY_CITY", lambda e: e.city, False)
    by_composite = AIcKeyDefinition("BY_COMPOSITE", lambda e: (e.id, e.city), True)
    return AIcMultiMappedList((by_id, by_email, by_city, by_composite)), by_id, by_email, by_city, by_composite


def test_cached_live_map_and_snapshot_tuple():
    employees, by_id, _, by_city, _ = _fixture()
    cities = employees.map_by_key_type(by_city)
    assert cities is employees.map_by_key_type(by_city)
    employees.extend((A, B, C))
    snapshot = cities["Prague"]
    assert snapshot == (A, C)
    employees.append(D)
    assert snapshot == (A, C)
    assert cities["Prague"] == (A, C, D)
    with pytest.raises(TypeError):
        cities["Prague"] = (B,)
    with pytest.raises(ValueError):
        employees.map_by_key_type(AIcKeyDefinition("BY_ID", lambda e: e.id, True))
    assert employees.map_by_key_type(by_id)[1] == (A,)


def test_group_remove_propagates_everywhere():
    employees, by_id, _, by_city, _ = _fixture()
    employees.extend((A, B, C, D))
    part = employees.sub_list(0, 3)
    cities = employees.map_by_key_type(by_city)
    del cities["Prague"]
    assert list(employees) == [B]
    assert list(part) == [B]
    assert dict(employees.map_by_key_type(by_id)) == {2: (B,)}
    employees.append(A)
    assert list(part) == [B]
    cities.clear()
    assert len(employees) == 0


def test_unique_collisions_are_atomic():
    employees, _, by_email, _, _ = _fixture()
    employees.extend((A, B))
    duplicate = Employee(5, B.email, "Prague")
    with pytest.raises(ValueError):
        employees.extend((C, duplicate))
    with pytest.raises(ValueError):
        employees[0] = duplicate
    assert list(employees) == [A, B]
    assert employees.map_by_key_type(by_email)[B.email] == (B,)


def test_weak_tracked_nested_views_and_member_insert():
    employees, _, _, _, _ = _fixture()
    employees.extend((A, B, C))
    first = employees.sub_list(0, 2)
    second = first.sub_list(1, 2)
    employees.insert(1, D)
    assert list(first) == [A, B]
    assert list(second) == [B]
    with pytest.raises(ValueError):
        first.insert(0, C)
    E = Employee(5, "e@example.org", "Prague")
    first.insert(1, E)
    assert list(first) == [A, E, B]
    employees.remove(B)
    assert list(first) == [A, E]
    assert list(second) == []
    F = Employee(6, "f@example.org", "Brno")
    second.insert(0, F)
    assert list(second) == [F]
    assert F in employees


def test_composite_keys_detached_copy_and_bulk_replace():
    employees, _, _, by_city, by_composite = _fixture()
    employees.extend((C, B, A))
    sub = employees.sub_list(0, 2)
    employees.sort(key=lambda e: e.id)
    assert list(employees) == [A, B, C]
    assert C in sub and B in sub
    copy = employees.copy_range(0, 2)
    del copy[0]
    assert list(employees) == [A, B, C]
    assert employees.map_by_key_type(by_composite)[(1, "Prague")] == (A,)
    employees.replace_all(lambda e: Employee(e.id, e.email, "Elsewhere"))
    assert len(employees.map_by_key_type(by_city)["Elsewhere"]) == 3
    assert len(sub) == 2 and all(e.city == "Elsewhere" for e in sub)


def test_validation_of_definitions_and_unhashable_key():
    with pytest.raises(ValueError):
        AIcKeyDefinition(" ", lambda e: e.id, False)
    duplicate_a = AIcKeyDefinition("REPEATED", lambda e: e.id, False)
    duplicate_b = AIcKeyDefinition("REPEATED", lambda e: e.id, False)
    with pytest.raises(ValueError):
        AIcMultiMappedList((duplicate_a, duplicate_b))
    unhashable = AIcKeyDefinition("BY_LIST", lambda e: [e.id], False)
    employees = AIcMultiMappedList((unhashable,))
    with pytest.raises(TypeError):
        employees.append(A)
    assert len(employees) == 0
