package eu.algites.lib.data.dataobject.xmlschema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind;

/**
 * XML Schema identity of an XMLDEFS data-object type or global element.
 * The XML wire technology remains an independent concern of AIiXmlDataObject.
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaDataObjectXmlSchema {
    /** @return XML declaration kind, TYPE by default */
    AInXmlObjectDefinitionKind definitionKind() default AInXmlObjectDefinitionKind.TYPE;
    /** @return namespace URI of the declaration, or empty for no namespace */
    String namespace() default "";
    /** @return local part of the XML declaration QName */
    String namespaceLocalName() default "";
}
