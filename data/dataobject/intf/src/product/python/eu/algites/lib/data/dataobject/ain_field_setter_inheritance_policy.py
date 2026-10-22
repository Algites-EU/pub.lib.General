"""Policy for inherited mutable setter parameter typing."""
from enum import Enum


class AInFieldSetterInheritancePolicy(str, Enum):
    """Select fixed or generically refinable setter parameter types.

    DEFAULT delegates to the containing contract or generator configuration;
    generated read contracts retain the effective choice for other artifacts.
    """

    DEFAULT = 'DEFAULT'
    INHERIT_AS_GENERIC_PARAMETER = 'INHERIT_AS_GENERIC_PARAMETER'
    INHERIT_AS_FIXED_PARAMETER = 'INHERIT_AS_FIXED_PARAMETER'
