package eu.algites.lib.common.validation;

import eu.algites.lib.common.list.AIcBoundedList;
import java.util.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Verifies technical null-locale diagnostics and English localization fallbacks. */
public class AItcValidationIssueLocaleTest {
    @Test
    public void testBoundedListIssueLocales() {
        AIcBoundedList<String> locValues = new AIcBoundedList<>(2, 5);
        AIiValidationIssue locIssue = locValues.getValidationIssues().get(0);
        Assert.assertEquals(locIssue.getCode(), "MIN_ITEMS");
        Assert.assertEquals(locIssue.getMessage(null), "MIN_ITEMS(actualSize=0, bound=2)");
        Assert.assertTrue(locIssue.getMessage(Locale.ENGLISH).contains("at least 2"));
        Assert.assertTrue(locIssue.getMessage(Locale.GERMAN).contains("at least 2"));
        Assert.assertTrue(locIssue.getMessage(Locale.forLanguageTag("cs-CZ")).contains("alespoň 2"));
        Assert.assertNull(locIssue.getRelatedTopicText("unknown.topic", Locale.ENGLISH));
        Assert.assertTrue(locValues.getInvalidStateMessage(null).startsWith("MIN_ITEMS("));
        Assert.assertTrue(locValues.getInvalidStateMessage(Locale.GERMAN).startsWith("List must"));
    }
}
