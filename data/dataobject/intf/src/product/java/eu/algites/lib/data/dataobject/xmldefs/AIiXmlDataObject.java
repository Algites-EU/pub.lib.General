package eu.algites.lib.data.dataobject.xmldefs;

import eu.algites.lib.data.dataobject.AIiDataObject;

/**
 * Marker for contracts of XML Schema types and global elements. An element contract may
 * extend both its XML type contract and its substitutable element head contract.
 * No runtime OpenChoice registration is required for such interface inheritance.
 */
public interface AIiXmlDataObject extends AIiDataObject {
}
