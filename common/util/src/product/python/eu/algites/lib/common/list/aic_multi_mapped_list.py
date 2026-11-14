"""Ordered list with cached live, multi-key map views and tracked membership views.

Author: linhart1.

All public mutations are performed on a validated candidate before publication.
External mutation of a stored value's key properties violates the caller contract.
Neither this implementation nor its Java counterpart is thread-safe.
"""

from __future__ import annotations

import weakref
from collections import defaultdict
from collections.abc import Callable, Iterable, Iterator, Mapping, MutableMapping, MutableSequence
from typing import Generic, TypeVar, cast, overload

from .aii_key_definition import AIiKeyDefinition
from .aii_multi_mapped_list import AIiMultiMappedList
from .ain_bounds_mutation_policy import AInBoundsMutationPolicy
from .aicd_bounded_list_validation_issue import AIcdBoundedListValidationIssue

V = TypeVar("V")
K = TypeVar("K")


class _Entry(Generic[V]):
    __slots__ = ("value",)

    def __init__(self, value: V) -> None:
        if value is None:
            raise ValueError("Null values cannot be inserted")
        self.value = value


class _KeyMap(MutableMapping[K, tuple[V, ...]], Generic[V, K]):
    """Live key map with snapshot tuple values and deletion-only mutation."""

    def __init__(self, owner: AIcMultiMappedList[V], definition: AIiKeyDefinition[V, K]) -> None:
        self._owner = owner
        self._definition = definition

    def _groups(self) -> dict[K, list[_Entry[V]]]:
        if self._owner._by_id.get(id(self._definition)) is not self._definition:
            raise ValueError("This key definition is no longer registered")
        return cast(dict[K, list[_Entry[V]]], self._owner._groups[id(self._definition)])

    def __getitem__(self, key: K) -> tuple[V, ...]:
        return tuple(entry.value for entry in self._groups()[key])

    def __iter__(self) -> Iterator[K]:
        return iter(tuple(self._groups()))

    def __len__(self) -> int:
        return len(self._groups())

    def __setitem__(self, key: K, values: tuple[V, ...]) -> None:
        raise TypeError("Insert values through the parent list, not a key map")

    def __delitem__(self, key: K) -> None:
        entries = self._groups()[key]
        remove = set(entries)
        self._owner._publish([item for item in self._owner._items if item not in remove])

    def clear(self) -> None:
        """Remove all values from the owning list and every map.

        Returns:
            ``None``.
        """
        self._groups()  # reject a stale map view after deregistration
        self._owner.clear()


class _SubList(MutableSequence[V], Generic[V]):
    """Weakly tracked backed member set, distinct from a positional slice."""

    def __init__(self, owner: AIcMultiMappedList[V], members: list[_Entry[V]], hint: int) -> None:
        self._owner = owner
        self._members = members
        self._hint = hint

    def _synchronize(self, surviving: set[_Entry[V]], replacements: Mapping[_Entry[V], _Entry[V]]) -> None:
        self._members = [replacements.get(item, item) for item in self._members
                         if item in surviving or item in replacements]

    def __len__(self) -> int:
        return len(self._members)

    @overload
    def __getitem__(self, index: int) -> V: ...

    @overload
    def __getitem__(self, index: slice) -> list[V]: ...

    def __getitem__(self, index: int | slice) -> V | list[V]:
        if isinstance(index, slice):
            return [item.value for item in self._members[index]]
        return self._members[index].value

    def __setitem__(self, index: int | slice, value: V | Iterable[V]) -> None:
        if isinstance(index, slice):
            raise TypeError("Slice assignment is not supported; use replace_all on the parent")
        original = self._members[index]
        self._owner._replace_entry(original, cast(V, value))

    def __delitem__(self, index: int | slice) -> None:
        victims = self._members[index] if isinstance(index, slice) else [self._members[index]]
        removed = set(victims)
        if removed:
            self._owner._publish([item for item in self._owner._items if item not in removed])

    def insert(self, index: int, value: V) -> None:
        """Insert an entry into the parent and only this view's membership.

        Args:
            index: Position in the membership view (clamped like list.insert).
            value: Non-None stored value with valid keys.

        Returns:
            ``None``.

        Raises:
            ValueError: If the value is None or a unique key conflicts.
        """
        index = max(0, min(index if index >= 0 else len(self) + index, len(self)))
        if index < len(self):
            source_index = self._owner._position(self._members[index])
        elif self._members:
            source_index = self._owner._position(self._members[-1]) + 1
        else:
            source_index = min(self._hint, len(self._owner))
        created = _Entry(value)
        candidate = self._owner._items.copy()
        candidate.insert(source_index, created)
        self._owner._publish(candidate)
        self._members.insert(index, created)

    def sub_list(self, start: int, end: int) -> _SubList[V]:
        """Create a nested live membership view for a half-open range.

        Args:
            start: Inclusive zero-based position within this view.
            end: Exclusive zero-based position within this view.

        Returns:
            Weakly registered nested sublist with independent membership.

        Raises:
            IndexError: If the range is invalid.
        """
        if not (0 <= start <= end <= len(self)):
            raise IndexError("Invalid sublist bounds")
        if start < len(self):
            hint = self._owner._position(self._members[start])
        elif self._members:
            hint = self._owner._position(self._members[-1]) + 1
        else:
            hint = self._hint
        return self._owner._track(self._members[start:end], hint)


