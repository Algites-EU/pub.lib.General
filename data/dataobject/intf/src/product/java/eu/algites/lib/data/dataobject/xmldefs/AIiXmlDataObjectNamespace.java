package eu.algites.lib.data.dataobject.xmldefs;

/**
 * Common base for namespace-specific XML data-object interfaces generated from XSDs.
 * A namespace contract carries its exact namespace URI through {@link AIaXmlDataObject},
 * and is inherited by every generated XML object in the corresponding namespace.
 * It is not a JSON or YAML data-object contract.
 */
public interface AIiXmlDataObjectNamespace extends AIiXmlDataObject {
}
