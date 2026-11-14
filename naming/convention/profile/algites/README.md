# Algites naming convention profile

Provides the standard Algites naming profiles for Java and Python while keeping the policy model reusable by external consumers.

The profile provider is `eu.algites.lib.naming.convention.profile.algites.AIcAlgitesNamingProfiles` in Java and `eu.algites.lib.naming.convention.profile.algites.aic_algites_naming_profiles` in Python. The reusable catalog interpreter and match type live only in `naming/convention/impl`.


Standard Java/Python naming catalogs are shipped as YAML resources.
Both Java and Python load the same declared compositions without a legacy naming profile adapter.


## Python naming YAML resource loading

The `src/product/python` and `src/product/python.gen` directories are Python
import roots, not roots of the surrounding source repository. The configuration
files live in this artifact's `src/product/config` resource root. On an installed
Python wheel, Modustro stages this root into the artifact's isolated package
namespace. `AIcAlgitesNamingProfiles.javaProfile()` / `pythonProfile()` (Java), or
`java_profile()` / `python_profile()` (Python) load those files through the generic
catalog. The referenced `standard-conventions.yml` is owned by the
`naming/convention/intf` artifact, not copied into this profile.

For local source-tree execution without an installed wheel, explicitly expose
`src/product/config` on `PYTHONPATH` together with the generic impl Python
source root and the `naming/convention/intf/src/product/config` shared resource root (or use `AIcNameConventionCatalog.load(path)` for a user-supplied YAML
file). The runtime never searches repository parents or depends on the repository
layout. Python namespace package paths below those roots are the same as when
running from a wheel.


## Format-independent DataObject output

The `algites`, `standardprefix` and `standardpostfix` profiles define one
read-only DataObject interface naming role, one mutable SDO interface role and
one concrete SDO implementation role per language. Their source-code output
names are independent of the originating XMLDEFS/JSONDEFS/YAMLDEFS dialect.
The corresponding naming selectors are `--dointf-naming-convention`,
`--sdointf-naming-convention` and `--sdoimpl-naming-convention`, each qualified
by Java or Python. Format-specific output profiles and synthetic `_xe` class
markers are obsolete and are not accepted as naming selections.

The separate `xmldef`, `jsondef` and `yamldef` naming catalogs remain required
for recognizing **input schema filenames**. XML QName/element and JSON/YAML
schema metadata are preserved by the Defs generator in annotations on the
single read-only contract, rather than by additional generated type names.
The catalog rejects ambiguously matching output filename patterns.
