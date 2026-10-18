package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * XML Schema identity of an XML type or global element contract.
 * This annotation complements {@code AIaDataObject}; XML QName identity must not be
 * confused with common schema ID, Java type name, or the field display label.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaXmlDataObject {
    /**
     * Identifies whether the contract represents a type or a global element.
     *
     * @return XML definition kind, TYPE by default
     */
    AInXmlObjectDefinitionKind definitionKind() default AInXmlObjectDefinitionKind.TYPE;

    /**
     * Namespace URI of the XML declaration, or empty for no namespace.
     *
     * @return namespace URI
     */
    String namespace() default "";

    /**
     * Local part of the declaration's QName, or empty for generated/default naming.
     *
     * @return XML QName local name
     */
    String namespaceLocalName() default "";
}
