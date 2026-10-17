package eu.algites.lib.data.smartdataobject;

import java.util.List;

/** Raised when an effective wire representation cannot satisfy known data constraints. */
public final class AIxSmartDataValidationException extends IllegalStateException {
    private final List<AIcdSmartDataValidationIssue> issues;

    /**
     * Creates an exception describing an invalid Smart Data Object state.
     * @param aIssues issues supplied to the operation
     */
    public AIxSmartDataValidationException(List<AIcdSmartDataValidationIssue> aIssues) {
        super("SmartDataObject effective values are invalid: " + aIssues);
        issues = List.copyOf(aIssues);
    }
    /**
     * Returns the issues.
     * @return the issues
     */
    public List<AIcdSmartDataValidationIssue> getIssues() { return issues; }
}
