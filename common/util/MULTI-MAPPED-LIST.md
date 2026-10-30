# Multi-mapped list — library contract (Java and Python)

**Author:** `linhart1`  
**Artifact:** `pub.lib.General_common.util`  
**Packages:** Java `eu.algites.lib.common.list`, Python `eu.algites.lib.common.list`  
**Scope:** Standalone general-purpose collection. Defs Codegen integration is a *separate, future change*.

## 1. Motivation and model

`AIiMultiMappedList<V>` is a mutable Java `List<V>` that provides any number of
lookup views, each grouping the list's occurrences by an independently specified
key extractor. The list maintains the order of its elements; **no map iteration
order is guaranteed**. Keys may be simple immutable objects or immutable composite
records. Key types are selected at compile time through `AIiKeyDefinition<V,K>`.

The collection is useful for two distinct schema patterns:

1. An XML repeated element collection with one or more `xs:key` constraints.
   Key fields are **ordinary fields of the value object** and remain accessible
   from it; they are not stripped out of the value type.
2. A JSON/YAML object with dynamic property names (`additionalProperties`,
   `patternProperties`). The property name is the logical key and must be
   retained separately by the generated value model, because it does not
   appear inside the JSON property value itself.

Neither frontend mapping is implemented in this deliverable. The collection
is intentionally unaware of XML, JSON, YAML and Defs Codegen.

### Java usage

```java
record Employee(long id, String email, String city) {}
record LocationKey(String city, String email) {}

AIiKeyDefinition<Employee, Long> BY_ID =
    new AIcKeyDefinition<>("BY_ID", Employee::id, true);
AIiKeyDefinition<Employee, String> BY_CITY =
    new AIcKeyDefinition<>("BY_CITY", Employee::city, false);
AIiKeyDefinition<Employee, LocationKey> BY_LOCATION =
    new AIcKeyDefinition<>("BY_LOCATION",
        e -> new LocationKey(e.city(), e.email()), true);

AIiMultiMappedList<Employee> employees =
    new AIcMultiMappedList<>(BY_ID, BY_CITY, BY_LOCATION);
employees.add(new Employee(101, "a@example.org", "Prague"));
employees.add(new Employee(102, "b@example.org", "Prague"));

Map<Long, List<Employee>> byId = employees.mapByKeyType(BY_ID);
Map<String, List<Employee>> byCity = employees.mapByKeyType(BY_CITY);
List<Employee> pragueSnapshot = byCity.get("Prague");

byCity.remove("Prague"); // removes BOTH employees from the list and every map
```

### Python usage

```python
from dataclasses import dataclass
from eu.algites.lib.common.list.aic_key_definition import AIcKeyDefinition
from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList

@dataclass(frozen=True)
class Employee:
    id: int
    city: str

BY_ID = AIcKeyDefinition("BY_ID", lambda e: e.id, True)
BY_CITY = AIcKeyDefinition("BY_CITY", lambda e: e.city, False)
employees = AIcMultiMappedList((BY_ID, BY_CITY))
employees.append(Employee(101, "Prague"))
employees.append(Employee(102, "Prague"))
by_city = employees.map_by_key_type(BY_CITY)
snapshot = by_city["Prague"]  # immutable tuple of two values

del by_city["Prague"]          # removes both entries from the parent list
```

## 2. Public API, arguments, results and exceptions

### Java `AIiKeyDefinition<V,K>`

| Member | Arguments | Result | Contract and failures |
|---|---|---|---|
| `getId()` | none | Nonblank `String` identifier | Must be unique among a list's registered definitions. Do not mutate after registration. |
| `extractKey(V value)` | Non-null stored value | Non-null key `K` | Extractor must be deterministic and side-effect-free. Exceptions propagate to the attempted mutation; the list is not changed. Keys must have stable equality and hash codes. |
| `isUnique()` | none | `boolean` | When true, duplicate keys raise `IllegalArgumentException` during insertion or replacement. |

### Java `AIcKeyDefinition<V,K>`

`new AIcKeyDefinition(String id, Function<? super V,? extends K> extractor,
boolean unique)` stores its arguments immutably. Null arguments throw
`NullPointerException`, and a blank identifier throws `IllegalArgumentException`.
Definition objects use reference identity, *not* their `id` strings, for map
lookup. Two distinct definitions with the same id cannot be registered together.

### Java `AIcMultiMappedList<V>` and `AIiMultiMappedList<V>`

