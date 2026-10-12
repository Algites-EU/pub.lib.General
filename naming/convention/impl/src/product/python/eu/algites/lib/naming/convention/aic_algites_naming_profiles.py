from __future__ import annotations

from eu.algites.lib.naming.convention.aic_name_convention_catalog import AIcNameConventionCatalog


class AIcAlgitesNamingProfiles:
    """Resolves declarative naming conventions from YAML, with no hardcoded naming rules."""

    @staticmethod
    def java_profile() -> AIcNameConventionCatalog:
        return AIcNameConventionCatalog.standard('java')

    @staticmethod
    def python_profile() -> AIcNameConventionCatalog:
        return AIcNameConventionCatalog.standard('python')
