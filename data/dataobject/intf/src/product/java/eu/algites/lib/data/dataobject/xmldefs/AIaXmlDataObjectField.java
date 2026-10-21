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

    /**
     * Zero-based position in the declaring XML content model. A negative value
     * means that no schema order was supplied, as with manually written
     * contracts. The order is applied within each declaring interface so
     * inherited base fields precede locally declared derived fields.
     *
     * @return schema position or {@code -1} when unspecified
     */
    int order() default -1;

    /**
     * Minimum number of XML wire occurrences. Negative means the schema has not
     * supplied an occurrence bound, not an implicit requirement. Attributes have
     * cardinality zero or one; generated contracts include their exact bounds.
     *
     * @return minimum XML occurrence count, or {@code -1} when unspecified
     */
    int minOccurs() default -1;

    /**
     * Maximum number of XML wire occurrences. {@code -1} means unspecified or
     * unbounded; a nonnegative value is an enforceable wire limit.
     *
     * @return maximum XML occurrence count, or {@code -1} without a finite limit
     */
    int maxOccurs() default -1;

    /**
     * Namespace selection of an {@code xs:any} particle. Values are the XSD
     * namespace expressions ({@code ##any}, {@code ##other}, {@code ##local},
     * {@code ##targetNamespace} or a space-separated sequence of URIs).
     * The empty default denotes an ordinary named XML field.
     *
     * @return original namespace constraint of the wildcard
     */
    String wildcardNamespace() default "";

    /**
     * XSD processContents policy of an XML wildcard. {@code lax} and
     * {@code skip} retain the complete fragment; {@code strict} requires
     * an external schema-aware validator before claiming schema validity.
     *
     * @return original processContents policy, or the XSD default
     */
    String wildcardProcessContents() default "strict";
}
