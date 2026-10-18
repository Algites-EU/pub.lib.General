# Naming convention implementation

Provides the standard Algites naming profiles for Java and Python while keeping the policy model reusable by external consumers.

Java package namespace: `eu.algites.lib.naming.convention`. Python uses the same namespace as a PEP 420 package and keeps each public `AI*` type in its own module.


Standard Java/Python naming catalogs are shipped as YAML resources.
Both Java and Python load the same declared compositions without a legacy naming profile adapter.


## Python naming YAML resource loading

The `src/product/python` and `src/product/python.gen` directories are Python
import roots, not roots of the surrounding source repository. The configuration
files live in this artifact's `src/product/config` resource root. On an installed
Python wheel, Modustro stages this root into the artifact's isolated package
namespace. `AIcNameConventionCatalog_1.standard("java" / "python")` reads those
packaged YAML resources using `importlib.resources`, including the referenced
`standard-conventions.yml` sibling.

For local source-tree execution without an installed wheel, explicitly expose
`src/product/config` on `PYTHONPATH` along with this artifact's Python source
roots (or use `AIcNameConventionCatalog_1.load(path)` for a user-supplied YAML
file). The runtime never searches repository parents or depends on the repository
layout. Python namespace package paths below those roots are the same as when
running from a wheel.


XML global element synthetic contracts use new `*-xml-element-intf` and
`*-xml-element-class` naming profiles for all preexisting interface/class
roles. `xml-element-marker` is `_xe` for generated Java and Python class
names; Python filename composites use `xml-element-file-marker` (`_xe_`)
after the already-underscored `aiig_`/`aicgd_` technical file prefix,
producing `aiig__xe_payment_1.py`. Existing names for non-XML types remain
unchanged. Every recognizer must reject ambiguously matching user-supplied
compositions; users of custom naming profiles are responsible for constructing
uniquely reversible patterns.
