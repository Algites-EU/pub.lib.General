from __future__ import annotations
from collections.abc import Iterable, MutableSequence, Callable
from eu.algites.lib.common.interval.ais_interval_utils import AIsIntervalUtils
from eu.algites.lib.common.list.ain_bounds_mutation_policy import AInBoundsMutationPolicy
from eu.algites.lib.common.validation.aii_validatable import AIiValidatable
from eu.algites.lib.common.list.aicd_bounded_list_validation_issue import AIcdBoundedListValidationIssue
T = __import__('typing').TypeVar('T')

class AIcBoundedList(MutableSequence[T], AIiValidatable[AIcdBoundedListValidationIssue]):
    """Bounded mutable list. Every size change is transactional; callbacks edit a detached copy.

    Constructor accepts (min_size, max_size[, policy]) or (AIiInterval[, policy]).
    Python integers are checked against the Java 32-bit list-size domain for parity.
    """
    def __init__(self, aMinSize=None, aMaxSize=None,
                 aPolicy=AInBoundsMutationPolicy.PREVENT_WORSENING):
        if hasattr(aMinSize, 'getLeftBoundary'):
            interval = aMinSize
            if isinstance(aMaxSize, AInBoundsMutationPolicy): aPolicy = aMaxSize
            AIsIntervalUtils.validateInterval(interval)
            self._min_size = (None if interval.getLeftBoundary().isBoundaryValueIgnored() else
                              interval.getLeftValue() + int(interval.getLeftBoundary().isOpen()))
            self._max_size = (None if interval.getRightBoundary().isBoundaryValueIgnored() else
                              interval.getRightValue() - int(interval.getRightBoundary().isOpen()))
        else:
            self._min_size, self._max_size = aMinSize, aMaxSize
        self._policy = AInBoundsMutationPolicy(aPolicy)
        for value in (self._min_size, self._max_size):
            if value is not None and (type(value) is not int or not 0 <= value <= 2_147_483_647):
                raise ValueError('Bounds must be nonnegative signed 32-bit integers or None')
        if self._min_size is not None and self._max_size is not None and self._min_size > self._max_size:
            raise ValueError('Minimum size exceeds maximum size')
        self._values: list[T] = []

    def getMinSize(self) -> int | None: return self._min_size
    def getMaxSize(self) -> int | None: return self._max_size
    def getMutationPolicy(self) -> AInBoundsMutationPolicy: return self._policy
    def __len__(self): return len(self._values)
    def __getitem__(self, index): return self._values[index]
    def __iter__(self): return iter(self._values)
    def __setitem__(self, index, value):
        self.modifyAtomically(lambda items: items.__setitem__(index, value))
    def __delitem__(self, index):
        self.modifyAtomically(lambda items: items.__delitem__(index))
    def insert(self, index: int, value: T):
        self.modifyAtomically(lambda items: items.insert(index,value))
    def _below(self, n): return max(0, self._min_size-n) if self._min_size is not None else 0
    def _above(self, n): return max(0, n-self._max_size) if self._max_size is not None else 0

    def modifyAtomically(self, aChange: Callable[[list[T]], object]) -> None:
        if aChange is None: raise ValueError('Change callback is required')
        candidate = list(self._values)
        aChange(candidate)
        if len(candidate) > 2_147_483_647: raise ValueError('List exceeds Java-compatible size')
        if self._policy is AInBoundsMutationPolicy.PREVENT_WORSENING:
            if self._below(len(candidate)) > self._below(len(self)) or self._above(len(candidate)) > self._above(len(self)):
                raise ValueError('List mutation increases a cardinality constraint violation')
        self._values[:] = candidate

    def get(self, aIndex: int) -> T: return self[aIndex]
    def set(self, aIndex: int, aElement: T) -> T:
        previous = self[aIndex]
        self[aIndex] = aElement
        return previous
    def removeAt(self, aIndex: int) -> T:
        previous = self[aIndex]
        del self[aIndex]
        return previous
    def add(self, *args) -> None:
        if len(args) == 1: self.append(args[0])
        elif len(args) == 2: self.insert(args[0], args[1])
        else: raise TypeError('add expects an element or an index and element')
    def addAll(self, *args) -> bool:
        if len(args) == 1: index, items = len(self), args[0]
        elif len(args) == 2: index, items = args
        else: raise TypeError('addAll expects items or an index and items')
        frozen = list(items)
        if not frozen: return False
        self.modifyAtomically(lambda data: data.__setitem__(slice(index,index),frozen))
        return True
    def extend(self, aItems: Iterable[T]) -> None:
        self.addAll(aItems)
    def __iadd__(self, aItems: Iterable[T]):
        self.addAll(aItems)
        return self
    def removeAll(self, items: Iterable[T]) -> bool:
        targets = list(items)
        count = len(self)
        self.modifyAtomically(lambda data: data.__setitem__(slice(None), [v for v in data if v not in targets]))
        return len(self) != count
    def retainAll(self, items: Iterable[T]) -> bool:
        targets = list(items)
        count = len(self)
        self.modifyAtomically(lambda data: data.__setitem__(slice(None), [v for v in data if v in targets]))
        return len(self) != count
    def removeIf(self, pred: Callable[[T],bool]) -> bool:
        count = len(self)
        self.modifyAtomically(lambda data: data.__setitem__(slice(None), [v for v in data if not pred(v)]))
        return len(self) != count
    def clear(self) -> None: self.modifyAtomically(lambda items: items.clear())
    def isValid(self) -> bool: return self._below(len(self)) == 0 and self._above(len(self)) == 0
    def getValidationIssues(self) -> list[AIcdBoundedListValidationIssue]:
        if self._below(len(self)):
            return [AIcdBoundedListValidationIssue('MIN_ITEMS', len(self), self._min_size)]
        if self._above(len(self)):
            return [AIcdBoundedListValidationIssue('MAX_ITEMS', len(self), self._max_size)]
        return []

    def getInvalidStateMessage(self, aLocale: str | None) -> str:
        return super().getInvalidStateMessage(aLocale)
    def __repr__(self): return f'AIcBoundedList({self._min_size}, {self._max_size}, {self._policy!r}): {self._values!r}'
