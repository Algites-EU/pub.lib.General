"""Presence constraints shared by plain data objects and SmartDataObjects."""
from enum import Enum


class AInFieldPresenceKind(str, Enum):
    REQUIRED = 'REQUIRED'
    ALLOWED = 'ALLOWED'
    FORBIDDEN = 'FORBIDDEN'
