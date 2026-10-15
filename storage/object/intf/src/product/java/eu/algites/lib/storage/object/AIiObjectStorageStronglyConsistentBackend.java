package eu.algites.lib.storage.object;

/**
 * Strong per-key read/conditional-write backend suitable as a primitive for
 * durable release-ledger control records, subject to further application-level
 * fencing, audit and authorization rules.
 *
 * <p>Every key operation is linearizable across all independent clients using
 * this physical namespace. Successful create/CAS acknowledgements are visible
 * to subsequent HEAD and GET calls; revision tokens uniquely identify stored
 * generations and never permit ABA acceptance after delete/recreate.
 * The implementation must establish these properties with the real backend,
 * not with a process-local lock or a client-side check-then-PUT.</p>
 *
 * <p>This interface does NOT promise atomic transactions across multiple keys,
 * lease enforcement, distributed fencing, or remote publication idempotency.</p>
 */
public interface AIiObjectStorageStronglyConsistentBackend
        extends AIiObjectStorageReader,
                AIiObjectStorageCreateOnlyWriter,
                AIiObjectStorageConditionalUpdater {
}