| Member | Arguments | Return | Behavior and errors |
|---|---|---|---|
| Constructor `()` | none | New empty list, no indexes | No additional indexes can be registered later. |
| Constructor `(Collection<definitions>)` | Definitions fixed for list lifetime | New empty list | Duplicate/blank ids fail with `IllegalArgumentException`; null definitions with `NullPointerException`. |
| Constructor `(definitions...)` | Varargs key definitions | New empty list | Same validation rules as collection constructor. |
| `mapByKeyType(definition)` | **Exact registered definition instance** of type `AIiKeyDefinition<V,K>` | `Map<K,List<V>>` | Cached live map; a different definition instance (even with same id) throws `IllegalArgumentException`; null throws `NullPointerException`. |
| `get(int)` | Index in `[0,size)` | Value at index | Standard `IndexOutOfBoundsException` for invalid position. |
| `add(V)` / `add(int,V)` | Non-null value, optional insertion position | Boolean / void | Rebuilds indexes. `NullPointerException` for null, `IllegalArgumentException` for unique conflict. |
| `addAll(...)` | Collection and optional index | Whether list changed | **Atomic**; if any key conflicts or extractor throws, no entries are added. |
| `set(int,V)` | Existing position, non-null replacement | Previous value | Replaces the occurrence in all views and indexes; checks all unique keys first. |
| `remove(int)` / `remove(Object)` | Position or equal value | Previous value / boolean | Removes one list occurrence and updates every map and active sublist. |
| `clear()` | none | void | Removes all occurrences from every view. |
| `removeAll(Collection)` / `retainAll(Collection)` / `removeIf(Predicate)` | Membership/filter | Whether list changed | Atomic on the parent list; predicate failures leave all collections unchanged. |
| `replaceAll(UnaryOperator)` | Value transformation | void | Atomically checks final index state; swapping two keys across positions is permitted. |
| `sort(Comparator)` | Comparator or null for natural ordering | void | Sorts parent occurrences, preserving their identity as members of sublists. |
| `subList(from,to)` | Valid half-open range | `List<V>` tracked live membership view | Parent additions don't enlarge membership; **any** removal propagates; sublist additions affect only the owning sublist and parent, not unrelated sublists. Range errors throw `IndexOutOfBoundsException`. |
| `copyRange(from,to)` | Valid half-open range | `AIiMultiMappedList<V>` | Independent list/maps, **shallow copy of value references**. Range errors throw `IndexOutOfBoundsException`. |

`List` additionally supplies familiar inherited `size`, `isEmpty`, `iterator`,
`listIterator`, `contains`, `indexOf`, `toArray` and other collection operations.
Structural mutations through iterators propagate through the same central path.
Iterators are fail-fast on many concurrent structural modifications but are **not**
a concurrency guarantee.

### Map views (`Map<K,List<V>>`)

The same map instance is returned on successive `mapByKeyType` calls with the
same registered definition object. It is not reconstructed for each read.

| Operation | Result or effect | Supported? |
|---|---|---|
| `get(key)` | **Immutable snapshot** list of group values, or `null` if absent | Yes |
| `containsKey(key)`, `size()` | Current map membership | Yes |
| `remove(key)` | Removes **all** matching occurrences everywhere and returns the removed snapshot; returns `null` if absent | Yes |
| `clear()` | Removes all entries from parent and every map/sublist | Yes |
| `keySet().remove(key)`, `values().remove(snapshot)`, `entrySet().iterator().remove()` | Removes the corresponding full group | Yes |
| `put(key,group)`, `putAll(...)`, `entry.setValue(...)` | Would bypass extraction of authoritative keys | **No:** `UnsupportedOperationException` |
| `get(key).add(...)`, `.remove(...)`, `.clear()` | Would modify a previously returned snapshot | **No:** `UnsupportedOperationException` |

The map's key order is unspecified; values **inside each snapshot group** are in
current list order. Old snapshots do not change when the parent list is edited.
Snapshot collections do not clone the value objects they contain. `map.remove`
can remove many elements; this is expected, including for non-unique keys.

A unique definition guarantees at most one list value in each group. It does
**not** change the public value type from `List<V>` to `V`.

### Python differences

Python does not expose Java `List` or Java `Map` interfaces. Its implementation
extends `MutableSequence[V]` and returns a cached `MutableMapping[K, tuple[V,...]]`.
The immutable `tuple` is the equivalent group snapshot; `del map[key]` removes
all matching occurrences. `sub_list(start,end)` creates a tracked membership
view, while ordinary Python slices yield **detached lists of references**.
`copy_range`, `replace_all` and `remove_if` mirror the Java operations. Python
`insert` follows Python index clamping and `ValueError`/`TypeError` conventions.
Python slice assignment is not supported by this implementation.

## 3. Key definitions and uniqueness

Every definition has one extractor, one unique nonblank id and one `unique`
flag. These are registered once at list creation. At insertion the container
computes every applicable key and checks every unique definition. The first
key definition does **not** take priority over later definitions: **all**
constraints apply. A non-unique definition groups multiple values with the
same key, rather than silently dropping duplicates.

