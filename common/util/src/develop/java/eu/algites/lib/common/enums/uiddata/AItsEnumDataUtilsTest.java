package eu.algites.lib.common.enums.uiddata;

import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.LAST_UID_HEADER_PART_POSITION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AIsUidEnumDataUtils}.
 *
 * <p>
 * The tests use a small test enum-uiddata-type with the following UID layout:
 * {@code <origin>:<namespace>:<classifier>:<fileType>}.
 * </p>
 * @author linhart1
 */
public class AItsEnumDataUtilsTest {

	private static final AIiUidEnumDataType<AIcTestUidEnumDataRecord, AInUidEnumDataOrigin> TEST_ENUM_DATA_TYPE = new AIcTestEnumDataType();

	/**
	 * Test record used as a parsed representation for the test UID format.
	 *
	 * @param uid UID string
	 * @param origin origin (builtin/custom)
	 * @param namespace namespace part (empty for builtin)
	 * @param classifier optional classifier
	 * @param fileType required file type
	 */
	public record AIcTestUidEnumDataRecord(
			String uid,
			AInUidEnumDataOrigin origin,
			String namespace,
			String classifier,
			String fileType
	) implements AIiUidEnumDataRecord { }

	/**
	 * Test enum-uiddata-type used by tests.
	 */
	private static final class AIcTestEnumDataType implements AIiUidEnumDataType<AIcTestUidEnumDataRecord, AInUidEnumDataOrigin> {

		@SuppressWarnings("unchecked")
		private static final List<AIiUidPartMetadata<AInUidEnumDataOrigin>> SPECIFIC_UID_PARTS_METADATA = List.of(
				new AIrUidPartMetadata(() -> "classifier", Collections.emptyMap()),
				new AIrUidPartMetadata(() -> "fileType", Map.of(AInUidEnumDataOrigin.BUILTIN, true, AInUidEnumDataOrigin.CUSTOM, true))
		);

		@Override
		public BiFunction<String, List<String>, ? extends AIcTestUidEnumDataRecord> getUidRecordFactory() {
			return (BiFunction<String, List<String>, AIcTestUidEnumDataRecord>) (aUid, aParts)
					-> new AIcTestUidEnumDataRecord(
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

		@Override
		public int getUidPartCount() {
			return getSpecificUidPartsMetadata().size() + LAST_UID_HEADER_PART_POSITION + 1;
		}
	}

	/**
	 * Test that a UID can be created from a specific UID parts list and that the UID can be parsed back to the same parts.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCreateBuiltinUidAndParseRoundtrip() {
		final List<String> locSpecificUidParts = List.of("", "jar");
		final List<AIiUidPartMetadata<AInUidEnumDataOrigin>> locSpecificUidPartsMetadata = List.of(
				new AIrUidPartMetadata(() -> "classifier", Collections.emptyMap()),
				new AIrUidPartMetadata(() -> "fileType", Map.of(AInUidEnumDataOrigin.BUILTIN, true, AInUidEnumDataOrigin.CUSTOM, true))
		);
		final String locUid = AIsUidEnumDataUtils.createBuiltinUid(locSpecificUidParts, locSpecificUidPartsMetadata);
		Assert.assertEquals(locUid, "builtin:::jar");

		final AIcTestUidEnumDataRecord locParts = AIsUidEnumDataUtils.parseUid(TEST_ENUM_DATA_TYPE, locUid);
		Assert.assertEquals(locParts.origin(), AInUidEnumDataOrigin.BUILTIN);
		Assert.assertEquals(locParts.namespace(), "");
		Assert.assertEquals(locParts.classifier(), "");
		Assert.assertEquals(locParts.fileType(), "jar");
	}

	/**
	 * Test that the origin and namespace can be extracted from a UID.
	 */
	@Test
	public void testGetOriginAndNamespace() {
		final String locUid = "builtin:::jar";
		final AIiUidEnumDataOrigin locOrigin = AIsUidEnumDataUtils.getOrigin(TEST_ENUM_DATA_TYPE, locUid);
		final String locNamespace = AIsUidEnumDataUtils.getNamespace(TEST_ENUM_DATA_TYPE, locUid);
		Assert.assertEquals(locOrigin, AInUidEnumDataOrigin.BUILTIN);
		Assert.assertEquals(locNamespace, "");
	}

	/**
	 * Test that the namespace must be empty for builtin UIDs.
	 */
	@Test
	public void testBuiltinNamespaceMustBeEmpty() {
		final String locInvalidUid = "builtin:eu.algites::jar";
		Assert.assertFalse(AIsUidEnumDataUtils.isValidOutputTypeUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
		Assert.expectThrows(IllegalArgumentException.class, () -> AIsUidEnumDataUtils.validateUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
	}

	/**
	 * Test that the namespace must be non-blank for custom UIDs.
	 */
	@Test
	public void testCustomNamespaceMustBeNonBlank() {
		final String locValidUid = "custom:eu.algites::jar";
		Assert.assertTrue(AIsUidEnumDataUtils.isValidOutputTypeUid(TEST_ENUM_DATA_TYPE, locValidUid));
		final AIcTestUidEnumDataRecord locParts = AIsUidEnumDataUtils.parseUid(TEST_ENUM_DATA_TYPE, locValidUid);
		Assert.assertEquals(locParts.origin(), AInUidEnumDataOrigin.CUSTOM);
		Assert.assertEquals(locParts.namespace(), "eu.algites");

		final String locInvalidUid = "custom:::jar";
		Assert.assertFalse(AIsUidEnumDataUtils.isValidOutputTypeUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
		Assert.expectThrows(IllegalArgumentException.class, () -> AIsUidEnumDataUtils.validateUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
	}

	/**
	 * Test that invalid characters are rejected.
	 */
	@Test
	public void testInvalidCharactersAreRejected() {
		final String locInvalidUid = "builtin:::ja$r";
		Assert.assertFalse(AIsUidEnumDataUtils.isValidOutputTypeUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
		Assert.expectThrows(IllegalArgumentException.class, () -> AIsUidEnumDataUtils.validateUid(TEST_ENUM_DATA_TYPE, locInvalidUid));
	}

	/**
	 * Test that the UID must have the correct number of segments.
	 */
	@Test
	public void testSplitUidIntoPartsRejectsWrongSegmentCount() {
		final String locInvalidUid = "builtin::jar";
		Assert.expectThrows(IllegalArgumentException.class, () -> AIsUidEnumDataUtils.splitUidIntoParts(TEST_ENUM_DATA_TYPE, locInvalidUid));
	}
}
