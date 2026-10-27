from __future__ import annotations

from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import AIcNameConventionCatalog


class AIcAlgitesNamingProfiles:
    """Resolves declarative naming conventions from YAML, with no hardcoded naming rules."""

    @staticmethod
    def java_profile() -> AIcNameConventionCatalog:
        return AIcNameConventionCatalog.load_resource('algites-java-name-conventions.yml')

    @staticmethod
    def python_profile() -> AIcNameConventionCatalog:
        return AIcNameConventionCatalog.load_resource('algites-python-name-conventions.yml')