A composite key should use an immutable Java `record`, or a frozen and hashable
Python dataclass / tuple. The standard value equality and hash code of those
objects cover all key components. There is no special case for single versus
composite keys in the container API.

`null`/`None` stored values and extracted keys are rejected. Invalid values,
failed extraction or collisions do not change the parent or any of its maps.
The collection is not thread-safe; external mutation of stored key attributes
violates the caller contract. Mutating the list through its supported methods,
including `set`, is valid: its indexes are rebuilt consistently.

## 4. Tracked sublists and memory management

Unlike the usual positional `AbstractList.subList` implementation, this
collection's sublists track **occurrence identity**. A duplicate value (two
entries that compare equal) is still two different occurrences, so deleting
one cannot accidentally delete the other sublist's member.

1. Creating `subList(1,3)` snapshots the **membership identities**, not the
   values; its items remain shared, and list changes via the view are backed.
2. Adding to the root list does not implicitly add to existing sublists.
3. Removing an occurrence from root, any map, any sublist, or an iterator removes
   that occurrence from all live sublists containing it.
4. Adding via a sublist inserts into both parent and that specific sublist. An
   unrelated existing sublist is not expanded automatically.
5. Nested `subList` creates another independent membership view; deletion still
   propagates across all views.
6. `set` and `replaceAll` replace the value of the existing logical occurrence
   while preserving its membership in existing sublists.
7. A sublist's empty membership keeps a positional insertion hint; inserting
   through the empty view places the new value near its original range.
8. `copyRange` creates an independent collection; subsequent membership
   modifications do not propagate between the original and copy.

The Java parent registers sublists using **`WeakReference` and
`ReferenceQueue`**, not a `WeakHashMap<List,...>`. Mutable lists have
content-dependent `equals`/`hashCode`, so using them as hash keys could cause
missed entries; two equal-content sublists must also remain distinct.
The Python implementation uses `weakref.WeakSet` of identity-based view objects.
No dedicated maintenance thread is required. An abandoned sublist is collectible
when its caller releases it; dead references are cleaned opportunistically.

The stronger sublist contract is deliberate. It is more predictable for active
map-based edits than the unspecified behavior of ordinary backed sublists after
external structural parent modifications. However, custom iterators may become
invalid on external changes; do not edit the parent concurrently while iterating.

## 5. Atomicity, snapshot semantics and cost

Changes use a candidate ordered list and rebuild all map-index groupings before
publication. This guarantees **all-or-nothing state changes for ordinary
single-threaded operations**, including `addAll`, `set`, `replaceAll` and most
bulk removals. Extractors and predicates must be pure: their external side
effects cannot be undone. There is no locking or concurrent modification safety.

Current cost is `O(number_of_definitions × total_list_size)` for a mutation,
plus membership-view propagation; map lookups are ordinarily hash-based `O(1)`
and cost `O(group_size)` to return a snapshot. This emphasizes correctness
before optimizing incremental index updates. Cloning the immutable snapshot
list for each `Map.get` is deliberate: callers cannot mutate a backing group.

## 6. Future Defs Codegen integration (NOT in this patch)

- XML `xs:key` may create one registered key definition per applicable key
  constraint. Simple field paths use the field's existing type; multiple
  `xs:field` declarations produce a composite immutable record/dataclass.
  All selected fields remain ordinary value fields. Other XSD constraints,
  including `xs:keyref`, require their own validation paths.
- JSON Schema `additionalProperties`/`patternProperties` can expose their
  dynamic property names through the **synthetic getter**, with proposed
  naming-convention setting
  **`multi_mapped_list_key_synthetic_getter: _getKey`**. The getter is synthetic,
  not the key itself. The key must be initialized during deserialization and
  must not appear as an extra JSON property during serialization. This setting
  is **planned, not yet installed** in codegen naming profiles.
- The four structural roles (`NAMED_VALUE`, `KEYED_VALUES`,
  `POSITION_TYPED_VALUE`, `SEQUENTIAL_VALUES`) remain a separate codegen task;
  this library implementation does not modify them or generated API sources.
- Although the container preserves list order, JSON object property order is
  not a semantically guaranteed component of JSON Schema. Callers must not
  require JSON ordering to be a stable cross-serialization identity.

## 7. Tests and verification

- Java: `AItcMultiMappedListTest` (TestNG), covering map caching, immutable
  snapshots, removal through maps, sublists, iterators, bulk changes,
  uniqueness, compound keys, rollback and value occurrence identity.
- Python: `test_multi_mapped_list.py` (pytest), covering the analogous
  collection and map contracts.
- Generated `java.gen` and `python.gen` trees are **not modified**.
