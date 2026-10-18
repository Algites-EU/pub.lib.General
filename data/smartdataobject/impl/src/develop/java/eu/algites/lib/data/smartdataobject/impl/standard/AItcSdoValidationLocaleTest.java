package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.common.list.AIcBoundedList;
import eu.algites.lib.common.object.props.labels.AIaFieldLabel;
import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.smartdataobject.AIcdSmartDataValidationIssue;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIaSmartDataObjectField;
import java.util.List;
import java.util.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Regression for SDO related topics, nested list validation and locale semantics. */
public class AItcSdoValidationLocaleTest {
    @AIaDataObject(schemaId = "test.Locale", schemaVersion = 1)
    public interface AIigDemo {
        @AIaDataObjectField(fieldName = "Password")
        @AIaFieldLabel(label = "Password", labels = {
                @AIaFieldLabel.Entry(roleCode = "field.name", localeCode = "cs", label = "Heslo"),
                @AIaFieldLabel.Entry(roleCode = "field.description", label = "Password to access the system")
        })
        String getPassword();
    }

    /** Factory-selected label resolver uses CPDiscovery without constructor reflection. */
    public interface AIigFactoryLabeled {
        @AIaFieldLabel(label = "Fallback", labelResolver =
                @eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition(
                    factoryClass = eu.algites.lib.common.object.props.labels.AIiFieldLabelResolverFactory.class,
                    factoryProviderId = "algites-standard"))
        String getName();
    }

    /** Confirms that the specialized factory SPI resolves through the configured registry. */
    @Test
    public void testFieldLabelFactoryDiscovery() {
        Assert.assertEquals(eu.algites.lib.common.object.props.labels.AIsFieldLabelUtils.findLabel(
                AIigFactoryLabeled.class, "name"), "Fallback");
    }

    @Test
    public void testRelatedTopicLocales() {
        var locIssue = new AIcdSmartDataValidationIssue("User.Password", "REQUIRED_ABSENT",
                "Required property is absent", AIigDemo.class, "Password", "Technical password field");
        Assert.assertEquals(AIaFieldLabel.FIELD_TITLE, "field.name");
        Assert.assertEquals(AIaFieldLabel.FIELD_DESCRIPTION, "field.description");
        Assert.assertEquals(locIssue.getRelatedTopicText(AIaFieldLabel.FIELD_TITLE, Locale.forLanguageTag("cs-CZ")), "Heslo");
        Assert.assertEquals(locIssue.getFieldName(), "Password");
        Assert.assertEquals(locIssue.getFieldTitle(null), "Password");
        Assert.assertEquals(locIssue.getFieldTitle(Locale.forLanguageTag("cs-CZ")), "Heslo");
        Assert.assertEquals(locIssue.getFieldTitle(Locale.GERMAN), "Password");
        Assert.assertEquals(locIssue.getFieldDescription(null), "Technical password field");
        Assert.assertEquals(locIssue.getFieldDescription(Locale.ENGLISH), "Password to access the system");
        Assert.assertEquals(locIssue.getMessage(null), "Required property is absent");
        Assert.assertEquals(locIssue.getMessage(Locale.GERMAN), "Required property is not present.");
        Assert.assertNull(locIssue.getRelatedTopicText("unknown", Locale.ENGLISH));
    }
}
