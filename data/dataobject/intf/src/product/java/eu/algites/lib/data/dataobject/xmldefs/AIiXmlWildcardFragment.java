package eu.algites.lib.data.dataobject.xmldefs;

/**
 * XML data-object view of an otherwise unbound {@code xs:any} subtree.
 * Implementations carry the entire namespace-aware XML subtree; concrete XSD
 * implementations may also implement a generated namespace-specific interface.
 */
public interface AIiXmlWildcardFragment extends AIiXmlDataObject {
    /**
     * Returns the complete, single-element XML subtree for lossless round trips.
     * The caller must validate its QName against the declaring {@code xs:any} constraint.
     *
     * @return serialized XML element including its attributes, descendants and namespace declarations
     */
    String xmlFragment();
}
