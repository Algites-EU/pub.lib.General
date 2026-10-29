from enum import Enum

class AInBoundsMutationPolicy(str, Enum):
    ALLOW_ANY = 'ALLOW_ANY'
    PREVENT_WORSENING = 'PREVENT_WORSENING'
