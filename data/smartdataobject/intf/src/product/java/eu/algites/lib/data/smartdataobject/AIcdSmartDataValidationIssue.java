package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.common.validation.AIiValidationIssue;
import eu.algites.lib.common.object.props.labels.AIsFieldLabelUtils;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinFormat;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;
import java.util.Locale;

/**
 * Validation diagnostic with optional originating field and nested issue context.
 * @param path technical path to the invalid value
 * @param code stable validation code
 * @param message original diagnostic text for the issue
 * @param ownerType Java type declaring the affected property, when available
 * @param propertyName canonical name of the affected property, when available
 * @param fieldDescription default explanatory text of the field, when available
 * @param nestedIssue optional nested validation issue produced by an embedded value
 */
public record AIcdSmartDataValidationIssue(
        String path, String code, String message,
        Class<?> ownerType, String propertyName, String fieldDescription,
        AIiValidationIssue nestedIssue) implements AIiSdoValidationIssue {

    /** Compatibility constructor for existing callers that supply only a technical issue. */
    public AIcdSmartDataValidationIssue(String aPath, String aCode, String aMessage) {
        this(aPath, aCode, aMessage, null, null, null, null);
    }

    /** Contextual field diagnostic, retaining the original wire name and a real declaring type. */
    public AIcdSmartDataValidationIssue(String aPath, String aCode, String aMessage,
            Class<?> aOwnerType, String aPropertyName, String aFieldDescription) {
        this(aPath, aCode, aMessage, aOwnerType, aPropertyName, aFieldDescription, null);
    }

    /** {@inheritDoc} */
    @Override public String getCode() { return code; }
    /** {@inheritDoc} */
    @Override public String getPath() { return path; }

    /** {@inheritDoc} */
    @Override public String getMessage(Locale aLocale) {
        if (nestedIssue != null) return nestedIssue.getMessage(aLocale);
        if (aLocale == null) return message;
        boolean locCzech = "cs".equals(aLocale.getLanguage());
        return switch (code) {
            case "REQUIRED_ABSENT" -> locCzech ? "Povinná vlastnost není nastavena." : "Required property is not present.";
            case "NULL_FORBIDDEN" -> locCzech ? "Hodnota null není povolena." : "Null value is not permitted.";
            case "WRONG_TYPE" -> locCzech ? "Nesprávný datový typ." : "Property has an incompatible type.";
            case "COLLECTION_INVALID" -> locCzech ? "Neplatná kardinalita kolekce." : message;
            default -> message;
        };
    }

    /** {@inheritDoc} */
    @Override public String getRelatedTopicText(String aTopicId, Locale aLocale) {
        String locSystemName = propertyName != null ? propertyName : trailingName(path);
        if (FIELD_SYSTEM_NAME.equals(aTopicId)) return locSystemName;
        if (FIELD_LOCALIZED_NAME.equals(aTopicId)) {
            if (aLocale == null) return locSystemName;
            if (ownerType == null || locSystemName == null) return locSystemName;
            String locProperty = getterPropertyName(locSystemName);
            String locLabel = AIsFieldLabelUtils.findLabel(ownerType, locProperty,
                    AInRenderingOutputBuiltinPurpose.USER, AInRenderingOutputBuiltinFormat.PLAIN_TEXT,
                    aLocale, FIELD_LOCALIZED_NAME);
            return locLabel == null || locLabel.isBlank() || locProperty.equals(locLabel)
                    ? locSystemName : locLabel;
        }
        if (FIELD_LOCALIZED_DESCRIPTION.equals(aTopicId)) {
            if (aLocale == null) return fieldDescription;
            if (ownerType != null && locSystemName != null) {
                String locLabel = AIsFieldLabelUtils.findLabel(ownerType, getterPropertyName(locSystemName),
                        AInRenderingOutputBuiltinPurpose.USER, AInRenderingOutputBuiltinFormat.PLAIN_TEXT,
                        aLocale, FIELD_LOCALIZED_DESCRIPTION);
                if (locLabel != null && !locLabel.isBlank()) return locLabel;
            }
            return fieldDescription == null || fieldDescription.isBlank() ? null : fieldDescription;
        }
        return nestedIssue == null ? null : nestedIssue.getRelatedTopicText(aTopicId, aLocale);
    }

    private static String getterPropertyName(String aName) {
        return aName.isEmpty() ? aName : Character.toLowerCase(aName.charAt(0)) + aName.substring(1);
    }

    private static String trailingName(String aPath) {
        if (aPath == null || aPath.isEmpty()) return null;
        String locValue = aPath.substring(aPath.lastIndexOf('.') + 1);
        int locIndex = locValue.indexOf('[');
        return locIndex < 0 ? locValue : locValue.substring(0, locIndex);
    }
}
