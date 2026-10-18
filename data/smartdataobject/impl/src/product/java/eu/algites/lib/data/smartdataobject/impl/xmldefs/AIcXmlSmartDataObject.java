package eu.algites.lib.data.smartdataobject.impl.xmldefs;

import eu.algites.lib.data.smartdataobject.impl.standard.AIcSmartDataObject;
import eu.algites.lib.data.smartdataobject.xmldefs.AIiXmlSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue;
import java.util.Map;

/**
 * Shared mutable implementation base for objects originating in XML Schema definitions.
 *
 * <p>This technology-specific base connects the general SmartDataObject storage,
 * field presence, validation and graph operations with the corresponding public
 * technology contract. Generated mutable objects extend this class so that
 * future XML Schema behavior can be added in one place without changing their
 * inheritance hierarchy. This class introduces no additional state or format
 * conversion semantics at present.
 *
 * <p>Instances are deliberately abstract: a generated subclass provides the
 * concrete schema fields, accessors and their metadata.
 */
public abstract class AIcXmlSmartDataObject extends AIcSmartDataObject implements AIiXmlSmartDataObject {
    /**
     * Initializes a schema-generated XML Schema instance with no explicitly present fields.
     * Generated field defaults remain effective without being inserted as raw values.
     */
    protected AIcXmlSmartDataObject() {
        super();
    }

    /**
     * Initializes a schema-generated XML Schema object from a typed virtual mapping.
     *
     * <p>The shared runtime retains identity while populating references; inherited
     * required-field and value constraints apply in the normal SmartDataObject manner.
     *
     * @param aMapping typed field graph to populate; must satisfy the shared runtime
     *                 mapping preconditions
     */
    protected AIcXmlSmartDataObject(Map<String, AIidVirtualFieldValue> aMapping) {
        super(aMapping);
    }
}
