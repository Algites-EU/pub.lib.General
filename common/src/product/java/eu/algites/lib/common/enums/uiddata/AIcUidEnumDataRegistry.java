package eu.algites.lib.common.enums.uiddata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default singleton implementation of {@link AIiUidEnumDataRegistry}.
 *
 * @author linhart1
 * @date 30.01.26
 */
public class AIcUidEnumDataRegistry implements AIiUidEnumDataRegistry {

	private static final AIcUidEnumDataRegistry INSTANCE = new AIcUidEnumDataRegistry();

	private final Object lockObject = new Object();

	private final IdentityHashMap<AIiUidEnumDataType<?, ?>, Map<String, AIcRegistryEntry>> entriesByDataType
			= new IdentityHashMap<>();

	/**
	 * Creates a uid enum data registry instance.
	 */
	protected AIcUidEnumDataRegistry() {
	}

	/**
	 * Gets the registry instance.
	 * @return registry instance
	 */
	public static AIcUidEnumDataRegistry getInstance() {
		return INSTANCE;
	}

	/** {@inheritDoc} */
	@Override
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> registerData(
			final boolean aReadOnly,
			final boolean aPermanent,
			final boolean aOverwriteExisting,
			final T... aDataToRegister
	) throws AIxEnumDataRegistryIllegalOperationException {

		if (aDataToRegister == null) {
			throw new IllegalArgumentException("Data to register must not be null");
		}

		final List<T> locRegisteredItems = new ArrayList<>();

		synchronized (lockObject) {
			for (final T locDataToRegister : aDataToRegister) {
				if (locDataToRegister == null) {
					continue;
				}

				final AIiUidEnumDataType<?, ?> locDataType = locDataToRegister.getDataType();
				if (locDataType == null) {
					throw new IllegalArgumentException("Data type must not be null for item " + locDataToRegister);
				}

				final String locUid = locDataToRegister.uid();
				AIsUidEnumDataUtils.validateUid((AIiUidEnumDataType) locDataType, locUid);

				final Map<String, AIcRegistryEntry> locByUidMap = entriesByDataType.computeIfAbsent(
						locDataType,
						aK -> new java.util.HashMap<>()
				);

				final AIcRegistryEntry locExistingEntry = locByUidMap.get(locUid);
				if (locExistingEntry != null) {
					if (locExistingEntry.isReadOnly()) {
						throw new AIxEnumDataRegistryIllegalOperationException(
								() -> "Cannot overwrite read-only item for uid '" + locUid + "' in data type '" + locDataType + "'"
						);
					}

					if (!aOverwriteExisting) {
						continue;
					}
				}

				locByUidMap.put(locUid, new AIcRegistryEntry(locDataToRegister, aReadOnly, aPermanent));
				locRegisteredItems.add(locDataToRegister);
			}
		}

		return locRegisteredItems;
	}

