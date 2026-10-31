"""Regressions for shared naming-convention schemas and their canonical identity."""

from __future__ import annotations

import json
from pathlib import Path

import pytest
import yaml


MODULE = Path(__file__).resolve().parents[3]
SCHEMAS = MODULE / "src/product"
IDENTITY = "eu.algites.lib.naming.convention"


def definition(kind: str, stem: str) -> dict:
    """Read one checked-in JSONDEFS/YAMLDEFS definition, without generated files."""
    ext = "jsondef" if kind == "jsondefs" else "yamldef"
    source = SCHEMAS / kind / "eu/algites/lib/naming/convention" / (
        f"{stem}_1.{ext}.schema.json")
    return json.loads(source.read_text(encoding="utf-8"))


@pytest.mark.parametrize("kind", ["jsondefs", "yamldefs"])
def test_standard_catalog_only_references_shared_field_definitions(kind: str) -> None:
    """The standard catalog tightens presence, without copying nested structures."""
    common = definition(kind, "common-naming-conventions")
    standard = definition(kind, "standard-conventions")
    assert standard["allOf"] == [{"$ref": common["$id"]}]
    assert standard["required"] == ["IdentifierConventions", "VersionConventions"]
    for name, schema in standard["properties"].items():
        pointer = ("#/$defs/" if name in ("IdentifierConventions", "VersionConventions")
                   else "#/properties/") + name
        assert schema == {"$ref": common["$id"] + pointer}
    for name in ("IdentifierConventions", "VersionConventions"):
        assert common["properties"][name] == {"$ref": "#/$defs/" + name}
        assert common["$defs"][name]["type"] == "object"
        assert common["$defs"][name]["additionalProperties"]["required"] == ["Regexp"]
    assert standard["additionalProperties"] is False
    assert common["x-algites-schema-id"] == IDENTITY + ".common-naming-conventions"
    assert standard["x-algites-schema-id"] == IDENTITY + ".standard-conventions"
    assert common["x-algites-schema-version"] == standard["x-algites-schema-version"] == 1


@pytest.mark.parametrize("kind", ["jsondefs", "yamldefs"])
def test_standard_catalog_requires_both_sections_but_common_does_not(kind: str) -> None:
    """A complete standard library is stricter than an independently extensible profile."""
    referencing = pytest.importorskip("referencing")
    jsonschema = pytest.importorskip("jsonschema")
    common = definition(kind, "common-naming-conventions")
    standard = definition(kind, "standard-conventions")
    registry = referencing.Registry().with_resource(
        common["$id"], referencing.Resource.from_contents(common))
    standard_validator = jsonschema.Draft202012Validator(standard, registry=registry)
    common_validator = jsonschema.Draft202012Validator(common, registry=registry)
    catalog_path = SCHEMAS / "config/eu/algites/lib/naming/convention/standard-conventions.yml"
    catalog = yaml.safe_load(catalog_path.read_text(encoding="utf-8"))
    assert standard_validator.is_valid(catalog)
    assert not standard_validator.is_valid({"IdentifierConventions": {}})
    assert not standard_validator.is_valid({"VersionConventions": {}})
    assert not standard_validator.is_valid({"IdentifierConventions": {}, "VersionConventions": {}, "Unknown": 1})
    assert common_validator.is_valid({"VersionConventions": {"v1": {"Regexp": "[0-9]+"}}})
    assert common_validator.is_valid({"StandardConventionsFile": "standard-conventions.yml"})


def test_xsd_restricts_shared_complex_type_without_repeated_shapes() -> None:
    """XML Schema 1.0 accepts required named sections on a restricted common type."""
    etree = pytest.importorskip("lxml.etree")
    root = SCHEMAS / "xmldefs/eu/algites/lib/naming/convention"
    common = etree.parse(str(root / "common-naming-conventions_1.xsd"))
    standard = etree.parse(str(root / "standard-conventions_1.xsd"))
    etree.XMLSchema(common)
    etree.XMLSchema(standard)
    ns = {"xs": "http://www.w3.org/2001/XMLSchema"}
    restriction = standard.xpath("//xs:complexType[@name='StandardConventions']/xs:complexContent/xs:restriction", namespaces=ns)
    assert len(restriction) == 1
    assert restriction[0].get("base") == "common:CommonNamingConventions"
    sections = restriction[0].xpath("xs:sequence/xs:element", namespaces=ns)
    assert [item.get("name") for item in sections] == ["IdentifierConventions", "VersionConventions"]
    assert all(item.get("minOccurs") is None for item in sections)
    assert all(not item.xpath("xs:complexType", namespaces=ns) for item in sections)
    assert common.getroot().get("targetNamespace") == IDENTITY + ".common-naming-conventions"
    assert standard.getroot().get("targetNamespace") == IDENTITY + ".standard-conventions"
