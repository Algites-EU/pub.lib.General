package eu.algites.lib.data.smartdataobject.xmldefs;

import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;

/**
 * Mutable SmartDataObject contract for data originating from XML Schema.
 *
 * <p>This is an interface composition, not a concrete runtime implementation.
 * Generated mutable contracts extend this marker together with their generated
 * read-only contract. The shared SmartDataObject API supplies presence, effective
 * values, validation and mapping operations; the technology marker preserves the
 * definition family for schema-aware consumers. A concrete implementation may
 * derive from the common SmartDataObject base class and implement the generated
 * mutable interface, without format-specific runtime registration.
 */
public interface AIiXmlSmartDataObject extends AIiSmartDataObject, AIiXmlDataObject {
}
