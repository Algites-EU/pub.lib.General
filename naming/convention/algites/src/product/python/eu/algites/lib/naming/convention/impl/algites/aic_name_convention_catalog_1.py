from __future__ import annotations

from importlib import resources
from pathlib import Path
import re
from typing import Any, Callable, Mapping
import yaml

from eu.algites.lib.naming.convention.impl.algites.aicd_name_match import AIcdNameMatch


_NAMED_CAPTURE = re.compile(r'\(\?<([A-Za-z][A-Za-z0-9_]*)>')
_CAMEL_BOUNDARY = re.compile(r'(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])')


class AIcNameConventionCatalog_1:
    """YAML-driven, language-independent name composition engine.

    Java-style (?<name>...) captures in YAML are translated to Python's (?P<name>...).
    Only file_name_stem + file_name_suffix is used to identify a file's profile.
    """

    def __init__(self, config: Mapping[str, Any]):
        self._config = dict(config)
        self._fragments = config.get('NameFragments', {})
        self._compositions = config.get('NameCompositions', {})
        self._conventions = config.get('NameConventions', {})
        self._identifiers = config.get('IdentifierConventions', {})
        self._versions = config.get('VersionConventions', {})
        self._expanded: dict[str, tuple[dict, ...]] = {}
        self._patterns: dict[str, re.Pattern] = {}
        for name in self._conventions:
            self.filename_pattern(name)

    @classmethod
    def _load_document(cls, filename: str, read_text: Callable[[str], str]) -> AIcNameConventionCatalog_1:
        """Resolve a YAML catalog and its standard sibling using one resource provider."""
        config = yaml.safe_load(read_text(filename))
        if not isinstance(config, dict):
            raise ValueError(f'Name conventions document must be a mapping: {filename}')
        if 'StandardConventionsFile' in config:
            standard_name = config['StandardConventionsFile']
            if not isinstance(standard_name, str) or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*\.yml', standard_name):
                raise ValueError(f'Invalid standard conventions resource name: {standard_name!r}')
            standard = yaml.safe_load(read_text(standard_name))
            if not isinstance(standard, dict):
                raise ValueError(f'Standard conventions document must be a mapping: {standard_name}')
            for section in ('IdentifierConventions', 'VersionConventions'):
                combined = dict(standard.get(section, {}))
                for name, item in config.get(section, {}).items():
                    if name in combined:
                        raise ValueError(f'Duplicate standard convention: {name}')
                    combined[name] = item
                config[section] = combined
        return cls(config)

    @classmethod
    def load(cls, path: str | Path) -> AIcNameConventionCatalog_1:
        """Load an explicitly selected external catalog without searching parent directories."""
        path = Path(path)
        return cls._load_document(path.name, lambda name: (path.parent / name).read_text(encoding='utf-8'))

    @classmethod
    def load_resource(cls, filename: str) -> AIcNameConventionCatalog_1:
        """Read the resource from the importable Python package or its Modustro resource bundle.

        This uses Python package-resource lookup, never the source-repository layout.
        Modustro publishes config resources under this artifact's isolated resource
        namespace. A local Python environment may also expose the config source
        root directly as a namespace package on sys.path.
        """
        if not isinstance(filename, str) or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*\.yml', filename):
            raise ValueError(f'Invalid naming catalog resource filename: {filename!r}')
        providers = (
            ('eu.algites.lib.naming.convention', ()),
            ('algites.pub.lib.general.naming.convention.algites',
             ('eu', 'algites', 'lib', 'naming', 'convention')),
        )
        for package, subpath in providers:
            try:
                root = resources.files(package).joinpath(*subpath)
            except (ModuleNotFoundError, TypeError):
                continue
            if root.joinpath(filename).is_file():
                return cls._load_document(filename, lambda name: root.joinpath(name).read_text(encoding='utf-8'))
        raise FileNotFoundError(
            f'Algites YAML naming resource {filename!r} not found in the installed '
            'naming artifact or its configured Python resource roots'
        )

    @classmethod
    def load_many(cls, sources) -> AIcNameConventionCatalog_1:
        """Load an ordered set of file/resource catalogs without permitting conflicting IDs.

        An identifier defined identically in multiple files is shared. Distinct definitions
        of one identifier are always an error; file order never grants precedence.
        """
        merged = {section: {} for section in (
            'IdentifierConventions', 'VersionConventions', 'NameFragments',
            'NameCompositions', 'NameConventions')}
        if not sources:
            raise ValueError('At least one naming convention catalog is required')
        for source in sources:
            value = str(source)
            catalog = cls.load_resource(value.removeprefix('resource:')) if value.startswith('resource:') else cls.load(value)
            for section, definitions in merged.items():
                for name, definition in catalog._config.get(section, {}).items():
                    if name in definitions and definitions[name] != definition:
                        raise ValueError(f'Conflicting naming definition {section}/{name!r} in {source}')
                    definitions[name] = definition
        return cls(merged)

    @classmethod
    def standard(cls, technology: str) -> AIcNameConventionCatalog_1:
        """Load an artifact-packaged catalog; independent of checkout and version context."""
        if technology not in ('java', 'python'):
            raise ValueError(f'Unsupported naming target: {technology}')
        return cls.load_resource(f'algites-{technology}-name-conventions.yml')

    @property
    def names(self) -> tuple[str, ...]:
        return tuple(self._conventions)

    def profile(self, name: str) -> Mapping[str, Any]:
        return self._conventions[name]

    def composition(self, name: str) -> tuple[dict, ...]:
        if name not in self._expanded:
            self._expanded[name] = self._expand(name, ())
        return self._expanded[name]

    def _expand(self, name: str, stack: tuple[str, ...]) -> tuple[dict, ...]:
        if name in stack:
            raise ValueError('Cyclic NameFragments reference: ' + ' -> '.join(stack + (name,)))
        value = self._fragments.get(name, self._compositions.get(name))
        if value is None:
            raise ValueError(f'Unknown NameFragment or NameComposition: {name}')
        results: list[dict] = []
        for segment in value['Segments']:
            if 'Ref' in segment:
                results.extend(self._expand(segment['Ref'], stack + (name,)))
            else:
                kind = segment['Kind']
                if kind not in ('constant_string', 'identifier', 'version', 'variable'):
                    raise ValueError(f'Unsupported segment kind: {kind}')
                if kind == 'constant_string' and 'Value' not in segment:
                    raise ValueError('Constant segment requires Value')
                if kind in ('identifier', 'version') and (('Convention' in segment) == ('CustomConventionRegexp' in segment)):
                    raise ValueError(f'{kind} must specify exactly one of Convention or CustomConventionRegexp')
                if kind == 'variable' and ('Name' not in segment or 'Regexp' not in segment):
                    raise ValueError('Variable segment requires Name and Regexp')
                results.append(dict(segment))
        return tuple(results)

    def segments(self, profile_name: str, structure: str) -> tuple[dict, ...]:
        reference = self._conventions[profile_name]['StructureNaming'][structure]
        return self.composition(reference)

    def _segment_pattern(self, segment: dict) -> str:
        kind = segment['Kind']
        if kind == 'constant_string':
            return re.escape(segment['Value'])
        if kind == 'variable':
            return f"(?P<{segment['Name']}>{segment['Regexp']})"
        expression = segment.get('CustomConventionRegexp')
        if expression is None:
            catalog = self._identifiers if kind == 'identifier' else self._versions
            expression = catalog[segment['Convention']]['Regexp']
        return _NAMED_CAPTURE.sub(r'(?P<\1>', expression)

    def pattern(self, profile_name: str, structure: str) -> re.Pattern:
        source = ''.join(self._segment_pattern(s) for s in self.segments(profile_name, structure))
        return re.compile(r'\A(?:' + source + r')\Z')

    def filename_pattern(self, profile_name: str) -> re.Pattern:
        if profile_name not in self._patterns:
            values = self._conventions[profile_name]['StructureNaming']
            if 'file_name_stem' not in values or 'file_name_suffix' not in values:
                raise ValueError(f'Incomplete filename profile: {profile_name}')
            seq = self.segments(profile_name, 'file_name_stem') + self.segments(profile_name, 'file_name_suffix')
            source = ''.join(self._segment_pattern(s) for s in seq)
            self._patterns[profile_name] = re.compile(r'\A(?:' + source + r')\Z')
        return self._patterns[profile_name]

    def parse(self, profile_name: str, structure: str, value: str) -> dict[str, str]:
        found = self.pattern(profile_name, structure).fullmatch(value)
        if found is None:
            raise ValueError(f'{value!r} does not match {profile_name}/{structure}')
        return {key: val for key, val in found.groupdict().items() if val is not None}

    def matches(self, profile_name: str, structure: str, value: str) -> bool:
        return self.pattern(profile_name, structure).fullmatch(value) is not None

    def recognize_filename(self, filename: str, allowed: set[str] | None = None) -> AIcdNameMatch | None:
        found = []
        for name, pattern in self._patterns.items():
            if allowed is not None and name not in allowed:
                continue
            match = pattern.fullmatch(filename)
            if match:
                found.append(AIcdNameMatch(name, {k: v for k, v in match.groupdict().items() if v is not None}))
        if len(found) > 1:
            raise ValueError(f'Ambiguous filename {filename!r}: {[item.convention_name for item in found]}')
        return found[0] if found else None

    def clean(self, root: str | Path, allowed: set[str]) -> tuple[Path, ...]:
        root = Path(root).resolve()
        if not root.is_dir():
            return ()
        deleted = []
        for path in sorted(root.rglob('*')):
            if not path.is_file() or path.is_symlink():
                continue
            if self.recognize_filename(path.name, allowed) is not None:
                path.unlink()
                deleted.append(path)
        return tuple(deleted)

    def identifier_convention(self, profile_name: str, structure: str) -> str | None:
        """Return the declared identifier conversion kind of one naming structure."""
        for segment in self.segments(profile_name, structure):
            if segment.get('Kind') == 'identifier':
                return segment.get('Convention')
        return None

    def format(self, profile_name: str, structure: str, variables: Mapping[str, str],
               source_convention: str | None = None) -> str:
        parts = []
        for segment in self.segments(profile_name, structure):
            kind = segment['Kind']
            if kind == 'constant_string':
                parts.append(segment['Value'])
                continue
            name = 'identifier' if kind == 'identifier' else 'version' if kind == 'version' else segment['Name']
            if name not in variables:
                raise ValueError(f'Missing naming variable: {name}')
            value = str(variables[name])
            if kind == 'identifier' and source_convention and 'Convention' in segment:
                value = self.convert_identifier(value, source_convention, segment['Convention'])
            if not re.fullmatch(self._segment_pattern(segment), value) if kind == 'variable' else not self._capture_valid(segment, value):
                raise ValueError(f'Invalid naming variable {name}={value!r} for {profile_name}/{structure}')
            parts.append(value)
        return ''.join(parts)

    def _capture_valid(self, segment: dict, value: str) -> bool:
        return re.fullmatch(self._segment_pattern(segment), value) is not None

    @staticmethod
    def convert_identifier(value: str, source: str, target: str) -> str:
        if source == target:
            return value
        if source.endswith('_snake_case_1'):
            words = value.split('_')
        elif source.endswith('_kebab_case_1'):
            words = value.split('-')
        elif source.endswith('_camel_case_1'):
            words = _CAMEL_BOUNDARY.split(value)
        elif source in ('all_lower_1', 'all_upper_1'):
            words = [value]
        else:
            raise ValueError('Custom identifier convention cannot automatically convert: ' + source)
        words = [word.lower() for word in words]
        if target == 'all_lower_1':
            return ''.join(words)
        if target == 'all_upper_1':
            return ''.join(words).upper()
        if target == 'lower_snake_case_1':
            return '_'.join(words)
        if target == 'upper_snake_case_1':
            return '_'.join(words).upper()
        if target == 'lower_kebab_case_1':
            return '-'.join(words)
        if target == 'upper_kebab_case_1':
            return '-'.join(words).upper()
        if target in ('lower_camel_case_1', 'upper_camel_case_1'):
            return (words[0] if target == 'lower_camel_case_1' else words[0].capitalize()) + ''.join(w.capitalize() for w in words[1:])
        raise ValueError('Unsupported formatter: ' + target)

    def convert(self, value: str, source_profile: str, target_catalog: AIcNameConventionCatalog_1,
                target_profile: str, structure: str, source_structure: str | None = None) -> str:
        source_structure = source_structure or structure
        vars = self.parse(source_profile, source_structure, value)
        source_ident = next((s for s in self.segments(source_profile, source_structure) if s['Kind'] == 'identifier'), None)
        origin = source_ident.get('Convention') if source_ident else None
        return target_catalog.format(target_profile, structure, vars, origin)
