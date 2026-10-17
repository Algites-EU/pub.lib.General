from enum import Enum

class AInIntervalBoundary(Enum):
    OPEN = ('open', False, True, '(', ')')
    CLOSED = ('closed', False, False, '[', ']')
    UNBOUNDED = ('unbounded', True, True, '(*', '*)')

    def __init__(self, aCode: str, aIgnored: bool, aOpen: bool, aLeft: str, aRight: str):
        self._code, self._ignored, self._open = aCode, aIgnored, aOpen
        self._left, self._right = aLeft, aRight

    def code(self) -> str: return self._code
    def isOpen(self) -> bool: return self._open
    def isBoundaryValueIgnored(self) -> bool: return self._ignored
    def getLeftBoundaryStringRepresentation(self) -> str: return self._left
    def getRightBoundaryStringRepresentation(self) -> str: return self._right
