from __future__ import annotations
from typing import Generic, TypeVar
from eu.algites.lib.common.interval.ain_interval_boundary import AInIntervalBoundary
from eu.algites.lib.common.interval.ais_interval_utils import AIsIntervalUtils
T = TypeVar('T')

class AIcInterval(Generic[T]):
    """Immutable interval with the same endpoints and validation as the Java type."""
    __slots__ = ('_left_boundary', '_left_value', '_right_boundary', '_right_value', '_frozen')

    def __init__(self, aLeftBoundary: AInIntervalBoundary, aLeftValue: T | None,
                 aRightBoundary: AInIntervalBoundary, aRightValue: T | None):
        object.__setattr__(self, '_left_boundary', aLeftBoundary)
        object.__setattr__(self, '_left_value', aLeftValue)
        object.__setattr__(self, '_right_boundary', aRightBoundary)
        object.__setattr__(self, '_right_value', aRightValue)
        AIsIntervalUtils.validateInterval(self)
        object.__setattr__(self, '_frozen', True)

    def __setattr__(self, aName, aValue):
        if getattr(self, '_frozen', False): raise AttributeError('Interval is immutable')
        object.__setattr__(self, aName, aValue)

    def getLeftBoundary(self) -> AInIntervalBoundary: return self._left_boundary
    def getLeftValue(self) -> T | None: return self._left_value
    def getRightBoundary(self) -> AInIntervalBoundary: return self._right_boundary
    def getRightValue(self) -> T | None: return self._right_value
    def __str__(self) -> str: return AIsIntervalUtils.toStringRepresentation(self)
    def __repr__(self) -> str: return f'AIcInterval({self._left_boundary!r}, {self._left_value!r}, {self._right_boundary!r}, {self._right_value!r})'
    def __eq__(self, aOther: object) -> bool:
        return (hasattr(aOther, 'getLeftBoundary') and self._left_boundary == aOther.getLeftBoundary()
                and self._left_value == aOther.getLeftValue() and self._right_boundary == aOther.getRightBoundary()
                and self._right_value == aOther.getRightValue())
    def __hash__(self): return hash((self._left_boundary, self._left_value, self._right_boundary, self._right_value))
