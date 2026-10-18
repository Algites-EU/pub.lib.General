package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinFormat;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolverFactory;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;

import java.util.Locale;
import java.util.ServiceLoader;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Title: {@link AItsFieldLabelUtilsTest}
 * </p>
 * <p>
 * Description: TestNG tests for {@link AIsFieldLabelUtils}
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 */
public class AItsFieldLabelUtilsTest {

	@Test
	public void testFindLabelUsesMappedLabelsForExplicitPurpose() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestMappedLabels.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "MyUserPropertyLabel", "Mapped label for USER must be returned");
	}

	@Test
	public void testFindLabelFallsBackToFieldNameIfNoAnnotation() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestNoAnnotation.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "myProperty", "Without annotation the field name must be returned");
	}

    /**
     * Verifies that both test-only factories are registered as Java services. The
     * integration tests below additionally exercise their resolution through CPDiscovery.
     */
    @Test
    public void testResolverFactoriesAvailableThroughServiceLoader() {
        boolean locLabelFactoryFound = false;
        boolean locPurposeFactoryFound = false;
        for (AIiCategoryProviderFactory<?, ?> locFactory : ServiceLoader.load(AIiCategoryProviderFactory.class)) {
            if (locFactory instanceof AIcTestFieldLabelResolverFactory) {
                locLabelFactoryFound = true;
            }
            if (locFactory instanceof AIcTestRenderingOutputPurposeResolverFactory) {
                locPurposeFactoryFound = true;
            }
        }
        Assert.assertTrue(locLabelFactoryFound, "Test field-label factory must be discoverable by ServiceLoader");
        Assert.assertTrue(locPurposeFactoryFound, "Test purpose factory must be discoverable by ServiceLoader");
    }

	@Test
	public void testFindLabelUsesLabelResolverIfPresent() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestLabelResolver.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "ResolvedLabel", "Label resolver must override mapping");
	}

	@Test
	public void testFindLabelUsesModeResolverWhenExplicitPurposeIsNull() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestModeResolverWithMapping.class,
				"myProperty",
				null,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "MyUserPropertyLabel", "Mode resolver should drive the mapping");
	}

	@Test
	public void testFindLabelFindsInheritedFieldLabelInParent() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcChildWithoutFields.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "ParentUserLabel", "Inherited field label should be resolved from parent");
	}

	@Test
	public void testFindLabelPrefersChildShadowedFieldOverParentField() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcChildWithShadowedField.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "ChildUserLabel", "Shadowed field in child should win over parent");
	}

	@Test
	public void testInvalidAnnotationCombinationThrowsException() {
		boolean locThrown = false;
		try {
			AIsFieldLabelUtils.findLabel(
					AIcTestInvalidAnnotation.class,
					"myProperty",
					AInRenderingOutputBuiltinPurpose.USER,
					AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());
		} catch (IllegalArgumentException locException) {
			locThrown = true;
		}

		Assert.assertTrue(locThrown, "Invalid annotation configuration must throw IllegalArgumentException");
	}

	private static final class AIcTestNoAnnotation {
		private String myProperty;
	}

	private static final class AIcTestMappedLabels {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "MyUserPropertyLabel"),
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.SYSTEM, label = "MySystemPropertyLabel")
		})
		private String myProperty;
	}

	private static final class AIcTestLabelResolver {
		@AIaFieldLabel(labelResolver = @AIaCategoryProviderFactoryDefinition(
                        factoryClass = AIiFieldLabelResolverFactory.class,
                        factoryCategory = "test-field-label",
                        factoryProviderId = "algites-field-label-test"))
		private String myProperty;
	}

	private static final class AIcTestModeResolverWithMapping {
		@AIaFieldLabel(
				purposeResolver = @AIaCategoryProviderFactoryDefinition(
                        factoryClass = AIiRenderingOutputPurposeResolverFactory.class,
                        factoryCategory = "test-purpose",
                        factoryProviderId = "algites-purpose-test"),
				labels = {
						@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "MyUserPropertyLabel"),
						@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.SYSTEM, label = "MySystemPropertyLabel")
				}
		)
		private String myProperty;
	}

	private static class AIcParentWithField {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "ParentUserLabel")
		})
		private String myProperty;
	}

	private static final class AIcChildWithoutFields extends AIcParentWithField {
	}

	private static class AIcParentWithShadowedField {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "ParentUserLabel")
		})
		private String myProperty;
	}

	private static final class AIcChildWithShadowedField extends AIcParentWithShadowedField {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "ChildUserLabel")
		})
		private String myProperty;
	}

	private static final class AIcTestInvalidAnnotation {
		@AIaFieldLabel(
				labelResolver = @AIaCategoryProviderFactoryDefinition(
                        factoryClass = AIiFieldLabelResolverFactory.class,
                        factoryCategory = "test-field-label",
                        factoryProviderId = "algites-field-label-test"),
				labels = {
						@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "MyUserPropertyLabel")
				}
		)
		private String myProperty;
	}



	@Test
	public void testFindLabelUsesMethodAnnotationOnGetter() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestMethodAnnotation.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "GetterUserLabel", "Getter annotation must be used when field has no annotation");
	}

	@Test
	public void testFindLabelUsesRecordComponentAnnotation() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestRecordComponent.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "RecordComponentUserLabel", "Record component annotation must be used for records");
	}

	@Test
	public void testFindLabelResolvesAnnotationFromInterfaceMethodForRecord() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestRecordImplementsInterface.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "InterfaceUserLabel", "Interface method annotation must be resolved for record implementing the interface");
	}


