from enum import Enum

class AInTextReferenceEncoding(str, Enum):
    NONE = 'NONE'
    JSON_DOTNET = 'JSON_DOTNET'
    JSON_DIAGNOSTIC = 'JSON_DIAGNOSTIC'
    YAML_NATIVE = 'YAML_NATIVE'
    XML_DIAGNOSTIC = 'XML_DIAGNOSTIC'
    XML_SCHEMA = 'XML_SCHEMA'
