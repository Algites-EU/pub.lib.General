package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;

/**
 * XML Schema wire representation metadata for a getter on an XMLDEFS interface.
 * Common field semantics belong exclusively to AIaDataObjectField on the common contract.
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldXmlSchema {
    /** @return XML QName namespace URI */
    String namespace() default "";
    /** @return XML QName local name */
    String namespaceLocalName() default "";
    /** @return ELEMENT or ATTRIBUTE representation */
    AInXmlValueKind valueKind() default AInXmlValueKind.ELEMENT;
    /**
     * Legacy XML-specific declaration order. Prefer the shared AIaDataObjectField.order().
     * @return XML declaration order, or -1 when unspecified
     * @deprecated use AIaDataObjectField.order() on the common getter
     */
    @Deprecated(since = "1.1", forRemoval = false)
    int order() default -1;
    /** @return original xs:any namespace expression, or empty if absent */
    String wildcardNamespace() default "";
    /** @return xs:any validation policy */
    String wildcardProcessContents() default "strict";
}
