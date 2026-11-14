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
/** @deprecated use {@link eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema}; legacy contracts remain supported during migration. */
@Deprecated(since = "1.1", forRemoval = false)
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
     * @deprecated use {@link eu.algites.lib.data.dataobject.AIaDataObjectField#order()}.
     * @return schema position or {@code -1} when unspecified
     */
    @Deprecated(since = "1.1", forRemoval = false)
    int order() default -1;

    /**
     * Namespace selection of an {@code xs:any} particle. Values are the XSD
     * namespace expressions ({@code ##any}, {@code ##other}, {@code ##local},
     * {@code ##targetNamespace} or a space-separated sequence of URIs).
     * The empty default denotes an ordinary named XML field.
     *
     * @deprecated {@code xs:any} is not part of the new generated DataObject ABI.
     * @return original namespace constraint of the wildcard
     */
    @Deprecated(since = "1.1", forRemoval = false)
    String wildcardNamespace() default "";

    /**
     * XSD processContents policy of an XML wildcard. {@code lax} and
     * {@code skip} retain the complete fragment; {@code strict} requires
     * an external schema-aware validator before claiming schema validity.
     *
     * @deprecated {@code xs:any} is not part of the new generated DataObject ABI.
     * @return original processContents policy, or the XSD default
     */
    @Deprecated(since = "1.1", forRemoval = false)
    String wildcardProcessContents() default "strict";
}
