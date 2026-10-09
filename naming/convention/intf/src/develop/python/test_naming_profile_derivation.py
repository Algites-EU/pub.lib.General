from eu.algites.lib.naming.convention.aicd_naming_profile import AIcdNamingProfile
from eu.algites.lib.naming.convention.aicd_output_name_rule import AIcdOutputNameRule
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention
from eu.algites.lib.naming.convention.ain_output_name_kind import AInOutputNameKind
from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.convention.ain_version_source import AInVersionSource


def test_output_rule_override_preserves_original():
    original = AIcdNamingProfile({}, {AInOutputNameKind.DATA_TYPE: AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, 'AI', 'cgd')},
                                 AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, '_', True, True), AIcdOutputVersionPolicy(True, '_'))
    derived = original.with_output_rule(AInOutputNameKind.DATA_TYPE, AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, 'AI', 'cgsdo'))
    assert original.output_rules[AInOutputNameKind.DATA_TYPE].type_marker == 'cgd'
    assert derived.output_rules[AInOutputNameKind.DATA_TYPE].type_marker == 'cgsdo'
    assert derived is not original
