"""SmartDataObject effective-read policies; unrelated to DataObject validation."""
from __future__ import annotations


def smart_data_object(*, allow_non_present_read: bool = False):
    """Set an interface-wide default for reading absent required values."""
    def decorate(contract):
        contract.__smart_data_object_allow_non_present_read__ = allow_non_present_read
        return contract
    return decorate


def smart_data_object_field(*, allow_non_present_read: bool = False):
    """Override the interface policy for a specific getter (including False)."""
    def decorate(getter):
        getter.__smart_data_object_allow_non_present_read__ = allow_non_present_read
        return getter
    return decorate
