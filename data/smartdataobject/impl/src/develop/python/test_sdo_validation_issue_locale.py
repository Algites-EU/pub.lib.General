"""Tests optional field related-text roles and technical-vs-user locale behavior."""
from eu.algites.lib.data.smartdataobject.aicd_smart_data_validation_issue import AIcdSmartDataValidationIssue
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel

class Contract:
    @AIaFieldLabel(label='Password', labels=(
        AIaFieldLabel.Entry(label='Heslo', localeCode='cs', roleCode='field.name'),
        AIaFieldLabel.Entry(label='Password to access the system', roleCode='field.description'),
    ))
    def getPassword(self) -> str:
        return ''

def test_sdo_topics_locale_contract():
    issue = AIcdSmartDataValidationIssue('User.Password', 'REQUIRED_ABSENT',
                                          'Required property is absent', Contract,
                                          'Password', 'Technical password field')
    assert AIaFieldLabel.FIELD_TITLE == 'field.name'
    assert AIaFieldLabel.FIELD_DESCRIPTION == 'field.description'
    assert issue.getRelatedTopicText(AIaFieldLabel.FIELD_TITLE, 'cs-CZ') == 'Heslo'
    assert issue.getFieldName() == 'Password'
    assert issue.getFieldTitle(None) == 'Password'
    assert issue.getFieldTitle('cs-CZ') == 'Heslo'
    assert issue.getFieldTitle('de-DE') == 'Password'
    assert issue.getFieldDescription(None) == 'Technical password field'
    assert issue.getFieldDescription('de-DE') == 'Password to access the system'
    assert issue.getMessage(None) == 'Required property is absent'
    assert issue.getMessage('de-DE') == 'Required property is not present.'
    assert issue.getRelatedTopicText('nonexistent', 'en') is None
