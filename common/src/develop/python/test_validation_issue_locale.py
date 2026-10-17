"""Regression tests for technical null-locale and English fallback behavior."""
import unittest
from eu.algites.lib.common.list.aic_bounded_list import AIcBoundedList

class ValidationIssueLocaleTest(unittest.TestCase):
    def test_technical_null_locale(self):
        values = AIcBoundedList(2, 5)
        issue = values.getValidationIssues()[0]
        self.assertEqual(issue.getCode(), 'MIN_ITEMS')
        self.assertEqual(issue.getMessage(None), 'MIN_ITEMS(actualSize=0, bound=2)')
        self.assertEqual(values.getInvalidStateMessage(None), issue.getMessage(None))
        self.assertIsNone(issue.getRelatedTopicText('unknown', None))

    def test_requested_and_unsupported_locales(self):
        values = AIcBoundedList(2, 5)
        issue = values.getValidationIssues()[0]
        self.assertIn('alespoň 2', issue.getMessage('cs-CZ'))
        self.assertIn('at least 2', issue.getMessage('en-GB'))
        self.assertEqual(issue.getMessage('xy-ZZ'), issue.getMessage('en-US'))

if __name__ == '__main__':
    unittest.main()
