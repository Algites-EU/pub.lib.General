package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import org.testng.annotations.Test;

/**
 * Verifies the metadata-backed default and explicit EMPTY_VALUE semantics in Java.
 *
 * @author linhart1
 */
public final class AItcDataObjectEmptyFixedDefaultTest {
    /** Read contract used to exercise the public annotation API. */
    public interface AIigDefaults extends AIiDataObject {
        /** @return the effective string, substituting only an explicitly empty value */
        @AIaDataObjectField(isEmptyAllowed = true,
            defaultValue = @AIaDataObjectFieldDefault(
                kind = AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY,
                value = "fallback", defined = true))
        String getEmptyFallback();

        /** @return the integer default only if no raw value is present */
        @AIaDataObjectField(defaultValue = @AIaDataObjectFieldDefault(
            kind = AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT,
            value = "42", defined = true))
        Integer getAbsentFallback();

        /** @return no default value from declaration-only metadata */
        @AIaDataObjectField(defaultValue = @AIaDataObjectFieldDefault(
            kind = AInDataObjectFieldDefaultKind.DECLARATION_ONLY,
            value = "documentation", defined = true))
        String getDocumentationOnly();

        /** @return the fixed field if present, without materializing it on read */
        @AIaDataObjectField(isEmptyAllowed = true,
            defaultValue = @AIaDataObjectFieldDefault(
                kind = AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT,
                value = "ABC", defined = true))
        String getFixed();

        /** @return primitive integer with a fixed constraint of one */
        @AIaDataObjectField(defaultValue = @AIaDataObjectFieldDefault(
            kind = AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT,
            value = "1", defined = true))
        int getFixedNumber();
    }

    /** Minimal hand-written writable counterpart for runtime-only regression tests. */
    public static final class AIcgdDefaults extends AIcSmartDataObject implements AIigDefaults {
        /** @return effective empty-fallback value */
        @Override public String getEmptyFallback() { return (String) get_EffectiveField("EmptyFallback"); }
        /** @return effective absence-fallback value */
        @Override public Integer getAbsentFallback() { return (Integer) get_EffectiveField("AbsentFallback"); }
        /** @return declaration-only field value */
        @Override public String getDocumentationOnly() { return (String) get_EffectiveField("DocumentationOnly"); }
        /** @return fixed field value */
        @Override public String getFixed() { return (String) get_EffectiveField("Fixed"); }
        /** @return the primitive fixed number */
        @Override public int getFixedNumber() { return (Integer) get_EffectiveField("FixedNumber"); }
        /** @param aValue integer that must equal one */
        public void setFixedNumber(int aValue) { set_RawField("FixedNumber", aValue); }
        /** @param aValue direct boxed test value, checked by metadata before writing */
        public void setFixedNumberRaw(Object aValue) { set_RawField("FixedNumber", aValue); }
        /** @param aValue ordinary (possibly empty) String */
        public void setEmptyFallback(String aValue) { set_RawField("EmptyFallback", aValue); }
        /** @return whether the distinct empty sentinel was assigned */
        public boolean isEmptyFallback() { return isEmpty_Field("EmptyFallback"); }
        /** Set the distinct empty sentinel. */
        public void markEmptyFallback() { set_EmptyField("EmptyFallback"); }
        /** Remove the empty-fallback raw value. */
        public void unsetEmptyFallback() { unset_Field("EmptyFallback"); }
        /** @param aValue assigned value, checked against fixed constraint */
        public void setFixed(String aValue) { set_RawField("Fixed", aValue); }
        /** Set the fixed field to empty, which must fail. */
        public void markEmptyFixed() { set_EmptyField("Fixed"); }
        /** @param aValue integer to store raw */
        public void setAbsentFallback(Integer aValue) { set_RawField("AbsentFallback", aValue); }
        /** Remove the raw integer value. */
        public void unsetAbsentFallback() { unset_Field("AbsentFallback"); }
    }

    /** Ensure empty-string assignment does not create the distinct empty sentinel. */
    @Test public void explicitEmptyRoundTrip() {
        var value = new AIcgdDefaults();
        assert value.getEmptyFallback() == null;
        value.setEmptyFallback("");
        assert !value.isEmptyFallback();
        assert "".equals(value.getEmptyFallback());
        value.markEmptyFallback();
        assert value.isEmptyFallback();
        assert "fallback".equals(value.getEmptyFallback());
        assert value.toString_RawValues(AIsTextFormatProfiles.JSON_STANDARD,
            AInTextOutputObjectReferenceMode.NO_REFERENCES).contains("\"EmptyFallback\":\"\"");
        value.unsetEmptyFallback();
        assert value.getEmptyFallback() == null;
        assert !value.isEmptyFallback();
    }

    /** Check all four default policies and their raw absence behavior. */
    @Test public void defaultPoliciesAndFixedConstraint() {
        var value = new AIcgdDefaults();
        assert value.getAbsentFallback() == 42;
        assert value.getDocumentationOnly() == null;
        assert value.getFixed() == null;
        try { value.setFixed("wrong"); assert false : "Fixed constraint must reject other values"; }
        catch (IllegalArgumentException expected) { /* expected */ }
        try { value.markEmptyFixed(); assert false : "Explicit empty must not bypass fixed"; }
        catch (IllegalArgumentException expected) { /* expected */ }
        value.setFixed("ABC");
        assert "ABC".equals(value.getFixed());
        value.setAbsentFallback(17);
        assert value.getAbsentFallback() == 17;
        value.unsetAbsentFallback();
        assert value.getAbsentFallback() == 42;
        // Primitive int reflection must accept Integer boxing, not Boolean.
        value.setFixedNumber(1);
        assert value.getFixedNumber() == 1;
        try { value.setFixedNumber(2); assert false : "Numeric fixed constraint must reject other numbers"; }
        catch (IllegalArgumentException expected) { /* expected */ }
        try { value.setFixedNumberRaw(Boolean.TRUE); assert false : "Boolean is not an integer"; }
        catch (IllegalArgumentException expected) { /* expected */ }
        assert value.getFixedNumber() == 1;
    }
}
