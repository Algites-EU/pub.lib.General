"""Unbound xs:any element modeled as an XML data object, not a primitive string."""
from __future__ import annotations
from functools import lru_cache
from .aii_xml_data_object import AIiXmlDataObject


class AIcXmlWildcardFragment(AIiXmlDataObject):
    """Retain the complete namespace-aware subtree of an unbound XML element."""

    def __init__(self, xml_fragment: str) -> None:
        self.xml_fragment = xml_fragment

    def __eq__(self, other: object) -> bool:
        return isinstance(other, AIcXmlWildcardFragment) and self.xml_fragment == other.xml_fragment

    def __hash__(self) -> int:
        return hash(self.xml_fragment)

    def __repr__(self) -> str:
        return f'{type(self).__name__}({self.xml_fragment!r})'


@lru_cache(maxsize=256)
def xml_wildcard_class(namespace_marker: type) -> type[AIcXmlWildcardFragment]:
    """Build a concrete XML fragment implementing its generated namespace marker."""
    if not issubclass(namespace_marker, AIiXmlDataObject):
        raise TypeError('XML wildcard namespace contract must extend AIiXmlDataObject')
    if issubclass(AIcXmlWildcardFragment, namespace_marker):
        return AIcXmlWildcardFragment
    return type('AIcXmlWildcardFragment_' + namespace_marker.__name__,
                (AIcXmlWildcardFragment, namespace_marker), {'__slots__': ()})
