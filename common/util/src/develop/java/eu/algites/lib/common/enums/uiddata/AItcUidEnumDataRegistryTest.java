package eu.algites.lib.common.enums.uiddata;

import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.LAST_UID_HEADER_PART_POSITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AIcUidEnumDataRegistry}.
 *
 * @author linhart1
 */
public class AItcUidEnumDataRegistryTest {

	private static final class AIcTestUidEnumDataType implements AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> {

		@SuppressWarnings("unchecked")
		private static final List<AIiUidPartMetadata<AInUidEnumDataOrigin>> SPECIFIC_UID_PARTS_METADATA = List.of(
				new AIrUidPartMetadata(() -> "classifier", Collections.emptyMap()),
				new AIrUidPartMetadata(() -> "fileType", Map.of(AInUidEnumDataOrigin.BUILTIN, true, AInUidEnumDataOrigin.CUSTOM, true))
		);

		@Override
		public BiFunction<String, List<String>, ? extends AIrTestUidEnumDataRecord> getUidRecordFactory() {
			return (BiFunction<String, List<String>, AIrTestUidEnumDataRecord>) (aUid, aParts)
					-> new AIrTestUidEnumDataRecord(
							aUid,
							AInUidEnumDataOrigin.getByCodeOrThrow(aParts.get(AIsUidEnumDataUtils.ORIGIN_UID_POSITION)),
							aParts.get(AIsUidEnumDataUtils.NAMESPACE_UID_POSITION),
							aParts.get(LAST_UID_HEADER_PART_POSITION + 1),
							aParts.get(LAST_UID_HEADER_PART_POSITION + 2)
					);
		}

		@Override
		public List<AIiUidPartMetadata<AInUidEnumDataOrigin>> getSpecificUidPartsMetadata() {
			return SPECIFIC_UID_PARTS_METADATA;
		}
	}

	public record AIrTestUidEnumDataRecord(
			String uid,
			AInUidEnumDataOrigin origin,
			String namespace,
			String classifier,
			String fileType
	) implements AIiUidEnumDataRecord { }

	private static final class AIcTestData implements AIiUidEnumData<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin, AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin>> {

		private final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> dataType;
		private final String uid;

		private AIcTestData(
				final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> aDataType,
				final String aUid
		) {
			dataType = aDataType;
			uid = aUid;
		}

		@Override
		public AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> getDataType() {
			return dataType;
		}

		@Override
		public String uid() {
			return uid;
		}
	}

	@Test
	public void testRegisterAndFindAndList() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcTestData locA = new AIcTestData(locType, "builtin:::jar");
		final AIcTestData locB = new AIcTestData(locType, "custom:eu.algites::jar");

		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();
		final List<AIcTestData> locRegistered = locRegistry.registerData(false, false, false, locA, locB);
		Assert.assertEquals(locRegistered.size(), 2);

		final AIcTestData locFoundA = locRegistry.getOrThrow(locType, locA.uid());
		Assert.assertSame(locFoundA, locA);

		final List<AIcTestData> locAllItems = locRegistry.items(locType);
		Assert.assertEquals(locAllItems.size(), 2);
	}

	@Test
	public void testOverwriteReadOnlyThrows() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcTestData locA1 = new AIcTestData(locType, "builtin:::jar");
		final AIcTestData locA2 = new AIcTestData(locType, "builtin:::jar");

		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();
		locRegistry.registerData(true, false, false, locA1);

		Assert.expectThrows(AIxEnumDataRegistryIllegalOperationException.class, () -> locRegistry.registerData(false, false, true, locA2));
	}

	@Test
	public void testOverwriteNonReadOnlyRespectsFlag() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcTestData locA1 = new AIcTestData(locType, "builtin:::jar");
		final AIcTestData locA2 = new AIcTestData(locType, "builtin:::jar");

		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();
		locRegistry.registerData(false, false, false, locA1);

		final List<AIcTestData> locNoOverwrite = locRegistry.registerData(false, false, false, locA2);
		Assert.assertEquals(locNoOverwrite.size(), 0);
		Assert.assertSame(locRegistry.getOrThrow(locType, locA1.uid()), locA1);

		final List<AIcTestData> locOverwrite = locRegistry.registerData(false, false, true, locA2);
		Assert.assertEquals(locOverwrite.size(), 1);
		Assert.assertSame(locRegistry.getOrThrow(locType, locA1.uid()), locA2);
	}

	@Test
	public void testReleasePermanentThrows() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcTestData locA = new AIcTestData(locType, "builtin:::jar");

		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();
		locRegistry.registerData(false, true, false, locA);

		Assert.expectThrows(AIxEnumDataRegistryIllegalOperationException.class, () -> locRegistry.releaseData(locType, locA.uid()));
	}

	@Test
	public void testReleaseRemovesItem() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcTestData locA = new AIcTestData(locType, "builtin:::jar");
		final AIcTestData locB = new AIcTestData(locType, "custom:eu.algites::jar");

		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();
		locRegistry.registerData(false, false, false, locA, locB);

		final List<AIcTestData> locReleased = locRegistry.releaseData(locType, locB.uid());
		Assert.assertEquals(locReleased.size(), 1);

		Assert.assertTrue(locRegistry.findByUid(locType, locB.uid()).isEmpty());
		Assert.assertEquals(locRegistry.items(locType).size(), 1);
	}

	@Test
	public void testGetOrThrowNotFound() {
		final AIiUidEnumDataType<AIrTestUidEnumDataRecord, AInUidEnumDataOrigin> locType = new AIcTestUidEnumDataType();
		final AIcUidEnumDataRegistry locRegistry = AIcUidEnumDataRegistry.getInstance();

		Assert.expectThrows(AIxEnumDataRegistryItemNotFoundException.class, () -> locRegistry.getOrThrow(locType, "builtin:::jar"));
	}
}