class AIcMultiMappedList(AIiMultiMappedList[V], Generic[V]):
    """Mutable, order-preserving list with many live key maps.

    Args:
        definitions: Registered key definitions; ids must be distinct and
            definitions are looked up using object identity.
        values: Initial values. Insertion is atomic across all definitions.

    Raises:
        ValueError: For repeated definition ids or conflicting unique keys.
        TypeError: If a definition is unusable or a key is unhashable.

    Map views permit deletion only and expose immutable tuple snapshots.
    Sublist views retain current membership across parent insertions and reflect
    removals from any path. Values are shared by reference; external edits to
    key attributes after insertion are unsupported.
    """

    def __init__(self, definitions: Iterable[AIiKeyDefinition[V, object]] | None = None,
                 values: Iterable[V] = (), *, dynamic_key_defs: bool | None = None,
                 min_size: int | None = None, max_size: int | None = None,
                 mutation_policy: AInBoundsMutationPolicy = AInBoundsMutationPolicy.PREVENT_WORSENING) -> None:
        for bound in (min_size, max_size):
            if bound is not None and (type(bound) is not int or not 0 <= bound <= 2_147_483_647):
                raise ValueError('Bounds must be nonnegative signed 32-bit integers or None')
        if min_size is not None and max_size is not None and min_size > max_size:
            raise ValueError('Minimum size exceeds maximum size')
        self._min_size, self._max_size = min_size, max_size
        self._policy = AInBoundsMutationPolicy(mutation_policy)
        # Legacy explicit-definition calls stay fixed until the migration deploy.
        # Zero-argument construction is dynamic, as is explicit dynamic_key_defs=True.
        self._dynamic_key_defs = (definitions is None if dynamic_key_defs is None
                                  else bool(dynamic_key_defs))
        self._definitions = tuple(definitions or ())
        self._by_id: dict[int, AIiKeyDefinition[V, object]] = {}
        used_names: set[str] = set()
        for definition in self._definitions:
            if not definition.id or definition.id in used_names or id(definition) in self._by_id:
                raise ValueError(f"Repeated/blank key definition: {definition.id}")
            used_names.add(definition.id)
            self._by_id[id(definition)] = definition
        self._items: list[_Entry[V]] = []
        self._groups: dict[int, dict[object, list[_Entry[V]]]] = {
            id(definition): {} for definition in self._definitions
        }
        self._maps: dict[int, _KeyMap[V, object]] = {}
        self._views: weakref.WeakSet[_SubList[V]] = weakref.WeakSet()
        initial = [_Entry(value) for value in values]
        if initial:
            self._publish(initial)

    def getMinSize(self) -> int | None: return self._min_size
    def getMaxSize(self) -> int | None: return self._max_size
    def getMutationPolicy(self) -> AInBoundsMutationPolicy: return self._policy
    def _below(self, count: int) -> int: return max(0, self._min_size - count) if self._min_size is not None else 0
    def _above(self, count: int) -> int: return max(0, count - self._max_size) if self._max_size is not None else 0
    def isValid(self) -> bool: return self._below(len(self)) == 0 and self._above(len(self)) == 0
    def getValidationIssues(self) -> list[AIcdBoundedListValidationIssue]:
        if self._below(len(self)):
            return [AIcdBoundedListValidationIssue('MIN_ITEMS', len(self), self._min_size)]
        if self._above(len(self)):
            return [AIcdBoundedListValidationIssue('MAX_ITEMS', len(self), self._max_size)]
        return []
    def modifyAtomically(self, aChange: Callable[[list[V]], object]) -> None:
        if aChange is None: raise ValueError('Change callback is required')
        candidate_values = [entry.value for entry in self._items]
        aChange(candidate_values)
        unused = list(self._items)
        candidate = []
        for value in candidate_values:
            old = next((entry for entry in unused if entry.value is value), None)
            if old is not None:
                unused.remove(old)
            candidate.append(old if old is not None else _Entry(value))
        if candidate != self._items:
            self._publish(candidate)

    def key_definition(self, identifier: str) -> AIiKeyDefinition[V, object] | None:
        """Return the exact registered key-definition object for ``identifier``.

        Args:
            identifier: Identity name from the generated XML or other schema.

        Returns:
            Registered definition or None; returned object must be used unchanged
            for a cached map-by-key lookup.

        Raises:
            TypeError: If identifier is not a string.
        """
        if not isinstance(identifier, str):
            raise TypeError('Index identifier must be text')
        return next((definition for definition in self._definitions
                     if definition.id == identifier), None)

    def get_key_count(self) -> int:
        """Return the number of registered definitions, not the number of key values."""
        return len(self._definitions)

    def is_one_key_list(self) -> bool:
        """Return whether precisely one key definition is registered."""
        return len(self._definitions) == 1

    def is_dynamic_key_defs_list(self) -> bool:
        """Return whether definition registration/removal is permitted."""
        return self._dynamic_key_defs

    def get_one_key_map(self) -> MutableMapping[object, tuple[V, ...]]:
        """Return the sole map (keys to immutable value groups), or raise ValueError."""
        if not self.is_one_key_list():
            raise ValueError('Exactly one key definition is required')
        return self.map_by_key_type(self._definitions[0])

    def map_by_key_id(self, identifier: str) -> MutableMapping[object, tuple[V, ...]]:
        """Return the map registered under identifier, or raise ValueError."""
        definition = self.key_definition(identifier)
        if definition is None:
            raise ValueError(f'Unregistered key definition: {identifier}')
        return self.map_by_key_type(definition)

    def _require_dynamic(self) -> None:
        if not self._dynamic_key_defs:
            raise ValueError('Key definitions are fixed')

    def add_key_definition(self, definition: AIiKeyDefinition[V, object]) -> None:
        """Atomically register a definition, validating all existing values first.

        Raises:
            ValueError: Fixed mode, invalid/duplicate definition or invalid keys.
            TypeError: The definition/extractor is invalid.
        """
        self._require_dynamic()
        if definition is None or not isinstance(definition.id, str) or not definition.id.strip():
            raise ValueError('A key definition needs a nonblank id')
        if definition.id in (d.id for d in self._definitions) or id(definition) in self._by_id:
            raise ValueError(f'Repeated key definition: {definition.id}')
        # Build on a temporary definition tuple; do not publish partial state.
        old_definitions = self._definitions
        self._definitions += (definition,)
        try:
            new_groups = self._build(self._items)
        except BaseException:
            self._definitions = old_definitions
            raise
        self._by_id[id(definition)] = definition
        self._groups = new_groups

    def remove_key_definition(self, identifier: str) -> bool:
        """Unregister a key by id without changing values; invalidate its map view."""
        self._require_dynamic()
        if not isinstance(identifier, str):
            raise TypeError('Index identifier must be text')
        definition = self.key_definition(identifier)
        if definition is None:
            return False
        self._definitions = tuple(d for d in self._definitions if d is not definition)
        self._by_id.pop(id(definition))
        self._maps.pop(id(definition), None)
        self._groups.pop(id(definition), None)
        return True

    def clear_key_definitions(self) -> None:
        """Unregister all key definitions, invalidate map views, and retain values."""
        self._require_dynamic()
        self._definitions = ()
        self._by_id.clear()
        self._maps.clear()
        self._groups.clear()

    # Explicit Java-style aliases preserve cross-language generated/API parity.
    getKeyCount = get_key_count
    isOneKeyList = is_one_key_list
    isDynamicKeyDefsList = is_dynamic_key_defs_list
    getOneKeyMap = get_one_key_map
    mapByKeyId = map_by_key_id
    addKeyDefinition = add_key_definition
    removeKeyDefinition = remove_key_definition
    clearKeyDefinitions = clear_key_definitions

    def _build(self, candidate: list[_Entry[V]]) -> dict[int, dict[object, list[_Entry[V]]]]:
        result: dict[int, dict[object, list[_Entry[V]]]] = {}
        for definition in self._definitions:
            groups: dict[object, list[_Entry[V]]] = defaultdict(list)
            for entry in candidate:
                key = definition.extract_key(entry.value)
                if key is None and definition.required:
                    raise ValueError(f"Missing required key from {definition.id}")
                group = groups[key]
                if key is not None and definition.unique and group:
                    raise ValueError(f"Duplicate unique key for {definition.id}: {key!r}")
                group.append(entry)
            result[id(definition)] = dict(groups)
        return result

    def _publish(self, candidate: list[_Entry[V]],
                 replacements: Mapping[_Entry[V], _Entry[V]] | None = None) -> None:
        if self._policy is AInBoundsMutationPolicy.PREVENT_WORSENING:
            if self._below(len(candidate)) > self._below(len(self)) or self._above(len(candidate)) > self._above(len(self)):
                raise ValueError('List mutation increases a cardinality constraint violation')
        indexed = self._build(candidate)
        self._items = candidate
        self._groups = indexed
        surviving = set(candidate)
        for view in tuple(self._views):
            view._synchronize(surviving, replacements or {})

    def _position(self, target: _Entry[V]) -> int:
        for index, entry in enumerate(self._items):
            if entry is target:
                return index
        raise ValueError("Entry no longer belongs to this list")

    def _replace_entry(self, target: _Entry[V], value: V) -> None:
        replacement = _Entry(value)
        candidate = self._items.copy()
        candidate[self._position(target)] = replacement
        self._publish(candidate, {target: replacement})

    def _track(self, members: list[_Entry[V]], hint: int) -> _SubList[V]:
        view = _SubList(self, members, hint)
        self._views.add(view)
        return view

    def __len__(self) -> int:
        return len(self._items)

    @overload
    def __getitem__(self, index: int) -> V: ...

    @overload
    def __getitem__(self, index: slice) -> list[V]: ...

    def __getitem__(self, index: int | slice) -> V | list[V]:
        if isinstance(index, slice):
            return [entry.value for entry in self._items[index]]
        return self._items[index].value

    def __setitem__(self, index: int | slice, value: V | Iterable[V]) -> None:
        if isinstance(index, slice):
            raise TypeError("Slice assignment unsupported; use replace_all")
        self._replace_entry(self._items[index], cast(V, value))

    def __delitem__(self, index: int | slice) -> None:
        candidate = self._items.copy()
        del candidate[index]
        if len(candidate) != len(self._items):
            self._publish(candidate)

    def insert(self, index: int, value: V) -> None:
        """Insert a value into the ordered list and every registered index.

        Args:
            index: List insertion position; negative and overflowing positions
                are clamped according to Python list semantics.
            value: Non-None value; key properties must stay stable after insertion.

        Returns:
            ``None``.

        Raises:
            ValueError: If any extracted unique key conflicts or is None.
            TypeError: If a key is unhashable or the extractor rejects the value.
        """
        candidate = self._items.copy()
        candidate.insert(index, _Entry(value))
        self._publish(candidate)

    def extend(self, values: Iterable[V]) -> None:
        """Append values atomically after validating every registered index.

        Args:
            values: Iterable of non-None values to append in iteration order.

        Returns:
            ``None``.

        Raises:
            ValueError: If any key conflicts or value is None.
            Exception: Any exception raised by an extractor.
        """
        additions = [_Entry(value) for value in values]
        if additions:
            self._publish(self._items + additions)

    def clear(self) -> None:
        """Remove all values from the list, maps and active membership views.

        Returns:
            ``None``.
        """
        if self._items:
            self._publish([])

    def map_by_key_type(self, definition: AIiKeyDefinition[V, K]) -> MutableMapping[K, tuple[V, ...]]:
        """Return a cached, deletion-only live map for a registered key type.

        Args:
            definition: The exact key definition instance registered in constructor.

        Returns:
            Same map object on successive calls, mapping keys to tuple snapshots.

        Raises:
            ValueError: If the definition is not registered with this list.
        """
        token = id(definition)
        if self._by_id.get(token) is not definition:
            raise ValueError(f"Unregistered key definition: {definition.id}")
        if token not in self._maps:
            self._maps[token] = _KeyMap(self, definition)
        return cast(MutableMapping[K, tuple[V, ...]], self._maps[token])

    def sub_list(self, start: int, end: int) -> MutableSequence[V]:
        """Create a tracked backed view of current members within a range.

        Args:
            start: Inclusive nonnegative position in the current list.
            end: Exclusive nonnegative position in the current list.

        Returns:
            Membership-based view, not an ordinary copy or positional slice.

        Raises:
            IndexError: If bounds fall outside the list or are reversed.
        """
        if not (0 <= start <= end <= len(self)):
            raise IndexError("Invalid sublist bounds")
        return self._track(self._items[start:end], start)

    def copy_range(self, start: int, end: int) -> AIcMultiMappedList[V]:
        """Create a detached shallow copy of a half-open positional range.

        Args:
            start: Inclusive range position.
            end: Exclusive range position.

        Returns:
            Independently mutable list with the same key definitions and value
            objects, but independent membership and map indexes.

        Raises:
            IndexError: If the range is invalid.
        """
        if not (0 <= start <= end <= len(self)):
            raise IndexError("Invalid copy range")
        return AIcMultiMappedList(self._definitions, self[start:end], min_size=self._min_size,
                                  max_size=self._max_size, mutation_policy=self._policy,
                                  dynamic_key_defs=self._dynamic_key_defs)

    def remove_if(self, predicate: Callable[[V], bool]) -> bool:
        """Remove all matching entries as a single atomic mutation.

        Args:
            predicate: Callable deciding whether each element should be removed.

        Returns:
            True if any occurrence was removed, otherwise False.

        Raises:
            Exception: Predicate exception; the original list is preserved.
        """
        candidate = [entry for entry in self._items if not predicate(entry.value)]
        if len(candidate) == len(self._items):
            return False
        self._publish(candidate)
        return True

    def replace_all(self, operator: Callable[[V], V]) -> None:
        """Atomically replace every value, preserving tracked membership tokens.

        Args:
            operator: Pure transformation producing non-None replacement values.

        Returns:
            ``None``.

        Raises:
            ValueError: If resulting unique key constraints are violated.
            Exception: Transformation or extraction error; no partial change.
        """
        replacement = {entry: _Entry(operator(entry.value)) for entry in self._items}
        if replacement:
            self._publish([replacement[entry] for entry in self._items], replacement)

    def sort(self, *, key: Callable[[V], object] | None = None, reverse: bool = False) -> None:
        """Sort parent occurrences, keeping each active view's existing members.

        Args:
            key: Optional comparison key extractor, defaulting to values themselves.
            reverse: Whether to sort in descending order.

        Returns:
            ``None``.

        Raises:
            TypeError: If selected values cannot be ordered.
        """
        candidate = sorted(self._items, key=lambda entry: key(entry.value) if key else entry.value,
                           reverse=reverse)
        if any(before is not after for before, after in zip(candidate, self._items)):
            self._publish(candidate)
