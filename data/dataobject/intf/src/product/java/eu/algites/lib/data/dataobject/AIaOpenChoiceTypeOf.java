package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one direct permitted substitution edge from the annotated contract to its parent.
 * This is not inherited implicitly by a newly declared domain contract. Implementations of
 * the annotated contract do not need to repeat it. A chain of explicitly declared edges
 * permits transitive OpenChoice substitution across JPMS modules.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AIaOpenChoiceTypesOf.class)
public @interface AIaOpenChoiceTypeOf {
    /** Direct parent contract into whose open choice this contract can be substituted. */
    Class<?> value();
}
