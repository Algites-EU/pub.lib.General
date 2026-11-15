"""Inputs for a CPDiscovery field-default invocation."""
from dataclasses import dataclass
from typing import Any
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_resolution_context import AIiCategoryProviderResolutionContext

@dataclass(frozen=True)
class AIiFieldDefaultValueFactoryContext(AIiCategoryProviderResolutionContext):
    expected_type: Any
    default_arguments: tuple[str, ...]

    def getExpectedType(self): return self.expected_type
    def getDefaultArguments(self) -> tuple[str, ...]: return self.default_arguments
