from enum import Enum
class AInUidEnumDataOrigin(Enum):
    BUILTIN = ('builtin', False)
    CUSTOM = ('custom', True)
    def __init__(self, aCode: str, aNamespaceUsed: bool):
        self._code, self._namespace_used = aCode, aNamespaceUsed
    def code(self) -> str: return self._code
    def namespaceUsed(self) -> bool: return self._namespace_used
    @staticmethod
    def findByCodeOrNull(aCode: str):
        return next((value for value in AInUidEnumDataOrigin if value.code() == aCode), None)
    @staticmethod
    def getByCodeOrThrow(aCode: str):
        value = AInUidEnumDataOrigin.findByCodeOrNull(aCode)
        if value is None: raise ValueError(f"Unsupported enumItemOrigin: '{aCode}'")
        return value
