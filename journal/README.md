# General Journal

`journal/intf` defines thread-safe append-only journal contracts. `journal/impl`
contains an in-memory implementation and TestNG regression tests.

## Read-only views and snapshots

`AIiReadOnlyJournal<E>` provides `getBatches()`, `getEntries()`, and `snapshot()`.
A read-only view may reflect ongoing writes through other references. Only
`AIiJournalSnapshot<E>` promises an immutable, atomically captured state. A
snapshot's `snapshot()` returns itself. Live read methods return independently
consistent unmodifiable views; obtain a snapshot to read both projections from
one state.

`AIiJournalEntry` instances are required to be immutable, including any nested
state they expose. The general journal cannot deep-copy arbitrary domain
objects, so applications must supply immutable entries or immutable snapshots
of their mutable values before appending. This is a contract, not a reflection-
based runtime verification.

## Atomic appends

`append` (array/varargs), `appendList` (list), and `appendSupplied`
(individual-entry suppliers) create exactly one batch. An `appendJournal`
operation atomically appends every batch from a source snapshot, preserving
boundaries and order. No required batch or entry IDs are imposed. Null entries
are invalid. Empty input is a no-op with no listener notifications.

Each operation has its own callback family: `beforeBatchAppend` and
`afterBatchAppend`, or `beforeJournalAppend` and `afterJournalAppend`.
Before callbacks can reject the entire append using the journal's generic
checked exception type; all before listeners see the same pre-append target
snapshot. They run under the per-journal serialization lock, so they must not
perform long-running work or attempt to append to the same journal.

After callbacks receive the confirmed post-append target snapshot and run
outside the journal's write lock, in commit order. They may append again, but
those nested notifications are queued until the current notification completes.
Failures of after callbacks cannot roll back the already committed append:
runtime exceptions are logged and other listeners are still notified. After
callbacks are *in-process notifications*, not persistent/durable subscriptions.
Durable delivery, recovery and cross-process serialization require a separate
persistent journal implementation, and are not claimed by this in-memory one.

Supplier evaluation is performed once, outside the write lock, in argument
order. If evaluation fails, no journal append or listener notification occurs.

## Building

The `journal/impl/src/develop/java` tests are native TestNG `@Test`
methods and run in the regular Modustro Gradle test lifecycle when its Java
toolchain and published builder dependencies are available.
