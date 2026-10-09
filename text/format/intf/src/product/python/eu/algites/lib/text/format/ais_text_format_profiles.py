from .aicd_text_format_profile import AIcdTextFormatProfile
from .ain_text_format_type import AInTextFormatType as F
from .ain_text_reference_encoding import AInTextReferenceEncoding as E
from .ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode as M

class AIsTextFormatProfiles:
    """Static profile catalog; do not instantiate."""
    _all = frozenset(M)
    _none = frozenset({M.NO_REFERENCES})
    JSON_STANDARD = AIcdTextFormatProfile('json-standard-1', F.JSON, E.NONE, False, _none)
    JSON_DOTNET_PRESERVE = AIcdTextFormatProfile('json-dotnet-preserve-1', F.JSON, E.JSON_DOTNET, False,
        frozenset({M.ALL_OBJECTS, M.CYCLIC_REFERENCES_ONLY, M.REPEATED_OBJECTS}))
    JSON_DIAGNOSTIC = AIcdTextFormatProfile('json-diagnostic-1', F.JSON, E.JSON_DIAGNOSTIC, True, _all)
    YAML_STANDARD = AIcdTextFormatProfile('yaml-standard-1', F.YAML, E.NONE, False, _none)
    YAML_NATIVE_REFERENCES = AIcdTextFormatProfile('yaml-native-references-1', F.YAML, E.YAML_NATIVE, False, _all)
    YAML_DIAGNOSTIC = AIcdTextFormatProfile('yaml-diagnostic-1', F.YAML, E.YAML_NATIVE, True, _all)
    XML_XSD = AIcdTextFormatProfile('xml-xsd-1', F.XML, E.XML_SCHEMA, False, _none)
    XML_DIAGNOSTIC = AIcdTextFormatProfile('xml-diagnostic-1', F.XML, E.XML_DIAGNOSTIC, True, _all)
    def __new__(cls):
        raise TypeError('Static utility class')
