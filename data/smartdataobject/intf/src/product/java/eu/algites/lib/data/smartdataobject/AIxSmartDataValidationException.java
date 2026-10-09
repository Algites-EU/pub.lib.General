package eu.algites.lib.data.smartdataobject;

import java.util.List;

/** Raised when an effective wire representation cannot satisfy known data constraints. */
public final class AIxSmartDataValidationException extends IllegalStateException {
    private final List<AIcdSmartDataValidationIssue> issues;

    public AIxSmartDataValidationException(List<AIcdSmartDataValidationIssue> aIssues) {
        super("SmartDataObject effective values are invalid: " + aIssues);
        issues = List.copyOf(aIssues);
    }
    public List<AIcdSmartDataValidationIssue> getIssues() { return issues; }
}