@Test
public void testFindLabelResolvesInterfaceAnnotationEvenIfClassDeclaresUnannotatedGetter() {
	String locLabel = AIsFieldLabelUtils.findLabel(
			AIcTestClassImplementsInterfaceWithUnannotatedGetter.class,
			"myProperty",
			AInRenderingOutputBuiltinPurpose.USER,
			AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

	Assert.assertEquals(locLabel, "InterfaceGetterUserLabel", "Interface getter annotation must be resolved even if class declares an unannotated getter");
}

	@Test
	public void testFindLabelFieldAnnotationOverridesGetterAnnotation() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestFieldOverridesMethod.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "FieldUserLabel", "Field annotation must override getter annotation");
	}

	@Test
	public void testFindLabelRecordComponentAnnotationOverridesAccessorMethodAnnotation() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestRecordComponentOverridesMethod.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT, Locale.getDefault());

		Assert.assertEquals(locLabel, "RecordComponentUserLabel", "Record component annotation must override accessor method annotation");
	}

	private static final class AIcTestMethodAnnotation {
		private String myProperty;

		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "GetterUserLabel")
		})
		public String getMyProperty() {
			return myProperty;
		}
	}

	private record AIcTestRecordComponent(
			@AIaFieldLabel(labels = {
					@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "RecordComponentUserLabel")
			})
			String myProperty
	) {
		/* Record as uiddata carrier. */
	}

	private interface AIiTestInterfaceWithAnnotatedAccessor {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "InterfaceUserLabel")
		})
		String myProperty();
	}

	private record AIcTestRecordImplementsInterface(
			String myProperty
	) implements AIiTestInterfaceWithAnnotatedAccessor {
		/* Record as uiddata carrier. */
	}


private interface AIiTestInterfaceWithAnnotatedGetter {
	@AIaFieldLabel(labels = {
			@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "InterfaceGetterUserLabel")
	})
	String getMyProperty();
}

