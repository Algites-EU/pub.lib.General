package eu.algites.lib.naming.convention.impl.algites;


import eu.algites.lib.naming.convention.AIiNameConventionCatalog;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Loads the declarative standard naming catalogs for Java and Python outputs. */
public final class AIcAlgitesNamingProfiles {
    private AIcAlgitesNamingProfiles() { }

    /** Default Java naming catalog, loaded from YAML resources. */
    public static AIiNameConventionCatalog javaProfile() {
        return standard("java");
    }

    /** Default Python naming catalog, loaded from YAML resources. */
    public static AIiNameConventionCatalog pythonProfile() {
        return standard("python");
    }

    private static AIiNameConventionCatalog standard(String aKind) {
        try { return AIcNameConventionCatalog.standard(aKind); }
        catch (IOException aError) { throw new UncheckedIOException(aError); }
    }
}
