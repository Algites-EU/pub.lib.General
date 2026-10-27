# Naming convention profiles

This artifact set contains declarative naming policies for the Algites, standard-prefix,
and standard-postfix naming systems. Its artifact children are located at:

- `profile/algites`: Algites policy resources and the `AIcAlgitesNamingProfiles` provider.
- `profile/standardprefix`: reusable prefix policy resources.
- `profile/standardpostfix`: reusable postfix policy resources.

The only general-purpose `AIcNameConventionCatalog` interpreter resides in the sibling
`impl` artifact. The interface, schema types, and the single authoritative
`standard-conventions.yml` file reside in sibling `intf`.

The YAML documents retain `StandardConventionsFile: standard-conventions.yml`; Java and
Python loaders resolve this shared resource from the `intf` dependency when no local
sibling file is present. This works with JAR/wheel publications as well as explicitly
configured source-package roots. Do not copy the shared standard YAML into the profiles.

The parent directory contributes to artifact IDs, e.g.
`pub.lib.General_naming.convention.profile.algites`. Consumers must migrate Maven
coordinates and Python distribution requirements together.
