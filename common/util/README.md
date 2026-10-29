# Algites Common (Java 21 and Python 3.13)

This artifact is published for both `java` and `python`. Each of the original Java
public types has a counterpart under `src/product/python` with the matching
Algites type name and Python snake-case module name. Python counterparts expose
the Java-named public operations where meaningful; standard Python collection
operations are supported on the bounded list as well.

## Validation and bounded collection

- `eu.algites.lib.common.validation.AIiValidatable`: current-state validity and
  locale-dependent diagnostic. An incomplete object may exist until validation.
- `eu.algites.lib.common.list.AInBoundsMutationPolicy`: `ALLOW_ANY` or
  `PREVENT_WORSENING` (default).
- `eu.algites.lib.common.list.AIcBoundedList<T>`: constrained mutable list.

The list accepts nullable inclusive `Integer` minimum/maximum bounds or an
`AIiInterval<Integer>` with closed/open/unbounded endpoints. Open integer
endpoints are rounded to the nearest admitted integer cardinality. A list may
begin below its required minimum. `isValid()` tests both limits regardless of
mutation policy. The maximum may be `null` (unbounded).

`PREVENT_WORSENING` prevents increases in either size-bound violation. For
example, adding to an undersized list or removing from an oversized list is
allowed; crossing from a valid size outside the bounds is not. In `ALLOW_ANY`,
any cardinality change can take place and subsequent validation reports whether
the state is valid. In either policy, no bounds are applied to the data values.

`modifyAtomically(Consumer<List<T>>)` works on a detached temporary list and
checks its **final size**. A thrown exception, including one from the callback,
leaves the original contents unchanged. Collection operations such as `addAll`,
`removeAll`, `retainAll`, `removeIf` and `subList().clear()` are likewise
all-or-nothing with regard to cardinality checks. This is a transactional
mutation method, **not** a synchronization primitive for concurrent access.

Java's two-argument constructors `(Integer,Integer)` and
`(AIiInterval<Integer>,AInBoundsMutationPolicy)` are necessarily ambiguous for
`(null,null)`. Use the parameterless constructor for the unbounded case, or
explicitly cast at least one null to `Integer`.

Python uses `None` for an unspecified limit and accepts `(min,max[,policy])`
or `(interval[,policy])`. The Python list implements `MutableSequence` and offers
`add`, `addAll`, `removeAll`, `retainAll`, `removeIf`, and `modifyAtomically`;
standard `.remove(value)` and `.pop(index)` retain their Python meanings.

## SmartDataObject compatibility

`AIiSmartDataObject` now also implements `AIiValidatable`. Its generic validity
reflects existing **deep effective-value** validation; raw/effective and
local/deep variants are preserved. Deep validation additionally incorporates
`AIiValidatable` containers such as `AIcBoundedList`. Local SDO validation is
unchanged, and internal validation diagnostics retain paths and codes.

## Parity notes

The existing Java UID utility has some surprising API behavior, including
`createCustomUid` delegating to the builtin origin. This is mirrored for
compatibility in Python rather than silently corrected. Python exposes
`AIrUidPartsRecord.origin()` and `.namespace()` like the Java record, while
Python's `findByUid()` uses `None` rather than Java `Optional.empty()`.
Java annotations have Python decorator/metadata equivalents; naturally, Python
reflection does not model Java records and interfaces identically.

## Generic validation issues and locale semantics

`AIiValidatable<T extends AIiValidationIssue>` exposes typed structured issues
via `getValidationIssues()`; `isValid()` and `getInvalidStateMessage(Locale)`
remain available. `AIiValidationIssue` exposes a stable diagnostic `getCode()`,
`getPath()`, `getMessage(Locale)` and extensible related-text topics via
`getRelatedTopicText(String, Locale)`. Unknown topic identifiers return `null`.

**Locale is deliberately not implicit:** passing Java `null` / Python `None`
requests low-level technical diagnostics, independent of the host locale.
An explicit supported locale uses the requested translation; unsupported
non-null locales fall back to **English**, not to the JVM/Python system locale.
An unset or unresolvable related-text topic is still `null` and is not a
localization fallback. Python represents locales as strings such as `cs-CZ`.

`AIcBoundedList` returns `AIcdBoundedListValidationIssue` values carrying the
actual and boundary sizes and the stable `MIN_ITEMS` / `MAX_ITEMS` codes.
The low-level `getMessage(null)` contains this structured cardinality data.