	/** {@inheritDoc} */
	@Override
	@SafeVarargs
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> releaseData(
			final T... aDataToRelease
	) throws AIxEnumDataRegistryIllegalOperationException {

		if (aDataToRelease == null) {
			throw new IllegalArgumentException("Data to release must not be null");
		}

		final List<T> locReleasedItems = new ArrayList<>();

		synchronized (lockObject) {
			for (final T locDataToRelease : aDataToRelease) {
				if (locDataToRelease == null) {
					continue;
				}

				final DT locDataType = locDataToRelease.getDataType();
				if (locDataType == null) {
					throw new IllegalArgumentException("Data type must not be null for item " + locDataToRelease);
				}

				final String locUid = locDataToRelease.uid();
				final T locReleasedItem = releaseByTypeAndUid(locDataType, locUid);
				if (locReleasedItem != null) {
					locReleasedItems.add(locReleasedItem);
				}
			}
		}

		return locReleasedItems;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> releaseData(
			final DT aDataType,
			final String... aUidToRelease
	) throws AIxEnumDataRegistryIllegalOperationException {

		if (aDataType == null) {
			throw new IllegalArgumentException("Data type must not be null");
		}
		if (aUidToRelease == null) {
			throw new IllegalArgumentException("Uids to release must not be null");
		}

		final List<T> locReleasedItems = new ArrayList<>();

		synchronized (lockObject) {
			for (final String locUid : aUidToRelease) {
				if (locUid == null) {
					continue;
				}

				AIsUidEnumDataUtils.validateUid((AIiUidEnumDataType) aDataType, locUid);

				final AIiUidEnumData<?, ?, ?> locReleasedItem = releaseByTypeAndUid(aDataType, locUid);
				if (locReleasedItem != null) {
					locReleasedItems.add((T) locReleasedItem);
				}
			}
		}

		return locReleasedItems;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> Optional<T> findByUid(
			final DT aDataType,
			final String aUid
	) {

		if (aDataType == null) {
			throw new IllegalArgumentException("Data type must not be null");
		}
		if (aUid == null) {
			throw new IllegalArgumentException("Uid must not be null");
		}

		AIsUidEnumDataUtils.validateUid((AIiUidEnumDataType) aDataType, aUid);

		synchronized (lockObject) {
			final Map<String, AIcRegistryEntry> locByUidMap = entriesByDataType.get(aDataType);
			if (locByUidMap == null) {
				return Optional.empty();
			}

			final AIcRegistryEntry locEntry = locByUidMap.get(aUid);
			if (locEntry == null) {
				return Optional.empty();
			}

			return Optional.of((T) locEntry.getData());
		}
	}

	/** {@inheritDoc} */
	@Override
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> T getOrThrow(
			final DT aDataType,
			final String aUid
	) throws AIxEnumDataRegistryItemNotFoundException {

		final Optional<T> locItem = findByUid(aDataType, aUid);
		if (locItem.isEmpty()) {
			throw new AIxEnumDataRegistryItemNotFoundException(
					() -> "Item not found for uid '" + aUid + "' in data type '" + aDataType + "'"
			);
		}
		return locItem.get();
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public final <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> items(
			final DT aDataType
	) {

		if (aDataType == null) {
			throw new IllegalArgumentException("Data type must not be null");
		}

		synchronized (lockObject) {
			final Map<String, AIcRegistryEntry> locByUidMap = entriesByDataType.get(aDataType);
			if (locByUidMap == null) {
				return List.of();
			}

			final Collection<T> locItems = new ArrayList<>();
			for (final AIcRegistryEntry locEntry : locByUidMap.values()) {
				locItems.add((T) locEntry.getData());
			}
			return List.copyOf(locItems);
		}
	}

	@SuppressWarnings("unchecked")
	private <DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> T releaseByTypeAndUid(
			final DT aDataType,
			final String aUid
	) throws AIxEnumDataRegistryIllegalOperationException {

		AIsUidEnumDataUtils.validateUid((AIiUidEnumDataType) aDataType, aUid);

		final Map<String, AIcRegistryEntry> locByUidMap = entriesByDataType.get(aDataType);
		if (locByUidMap == null) {
			return null;
		}

		final AIcRegistryEntry<DT, T> locEntry = locByUidMap.get(aUid);
		if (locEntry == null) {
			return null;
		}

		if (locEntry.isPermanent()) {
			throw new AIxEnumDataRegistryIllegalOperationException(
					() -> "Cannot release permanent item for uid '" + aUid + "' in data type '" + aDataType + "'"
			);
		}

		locByUidMap.remove(aUid);
		return locEntry.getData();
	}

	private static final class AIcRegistryEntry<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> {

		private final T data;
		private final boolean readOnly;
		private final boolean permanent;

		private AIcRegistryEntry(
				final T aData,
				final boolean aReadOnly,
				final boolean aPermanent
		) {
			data = aData;
			readOnly = aReadOnly;
			permanent = aPermanent;
		}

		private T getData() {
			return data;
		}

		private boolean isReadOnly() {
			return readOnly;
		}

		private boolean isPermanent() {
			return permanent;
		}
	}
}
