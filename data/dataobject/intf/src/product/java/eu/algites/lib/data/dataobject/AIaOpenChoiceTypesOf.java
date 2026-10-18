package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Container of independent direct OpenChoice substitution declarations. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaOpenChoiceTypesOf {
    /** Returns all directly permitted substitution heads. */
    AIaOpenChoiceTypeOf[] value();
}
