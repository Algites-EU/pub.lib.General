"""Public, implementation-independent contract for naming-convention catalogs."""
from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Mapping


class AIiNameConventionCatalog(ABC):
    """Resolve naming profiles, compositions and generated-method roles.

    A catalog instance is an immutable selection of naming conventions; selecting
    an optional generated-method profile produces a new catalog instance.
    Methods return ``None`` for disabled optional generated methods rather than
    inventing a fallback name.
    """

    @property
    @abstractmethod
    def generated_method_profile(self) -> str | None:
        """Return the selected generated-method profile or ``None`` when unset."""

    @abstractmethod
    def generated_method_reference(self, role: str) -> str | None:
        """Resolve a generated-method role to a configured reference.

        Args:
            role: Role key within the selected generated-method profile.

        Returns:
            Configured reference, or ``None`` if no profile/role is enabled.

        Raises:
            KeyError: If the selected profile is missing from the catalog.
        """

    @abstractmethod
    def with_generated_method_profile(self, profile: str) -> AIiNameConventionCatalog:
        """Return a separate catalog with ``profile`` selected.

        Raises:
            ValueError: If ``profile`` is not a configured method profile.
        """

    @abstractmethod
    def parse(self, profile_name: str, structure: str, value: str) -> dict[str, str]:
        """Parse ``value`` using a named structure and return capture values."""

    @abstractmethod
    def identifier_convention(self, profile_name: str, structure: str) -> str | None:
        """Return the identifier style declared for a profile's structure."""

    @abstractmethod
    def format(self, profile_name: str, structure: str, variables: Mapping[str, str],
               source_convention: str | None = None) -> str:
        """Format a named structure from identifiers and other captured variables."""

    @abstractmethod
    def matches(self, profile_name: str, structure: str, value: str) -> bool:
        """Whether ``value`` conforms to the named profile structure."""

    @abstractmethod
    def convert_identifier(self, value: str, source: str, target: str) -> str:
        """Convert identifier spelling between declared naming styles."""

    @abstractmethod
    def profile_value(self, name: str, section: str, key: str) -> str:
        """Return a profile section value, or an empty string if absent."""

    @abstractmethod
    def composition_reference(self, profile: str, role: str) -> str:
        """Return the composition reference assigned to ``role``."""
