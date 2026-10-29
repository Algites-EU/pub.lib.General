from __future__ import annotations
from typing import Any
from eu.algites.lib.common.interval.ain_interval_boundary import AInIntervalBoundary as Boundary

class AIsIntervalUtils:
    @staticmethod
    def validateInterval(aInterval) -> None:
        if aInterval is None: raise ValueError('Interval must not be null')
        lb, rb = aInterval.getLeftBoundary(), aInterval.getRightBoundary()
        if lb is None or rb is None: raise ValueError('Interval boundary must not be null')
        lv, rv = aInterval.getLeftValue(), aInterval.getRightValue()
        if not lb.isBoundaryValueIgnored() and lv is None: raise ValueError('Left value must not be null')
        if not rb.isBoundaryValueIgnored() and rv is None: raise ValueError('Right value must not be null')
        if not lb.isBoundaryValueIgnored() and not rb.isBoundaryValueIgnored():
            if lv > rv: raise ValueError('Invalid interval: left value is greater than right value')
            if lv == rv and (lb.isOpen() or rb.isOpen()):
                raise ValueError('Invalid interval: point interval requires both boundaries to be closed')

    @staticmethod
    def isLeftUnbounded(aInterval) -> bool: return aInterval.getLeftBoundary().isBoundaryValueIgnored()
    @staticmethod
    def isRightUnbounded(aInterval) -> bool: return aInterval.getRightBoundary().isBoundaryValueIgnored()

    @staticmethod
    def contains(aInterval, aValue) -> bool:
        if aValue is None: raise ValueError('Value must not be null')
        if not AIsIntervalUtils.isLeftUnbounded(aInterval):
            if aValue < aInterval.getLeftValue() or (aValue == aInterval.getLeftValue() and aInterval.getLeftBoundary().isOpen()): return False
        if not AIsIntervalUtils.isRightUnbounded(aInterval):
            if aValue > aInterval.getRightValue() or (aValue == aInterval.getRightValue() and aInterval.getRightBoundary().isOpen()): return False
        return True

    @staticmethod
    def _left(aInterval):
        return (aInterval.getLeftBoundary(), aInterval.getLeftValue())

    @staticmethod
    def _right(aInterval):
        return (aInterval.getRightBoundary(), aInterval.getRightValue())

    @staticmethod
    def _cmp(a, b, side):
        ba, va = a
        bb, vb = b
        if ba.isBoundaryValueIgnored(): return 0 if bb.isBoundaryValueIgnored() else (-1 if side == 'left' else 1)
        if bb.isBoundaryValueIgnored(): return 1 if side == 'left' else -1
        if va != vb: return (va > vb) - (va < vb)
        if ba.isOpen() == bb.isOpen(): return 0
        return (1 if ba.isOpen() else -1) if side == 'left' else (-1 if ba.isOpen() else 1)

    @staticmethod
    def compare(aLeft, aRight) -> int:
        c = AIsIntervalUtils._cmp(AIsIntervalUtils._left(aLeft), AIsIntervalUtils._left(aRight), 'left')
        return c if c else AIsIntervalUtils._cmp(AIsIntervalUtils._right(aLeft), AIsIntervalUtils._right(aRight), 'right')

    @staticmethod
    def isStrictlyBefore(aLeft, aRight) -> bool:
        if AIsIntervalUtils.isRightUnbounded(aLeft) or AIsIntervalUtils.isLeftUnbounded(aRight): return False
        lv, rv = aLeft.getRightValue(), aRight.getLeftValue()
        return lv < rv or (lv == rv and (aLeft.getRightBoundary().isOpen() or aRight.getLeftBoundary().isOpen()))

    @staticmethod
    def tryIntersect(aLeft, aRight):
        from eu.algites.lib.common.interval.aic_interval import AIcInterval
        l1, l2 = AIsIntervalUtils._left(aLeft), AIsIntervalUtils._left(aRight)
        r1, r2 = AIsIntervalUtils._right(aLeft), AIsIntervalUtils._right(aRight)
        l = l1 if AIsIntervalUtils._cmp(l1,l2,'left') >= 0 else l2
        r = r1 if AIsIntervalUtils._cmp(r1,r2,'right') <= 0 else r2
        if not l[0].isBoundaryValueIgnored() and not r[0].isBoundaryValueIgnored():
            if l[1] > r[1] or (l[1] == r[1] and (l[0].isOpen() or r[0].isOpen())): return None
        return AIcInterval(l[0],l[1],r[0],r[1])

    @staticmethod
    def intersect(aLeft, aRight):
        result = AIsIntervalUtils.tryIntersect(aLeft, aRight)
        if result is None: raise ValueError('Intervals do not overlap')
        return result

    @staticmethod
    def overlaps(aLeft, aRight) -> bool: return AIsIntervalUtils.tryIntersect(aLeft,aRight) is not None

    @staticmethod
    def isSatisfiable(aInterval) -> bool:
        if AIsIntervalUtils.isLeftUnbounded(aInterval) or AIsIntervalUtils.isRightUnbounded(aInterval): return True
        l, r = aInterval.getLeftValue(), aInterval.getRightValue()
        return l < r or (l == r and not aInterval.getLeftBoundary().isOpen() and not aInterval.getRightBoundary().isOpen())

    @staticmethod
    def toStringRepresentation(aInterval) -> str:
        lb, rb = aInterval.getLeftBoundary(), aInterval.getRightBoundary()
        left = '' if lb.isBoundaryValueIgnored() else str(aInterval.getLeftValue())
        right = '' if rb.isBoundaryValueIgnored() else str(aInterval.getRightValue())
        return f'{lb.getLeftBoundaryStringRepresentation()}{left}, {right}{rb.getRightBoundaryStringRepresentation()}'
