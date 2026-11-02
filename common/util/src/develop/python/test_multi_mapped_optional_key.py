"""Tests for optional and required indexed keys in MultiMappedList.

Author: linhart1.
"""

from dataclasses import dataclass

import pytest

from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList


@dataclass(frozen=True)
class Employee:
    """One immutable test value with optional composite identity."""

    identifier: int
    department: str | None
    code: int | None


def test_optional_composite_key_groups_absent_values_without_uniqueness_collision():
    """Incomplete tuples group under None; only complete tuples are unique."""
    definition = AIcKeyDefinition('ByDepartmentCode',
        lambda employee: ((employee.department, employee.code)
            if employee.department is not None and employee.code is not None else None),
        unique=True, required=False)
    employees = AIcMultiMappedList([definition], [
        Employee(1, 'Sales', None), Employee(2, None, 10),
        Employee(3, 'Sales', 10),
    ])
    by_key = employees.map_by_key_type(definition)
    assert [employee.identifier for employee in by_key[None]] == [1, 2]
    assert by_key[('Sales', 10)][0].identifier == 3
    with pytest.raises(ValueError, match='Duplicate unique key'):
        employees.append(Employee(4, 'Sales', 10))
    assert len(employees) == 3
    del by_key[None]
    assert [employee.identifier for employee in employees] == [3]


def test_required_key_rejects_missing_and_keeps_existing_data():
    """A missing required key fails atomically, including bulk operations."""
    definition = AIcKeyDefinition('Required', lambda employee: employee.code, True)
    employees = AIcMultiMappedList([definition], [Employee(1, None, 1)])
    with pytest.raises(ValueError, match='Missing required key'):
        employees.extend([Employee(2, None, 2), Employee(3, None, None)])
    assert [employee.identifier for employee in employees] == [1]