private static final class AIcTestClassImplementsInterfaceWithUnannotatedGetter implements AIiTestInterfaceWithAnnotatedGetter {
	@Override
	public String getMyProperty() {
		return "value";
	}
}

	private static final class AIcTestFieldOverridesMethod {

		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "FieldUserLabel")
		})
		private String myProperty;

		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "GetterUserLabel")
		})
		public String getMyProperty() {
			return myProperty;
		}
	}

	private record AIcTestRecordComponentOverridesMethod(
			@AIaFieldLabel(labels = {
					@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "RecordComponentUserLabel")
			})
			String myProperty
	) {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(purpose = AInRenderingOutputBuiltinPurpose.USER, label = "GetterUserLabel")
		})
		@Override
		public String myProperty() {
			return myProperty;
		}
	}



	@Test
	public void testRequirePropertyExistsDoesNotThrowForExistingPropertyOnField() {
	    AIsFieldLabelUtils.requirePropertyExists(
	            AIcTestMappedLabels.class,
	            "myProperty"
	    );
	}

	@Test
	public void testRequirePropertyExistsDoesNotThrowForExistingPropertyOnGetterOnly() {
	    AIsFieldLabelUtils.requirePropertyExists(
	            AIcTestGetterOnlyProperty.class,
	            "myProperty"
	    );
	}

	@Test
	public void testRequirePropertyExistsThrowsForUnknownProperty() {
	    boolean locThrown = false;
	    try {
	        AIsFieldLabelUtils.requirePropertyExists(
	                AIcTestMappedLabels.class,
	                "unknownProperty"
	        );
	    } catch (IllegalArgumentException locException) {
	        locThrown = true;
	    }
	    Assert.assertTrue(locThrown, "Unknown property must throw IllegalArgumentException");
	}

	

	@Test
	public void testFindLabelUsesMappedLabelsForExplicitFormatEnum() {
		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestMappedLabelsByFormat.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.JSON,
				Locale.getDefault());

		Assert.assertEquals(locLabel, "MyUserJsonLabel", "Mapped label for USER+JSON must be returned");
	}

	@Test
	public void testFindLabelUsesMappedLabelsForExplicitFormatCode() {
		AIiRenderingOutputFormat locCustomFormat = new AIcCustomRenderingOutputFormat("CUSTOM_FMT");

		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestMappedLabelsByFormatCode.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				locCustomFormat,
				Locale.getDefault());

		Assert.assertEquals(locLabel, "MyUserCustomFormatLabel", "Mapped label for USER+CUSTOM_FMT must be returned");
	}

	@Test
	public void testFindLabelUsesMappedLabelsForExplicitLocaleCode() {
		Locale locLocale = new Locale("cs", "CZ");

		String locLabel = AIsFieldLabelUtils.findLabel(
				AIcTestMappedLabelsByLocale.class,
				"myProperty",
				AInRenderingOutputBuiltinPurpose.USER,
				AInRenderingOutputBuiltinFormat.PLAIN_TEXT,
				locLocale);

		Assert.assertEquals(locLabel, "MyUserCzechLabel", "Mapped label for USER+cs_CZ must be returned");
	}

	private static final class AIcTestMappedLabelsByFormat {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(
					purpose = AInRenderingOutputBuiltinPurpose.USER,
					format = AInRenderingOutputBuiltinFormat.PLAIN_TEXT,
					label = "MyUserPlainTextLabel"
				),
				@AIaFieldLabel.Entry(
					purpose = AInRenderingOutputBuiltinPurpose.USER,
					format = AInRenderingOutputBuiltinFormat.JSON,
					label = "MyUserJsonLabel"
				)
		})
		private String myProperty;
	}

	private static final class AIcCustomRenderingOutputFormat implements AIiRenderingOutputFormat {

		private final String customFormatCode;

		private AIcCustomRenderingOutputFormat(String aCustomFormatCode) {
			customFormatCode = aCustomFormatCode;
		}

		@Override
		public String code() {
			return customFormatCode;
		}
	}

	private static final class AIcTestMappedLabelsByFormatCode {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(
					purpose = AInRenderingOutputBuiltinPurpose.USER,
					formatCode = "CUSTOM_FMT",
					label = "MyUserCustomFormatLabel"
				)
		})
		private String myProperty;
	}

	private static final class AIcTestMappedLabelsByLocale {
		@AIaFieldLabel(labels = {
				@AIaFieldLabel.Entry(
					purpose = AInRenderingOutputBuiltinPurpose.USER,
					localeCode = "cs_CZ",
					label = "MyUserCzechLabel"
				),
				@AIaFieldLabel.Entry(
					purpose = AInRenderingOutputBuiltinPurpose.USER,
					localeCode = "en_US",
					label = "MyUserEnglishLabel"
				)
		})
		private String myProperty;
	}


private static final class AIcTestGetterOnlyProperty {
	    public String getMyProperty() {
	        return "value";
	    }
	}
}
