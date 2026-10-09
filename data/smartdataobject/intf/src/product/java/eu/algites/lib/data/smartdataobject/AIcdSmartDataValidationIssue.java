package eu.algites.lib.data.smartdataobject;

/** Diagnostic issue local to an object or addressed by a graph path. */
public record AIcdSmartDataValidationIssue(String path, String code, String message) { }
