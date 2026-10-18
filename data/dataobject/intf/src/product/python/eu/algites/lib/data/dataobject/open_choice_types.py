"""Explicit OpenChoice participation, with lazy, class-keyed contract metadata."""
from __future__ import annotations

from threading import RLock
from weakref import WeakKeyDictionary

_ALLOWED_HEADS: WeakKeyDictionary[type, frozenset[type]] = WeakKeyDictionary()
_LOCK = RLock()


def open_choice_type_of(parent: type):
    """Mark an interface as a direct permitted substitution for *parent*."""
    def decorate(contract: type) -> type:
        if not issubclass(contract, parent) or contract is parent:
            raise TypeError('OpenChoice contract must be a strict subclass of its head')
        edges = contract.__dict__.get('__open_choice_type_of__', ())
        setattr(contract, '__open_choice_type_of__', (*edges, parent))
        with _LOCK:
            _ALLOWED_HEADS.clear()
        return contract
    return decorate


def allowed_heads(contract: type) -> frozenset[type]:
    """Calculate transitive *explicit* parent edges, not arbitrary subclass relations."""
    def visit(node: type, active: set[type]) -> set[type]:
        if node in active:
            raise ValueError('OpenChoice substitution cycle')
        result = set()
        for parent in node.__dict__.get('__open_choice_type_of__', ()):
            if parent is node or not issubclass(node, parent):
                raise TypeError('Invalid OpenChoice substitution')
            result.add(parent)
            result.update(visit(parent, active | {node}))
        return result
    with _LOCK:
        if contract not in _ALLOWED_HEADS:
            _ALLOWED_HEADS[contract] = frozenset(visit(contract, set()))
        return _ALLOWED_HEADS[contract]


def permits(value: object, head: type) -> bool:
    """Verify that the most specific declared domain contract permits the given head."""
    if not isinstance(value, head):
        return False
    if type(value) is head:
        return True
    contracts = {c for c in type(value).__mro__ if c.__dict__.get('__data_object__') is not None}
    if not contracts:
        contracts = {c for c in type(value).__mro__ if c.__dict__.get('__open_choice_type_of__')}
    leaves = (c for c in contracts if not any(other is not c and issubclass(other, c)
                                              for other in contracts))
    def effective_contract(contract: type) -> type:
        meta = contract.__dict__.get('__data_object__')
        source = getattr(meta, 'source_contract', None)
        if source is not None:
            if not issubclass(contract, source):
                raise TypeError('Invalid source contract for ' + contract.__name__)
            return source
        return contract
    return any((effective_contract(c) is head or
                head in allowed_heads(effective_contract(c))) for c in leaves)
