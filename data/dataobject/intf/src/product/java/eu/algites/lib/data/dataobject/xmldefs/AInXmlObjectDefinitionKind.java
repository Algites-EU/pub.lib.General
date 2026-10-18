package eu.algites.lib.data.dataobject.xmldefs;

/** Kinds of XML Schema declarations represented by generated XML data-object contracts. */
public enum AInXmlObjectDefinitionKind {
    /** XML Schema named or anonymous type contract, distinct from global element identity. */
    TYPE,
    /** XML Schema global element contract with an element QName and possible substitution head. */
    ELEMENT
}
