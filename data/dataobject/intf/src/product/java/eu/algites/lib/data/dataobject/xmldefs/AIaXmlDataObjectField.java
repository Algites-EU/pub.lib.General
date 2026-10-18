package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional XML representation metadata for a {@code AIaDataObjectField} getter.
 * XML QName and attribute-versus-element representation do not change the field's
 * independent schema identifier, presence policy, or nullability.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaXmlDataObjectField {
    /**
     * XML namespace URI, or empty for an unqualified field.
     *
     * @return namespace URI
     */
    String namespace() default "";

    /**
     * Local part of the field's XML QName, or empty for its common field name.
     *
     * @return XML QName local part
     */
    String namespaceLocalName() default "";

    /**
     * XML wire kind; ELEMENT is the default, ATTRIBUTE requires a scalar value.
     *
     * @return XML element or attribute representation
     */
    AInXmlValueKind valueKind() default AInXmlValueKind.ELEMENT;
}
