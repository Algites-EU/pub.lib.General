# Documentation model public interfaces

Public `AIiDocumentation*` read-only and write-access contracts under the
existing `eu.algites.lib.common.documentation.model` package. No concrete
implementations are required on this artifact's classpath.

The public XML documentation vocabulary is defined in
`src/product/xmldefs/eu/algites/lib/common/documentation/algites-documentation_1.xsd`
(namespace `urn:eu:algites:documentation:1`). Other XML Schema documents
can import its published schema at
`https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/common/documentation/algites-documentation_1.xsd`
and use `algdoc:title` inside `xs:annotation/xs:appinfo`. Standard
`xs:documentation` remains the source of `description`.
