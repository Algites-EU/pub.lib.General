from __future__ import annotations

from importlib import resources
from pathlib import Path
import re
from typing import Any, Callable, Mapping
import yaml

from eu.algites.lib.naming.convention.impl.standard.aicd_name_match import AIcdNameMatch
from eu.algites.lib.naming.convention.aii_name_convention_catalog import AIiNameConventionCatalog


_NAMED_CAPTURE = re.compile(r'\(\?<([A-Za-z][A-Za-z0-9_]*)>')
_CAMEL_BOUNDARY = re.compile(r'(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])')


class AIcNameConventionCatalog(AIiNameConventionCatalog):
    """YAML-driven, language-independent name composition engine.

    Java-style (?<name>...) captures in YAML are translated to Python's (?P<name>...).
    Only file_name_stem + file_name_suffix is used to identify a file's profile.
    """

    def __init__(self, config: Mapping[str, Any]):
        self._config = dict(config)
        self._fragments = config.get('NameFragments', {})
        self._compositions = config.get('NameCompositions', {})
        self._conventions: dict[str, Any] = {}
        self._families: dict[str, str] = {}
        for section, definitions in config.items():
            if section == 'NameConventions' and isinstance(definitions, Mapping):
                self._register_profiles(definitions, section)
            elif isinstance(definitions, Mapping) and isinstance(definitions.get('NameConventions'), Mapping):
                self._register_profiles(definitions['NameConventions'], section)
        # Compatibility aliases only. Domain semantics belong in the consuming tool.
        self._generated_methods = dict(config.get('GeneratedDataObjectMethodNameConventions', {}).get('NameConventions', {}))
        self._generated_method_profile = None
        self._identifiers = config.get('IdentifierConventions', {})
        self._versions = config.get('VersionConventions', {})
        for section, definitions in (('NameFragments', self._fragments),
                                     ('NameCompositions', self._compositions)):
            for name, definition in definitions.items():
                for index, segment in enumerate(definition.get('Segments', ())):
                    self._validate_segment(segment, f'{section}/{name}/Segments[{index}]')
        self._expanded: dict[str, tuple[dict, ...]] = {}
        self._patterns: dict[str, re.Pattern] = {}
        for name, definition in self._conventions.items():
            keys = definition.get('StructureNaming', {})
            if 'file_name_stem' in keys and 'file_name_suffix' in keys:
                self.filename_pattern(name)

    @staticmethod
    def _validate_segment(segment: Mapping[str, Any], location: str) -> None:
        """Enforce the four mutually exclusive naming forms, including inline regex captures.

        XML Schema 1.0 cannot tie the value of Kind to the selected content branch,
        so every runtime catalog enforces the same semantics as JSONDEFS/YAMLDEFS.
        """
        if not isinstance(segment, Mapping):
            raise ValueError(f'{location}: segment must be a mapping')
        keys = set(segment)
        if 'Ref' in keys:
            if keys != {'Ref'} or not isinstance(segment['Ref'], str) or not segment['Ref']:
                raise ValueError(f'{location}: Ref segment must contain only a nonempty Ref')
            return
        kind = segment.get('Kind')
        if kind == 'constant_string':
            allowed = {'Kind', 'Value'}
        elif kind in ('identifier', 'version'):
            selected = {'Convention', 'CustomConventionRegexp'} & keys
            if len(selected) != 1:
                raise ValueError(f'{location}: {kind} requires exactly one of Convention and CustomConventionRegexp')
            allowed = {'Kind', *selected}
        elif kind == 'variable':
            allowed = {'Kind', 'Name', 'Regexp'}
        else:
            raise ValueError(f'{location}: unsupported or missing segment Kind: {kind!r}')
        if keys != allowed:
            raise ValueError(f'{location}: {kind} expects {sorted(allowed)}; got {sorted(keys)}')
        for name in allowed - {'Kind', 'Value'}:
            if not isinstance(segment[name], str) or not segment[name]:
                raise ValueError(f'{location}: {name} must be a nonempty string')
        if kind == 'constant_string' and not isinstance(segment['Value'], str):
            raise ValueError(f'{location}: Value must be a string')
        if kind == 'variable' and not re.fullmatch(r'[A-Za-z][A-Za-z0-9_]*', segment['Name']):
            raise ValueError(f'{location}: invalid variable Name: {segment["Name"]!r}')
        if 'CustomConventionRegexp' in keys:
            pattern = segment['CustomConventionRegexp']
            expected = kind
            AIcNameConventionCatalog._validate_regexp(pattern, expected, location)
        elif kind == 'variable':
            AIcNameConventionCatalog._validate_regexp(segment['Regexp'], None, location)

    @staticmethod
    def _validate_regexp(expression: str, required_group: str | None, location: str) -> None:
        """Validate Java-style named capture syntax with the Python regex engine."""
        converted = _NAMED_CAPTURE.sub(r'(?P<\1>', expression)
        try:
            compiled = re.compile(converted)
        except re.error as exc:
            raise ValueError(f'{location}: invalid regular expression {expression!r}: {exc}') from exc
        if required_group is not None and required_group not in compiled.groupindex:
            raise ValueError(f'{location}: regular expression must define named capture {required_group!r}')

    def _register_profiles(self, entries: Mapping[str, Any], section: str) -> None:
        """Register profile families without knowing their application or role vocabulary."""
        for name, definition in entries.items():
            if name in self._conventions and self._conventions[name] != definition:
                raise ValueError(f'Conflicting naming profile: {name}')
            self._conventions[name] = definition
            self._families[name] = section

    @property
    def profile_names(self) -> tuple[str, ...]:
        """All profiles, including non-file-naming declarations."""
        return tuple(self._conventions)

    def profile_value(self, name: str, section: str, key: str) -> str:
        """Return an opaque role value from a named profile section."""
        definition = self.profile(name)
        mapping = definition.get(section, {}) if section else definition
        return mapping.get(key, '') if isinstance(mapping, Mapping) else ''

    def composition_reference(self, profile: str, role: str) -> str:
        """Resolve a structure role without interpreting what that role represents."""
        return self.profile_value(profile, 'StructureNaming', role)

    def schema_metadata_fields(self, profile: str) -> dict[str, str]:
        """Deprecated compatibility for existing generators; use profile_value instead."""
        definition = self.profile(profile)
        mapping = definition.get('SchemaMetadataNaming', definition)
        return {f'{side}_schema_{suffix}': mapping.get(f'{side}_schema_{suffix}', '')
                for side in ('schema_source', 'data_source')
                for suffix in ('url', 'identifier', 'version')}

    @classmethod
    def _load_document(cls, filename: str, read_text: Callable[[str], str]) -> AIcNameConventionCatalog:
        """Resolve a YAML catalog and its standard sibling using one resource provider."""
        config = yaml.safe_load(read_text(filename))
        if not isinstance(config, dict):
            raise ValueError(f'Name conventions document must be a mapping: {filename}')
        if 'StandardConventionsFile' in config:
            standard_name = config['StandardConventionsFile']
            if not isinstance(standard_name, str) or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*\.yml', standard_name):
                raise ValueError(f'Invalid standard conventions resource name: {standard_name!r}')
            try:
                standard_text = read_text(standard_name)
            except FileNotFoundError:
                standard_text = cls._read_standard_resource(standard_name)
            standard = yaml.safe_load(standard_text)
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

    @staticmethod
    def _read_standard_resource(filename: str) -> str:
        """Resolve shared naming grammar exclusively from the intf distribution.

        A profile's YAML file may live in a separate wheel; its standard naming
        rules are packaged just once by the naming-convention intf product.
        Source checkouts may expose intf/src/product/config as an import root.
        """
        providers = (
            ('eu.algites.lib.naming.convention', ()),
            ('algites.pub.lib.general.naming.convention.intf',
             ('eu', 'algites', 'lib', 'naming', 'convention')),
        )
        for package, subpath in providers:
            try:
                resource = resources.files(package).joinpath(*subpath, filename)
            except (ModuleNotFoundError, TypeError):
                continue
            if resource.is_file():
                return resource.read_text(encoding='utf-8')
        raise FileNotFoundError(f'Missing shared naming conventions from intf artifact: {filename}')

    @classmethod
    def load(cls, path: str | Path) -> AIcNameConventionCatalog:
        """Load an explicitly selected external catalog without searching parent directories."""
        path = Path(path)
        return cls._load_document(path.name, lambda name: (path.parent / name).read_text(encoding='utf-8'))

    @classmethod
    def load_resource(cls, filename: str) -> AIcNameConventionCatalog:
        """Read the resource from the importable Python package or its Modustro resource bundle.

        This uses Python package-resource lookup, never the source-repository layout.
        Modustro publishes config resources under this artifact's isolated resource
        namespace. A local Python environment may also expose the config source
        root directly as a namespace package on sys.path.
        """
        if not isinstance(filename, str) or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*\.yml', filename):
            raise ValueError(f'Invalid naming catalog resource filename: {filename!r}')
        from importlib.metadata import distributions
        providers = [('eu.algites.lib.naming.convention', ('sourcecode',))]
        for distribution in distributions():
            name = distribution.metadata.get('Name', '').replace('_', '-').lower()
            prefix = 'eu-algites-pub-lib-general-naming-convention-'
            if name.startswith(prefix):
                providers.append((f'algites.pub.lib.general.naming.convention.{name[len(prefix):].replace("-", ".")}',
                                  ('eu', 'algites', 'lib', 'naming', 'convention', 'sourcecode')))
        for package, subpath in providers:
            try:
                root = resources.files(package).joinpath(*subpath)
            except (ModuleNotFoundError, TypeError):
                continue
            if root.joinpath(filename).is_file():
                return cls._load_document(filename, lambda name: root.joinpath(name).read_text(encoding='utf-8'))
        raise FileNotFoundError(
            f'YAML naming resource {filename!r} not found in the installed '
            'naming artifact or its configured Python resource roots'
        )

    @classmethod
    def load_many(cls, sources) -> AIcNameConventionCatalog:
        """Load an ordered set of file/resource catalogs without permitting conflicting IDs.

        An identifier defined identically in multiple files is shared. Distinct definitions
        of one identifier are always an error; file order never grants precedence.
        """
        merged: dict[str, Any] = {section: {} for section in (
            'IdentifierConventions', 'VersionConventions', 'NameFragments',
            'NameCompositions', 'NameConventions')}
        if not sources:
            raise ValueError('At least one naming convention catalog is required')
        for source in sources:
            value = str(source)
            catalog = cls.load_resource(value.removeprefix('resource:')) if value.startswith('resource:') else cls.load(value)
            for section, section_data in catalog._config.items():
                if section == '$schema':
                    continue
                if not isinstance(section_data, Mapping):
                    if section in merged and merged[section] != section_data:
                        raise ValueError(f'Conflicting catalog field {section!r}')
                    merged[section] = section_data
                    continue
                if section != 'NameConventions' and isinstance(section_data.get('NameConventions'), Mapping):
                    target = merged.setdefault(section, {}).setdefault('NameConventions', {})
                    entries = section_data['NameConventions']
                else:
                    target = merged.setdefault(section, {})
                    entries = section_data
                for name, definition in entries.items():
                    if name in target and target[name] != definition:
                        raise ValueError(f'Conflicting naming definition {section}/{name!r} in {source}')
                    target[name] = definition
        return cls(merged)

    @property
    def names(self) -> tuple[str, ...]:
        """Filename-capable profiles, matching the Java catalog API."""
        return tuple(self._patterns)

    @property
    def generated_method_names(self) -> tuple[str, ...]:
        """Profiles used for generated DO/SDO methods, not for identifying files."""
        return tuple(self._generated_methods)

    def profile(self, name: str) -> Mapping[str, Any]:
        return self._conventions[name] if name in self._conventions else self._missing_profile(name)

    @staticmethod
    def _missing_profile(name: str):
        raise KeyError(f'Unknown naming profile: {name}')

    @property
    def generated_method_profile(self) -> str | None:
        """Return the selected generated-method naming profile, if any."""
        return self._generated_method_profile

    def generated_method_reference(self, role: str) -> str | None:
        """Return the selected composition ID, or None for a disabled optional raw method.

        An omitted/empty raw role explicitly suppresses both its abstract declaration
        and its concrete implementation.  Callers must not synthesize default names.
        """
        if self._generated_method_profile is None:
            return None
        return (self._generated_methods[self._generated_method_profile]
                .get('StructureNaming', {}).get(role) or None)

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
                self._validate_segment(segment, f'composition {name}')
                results.append(dict(segment))
        return tuple(results)

    def with_generated_method_profile(self, profile: str):
        """Select one generated DO/SDO method profile while preserving source naming profiles."""
        if profile not in self._generated_methods:
            raise ValueError(f'Unknown generated DataObject method naming profile: {profile}')
        result = AIcNameConventionCatalog(self._config)
        result._generated_method_profile = profile
        return result

    def segments(self, profile_name: str, structure: str) -> tuple[dict, ...]:
        if self._generated_method_profile:
            # Transitional mapping kept only for code generators not migrated yet.
            aliases = {'getter_method_name': 'standard_getter',
                       'setter_method_name': 'standard_setter'}
            if structure in aliases:
                profile_name, structure = self._generated_method_profile, aliases[structure]
        reference = self.profile(profile_name)['StructureNaming'][structure]
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
                value = self.convert_identifier_by_declared_style(value, source_convention, segment['Convention'])
            if not re.fullmatch(self._segment_pattern(segment), value) if kind == 'variable' else not self._capture_valid(segment, value):
                raise ValueError(f'Invalid naming variable {name}={value!r} for {profile_name}/{structure}')
            parts.append(value)
        return ''.join(parts)

    def _capture_valid(self, segment: dict, value: str) -> bool:
        return re.fullmatch(self._segment_pattern(segment), value) is not None

    def convert_identifier_by_declared_style(self, value: str, source: str, target: str) -> str:
        """Use declared lexical conversion styles, not profile names, to convert identifiers."""
        if source == target:
            return value
        origin = self._identifiers[source].get('ConversionStyle')
        destination = self._identifiers[target].get('ConversionStyle')
        if not origin or not destination:
            raise ValueError(f'Automatic identifier conversion requires ConversionStyle: {source} -> {target}')
        return self._convert_style(value, origin, destination)

    @staticmethod
    def _convert_style(value: str, source_style: str, target_style: str) -> str:
        if source_style.endswith('_snake'):
            words = value.split('_')
        elif source_style.endswith('_kebab'):
            words = value.split('-')
        elif source_style.endswith('_camel'):
            words = _CAMEL_BOUNDARY.split(value)
        elif source_style.endswith('_flat'):
            words = [value]
        else:
            raise ValueError(f'Unsupported source lexical style: {source_style}')
        words = [word.lower() for word in words]
        if target_style == 'lower_flat':
            return ''.join(words)
        if target_style == 'upper_flat':
            return ''.join(words).upper()
        if target_style == 'lower_snake':
            return '_'.join(words)
        if target_style == 'upper_snake':
            return '_'.join(words).upper()
        if target_style == 'lower_kebab':
            return '-'.join(words)
        if target_style == 'upper_kebab':
            return '-'.join(words).upper()
        if target_style in ('lower_camel', 'upper_camel'):
            return (words[0] if target_style == 'lower_camel' else words[0].capitalize()) + ''.join(w.capitalize() for w in words[1:])
        raise ValueError(f'Unsupported target lexical style: {target_style}')

    @staticmethod
    def convert_identifier(value: str, source: str, target: str) -> str:
        """Legacy conversion using historical built-in IDs; retained for current tool clients."""
        legacy = {'all_lower_1': 'lower_flat', 'all_upper_1': 'upper_flat',
                  'lower_snake_case_1': 'lower_snake', 'upper_snake_case_1': 'upper_snake',
                  'lower_camel_case_1': 'lower_camel', 'upper_camel_case_1': 'upper_camel',
                  'lower_kebab_case_1': 'lower_kebab', 'upper_kebab_case_1': 'upper_kebab'}
        if source == target:
            return value
        return AIcNameConventionCatalog._convert_style(value, legacy[source], legacy[target])

    def convert(self, value: str, source_profile: str, target_catalog: AIcNameConventionCatalog,
                target_profile: str, structure: str, source_structure: str | None = None) -> str:
        source_structure = source_structure or structure
        vars = self.parse(source_profile, source_structure, value)
        source_ident = next((s for s in self.segments(source_profile, source_structure) if s['Kind'] == 'identifier'), None)
        origin = source_ident.get('Convention') if source_ident else None
        return target_catalog.format(target_profile, structure, vars, origin)
