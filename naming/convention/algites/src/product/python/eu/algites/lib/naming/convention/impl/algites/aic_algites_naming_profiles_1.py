from __future__ import annotations

from eu.algites.lib.naming.convention.impl.algites.aic_name_convention_catalog_1 import AIcNameConventionCatalog_1


class AIcAlgitesNamingProfiles_1:
    """Resolves declarative naming conventions from YAML, with no hardcoded naming rules."""

    @staticmethod
    def java_profile() -> AIcNameConventionCatalog_1:
        return AIcNameConventionCatalog_1.standard('java')

    @staticmethod
    def python_profile() -> AIcNameConventionCatalog_1:
        return AIcNameConventionCatalog_1.standard('python')
