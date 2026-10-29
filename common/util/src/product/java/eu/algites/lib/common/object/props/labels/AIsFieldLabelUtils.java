package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormatResolverFactory;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputLocaleResolverFactory;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolverFactory;
import eu.algites.lib.factory.cpdiscovery.AIsCategoryProviderDiscovery;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormatResolver;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputLocaleResolver;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolver;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinFormat;
import eu.algites.lib.common.object.rendering.AIsRenderingOutputUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: {@link AIsFieldLabelUtils}
 * </p>
 * <p>
 * Description: Utilities for the field labeling processing.
 * Supports annotations declared on:
 * </p>
 * <ul>
 *   <li>fields</li>
 *   <li>methods (getters/accessors)</li>
 *   <li>record components</li>
 *   <li>interface methods (e.g., record implements interface)</li>
 * </ul>
 * <p>
 * In the presence of multiple annotations for the same property name, the following precedence is applied:
 * </p>
 * <ol>
 *   <li>record component annotation</li>
 *   <li>field annotation</li>
 *   <li>declared class method annotation</li>
 *   <li>interface method annotation</li>
 * </ol>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 6:57
 */
public class AIsFieldLabelUtils {

	private record AIcFieldMeta(
            String defaultLabel,
            String defaultRoleCode,
			Map<AIcLabelKey, String> labelsByKey,
			AIiFieldLabelResolver labelResolverOrNull,
			AIiRenderingOutputPurposeResolver purposeResolverOrNull,
			AIiRenderingOutputFormatResolver formatResolverOrNull,
			AIiRenderingOutputLocaleResolver localeResolverOrNull
	) {
		/* Record as uiddata carrier. */
	}

	
	private record AIcLabelKey(
            String roleCode,
			String purposeCode,
			String formatCodeOrNull,
			String localeCodeOrNull
	) {
		/* Record as uiddata carrier. */
	}

private record AIcFieldMetaNode(
			Map<String, AIcFieldMeta> declaredByPropertyName,
			AIcFieldMetaNode parentOrNull
	) {
		/* Record as uiddata carrier. */
	}

	private static final ClassValue<AIcFieldMetaNode> CACHE = new ClassValue<>() {
		/** {@inheritDoc} */
		@Override
		protected AIcFieldMetaNode computeValue(Class<?> type) {
			AIcFieldMetaNode locParentOrNull = null;

			Class<?> locSuperclass = type.getSuperclass();
			if (locSuperclass != null && locSuperclass != Object.class) {
				locParentOrNull = CACHE.get(locSuperclass);
			}

			Map<String, AIcFieldMeta> locDeclaredByPropertyName = buildDeclaredMetaMap(type);

			return new AIcFieldMetaNode(Collections.unmodifiableMap(locDeclaredByPropertyName), locParentOrNull);
		}
	};

	private static Map<String, AIcFieldMeta> buildDeclaredMetaMap(Class<?> aOwnerClass) {
		Map<String, AIcFieldMeta> locDeclaredByPropertyName = new HashMap<>();

		/*
		 * Lowest precedence first: interface method annotations.
		 */
		collectFromInterfaces(aOwnerClass, locDeclaredByPropertyName);

		/*
		 * Class declared methods override interface method annotations.
		 */
		collectFromDeclaredMethods(aOwnerClass, locDeclaredByPropertyName);

		/*
		 * Fields override method-based annotations.
		 */
		collectFromDeclaredFields(aOwnerClass, locDeclaredByPropertyName);

		/*
		 * Record components override everything else.
		 */
		collectFromRecordComponents(aOwnerClass, locDeclaredByPropertyName);

		return locDeclaredByPropertyName;
	}

	private static void collectFromDeclaredFields(Class<?> aOwnerClass, Map<String, AIcFieldMeta> aDeclaredByPropertyName) {
		for (Field locField : aOwnerClass.getDeclaredFields()) {
			AIaFieldLabel locAnnotation = locField.getAnnotation(AIaFieldLabel.class);
			if (locAnnotation == null) {
				continue;
			}

			validate(locAnnotation, "Field " + locField);

			AIcFieldMeta locMeta = buildMetaFromAnnotation(locAnnotation);
			aDeclaredByPropertyName.put(locField.getName(), locMeta);
		}
	}

	private static void collectFromDeclaredMethods(Class<?> aOwnerClass, Map<String, AIcFieldMeta> aDeclaredByPropertyName) {
		for (Method locMethod : aOwnerClass.getDeclaredMethods()) {
			AIaFieldLabel locAnnotation = locMethod.getAnnotation(AIaFieldLabel.class);
			if (locAnnotation == null) {
				continue;
			}

			String locPropertyNameOrNull = resolvePropertyNameFromMethod(locMethod);
			if (locPropertyNameOrNull == null || locPropertyNameOrNull.isBlank()) {
				continue;
			}

			validate(locAnnotation, "Method " + locMethod);

			AIcFieldMeta locMeta = buildMetaFromAnnotation(locAnnotation);
			aDeclaredByPropertyName.put(locPropertyNameOrNull, locMeta);
		}
	}

	private static void collectFromRecordComponents(Class<?> aOwnerClass, Map<String, AIcFieldMeta> aDeclaredByPropertyName) {
		if (!aOwnerClass.isRecord()) {
			return;
		}

		RecordComponent[] locComponents = aOwnerClass.getRecordComponents();
		if (locComponents == null || locComponents.length == 0) {
			return;
		}

		for (RecordComponent locComponent : locComponents) {
			AIaFieldLabel locAnnotation = locComponent.getAnnotation(AIaFieldLabel.class);
			if (locAnnotation == null) {
				continue;
			}

			validate(locAnnotation, "Record component " + locComponent);

			AIcFieldMeta locMeta = buildMetaFromAnnotation(locAnnotation);
			aDeclaredByPropertyName.put(locComponent.getName(), locMeta);
		}
	}

	private static void collectFromInterfaces(Class<?> aOwnerClass, Map<String, AIcFieldMeta> aDeclaredByPropertyName) {
		Set<Class<?>> locVisitedInterfaces = new HashSet<>();
		for (Class<?> locInterface : aOwnerClass.getInterfaces()) {
			collectFromInterfaceRecursive(locInterface, locVisitedInterfaces, aDeclaredByPropertyName);
		}
	}

	private static void collectFromInterfaceRecursive(
			Class<?> aInterface,
			Set<Class<?>> aVisitedInterfaces,
			Map<String, AIcFieldMeta> aDeclaredByPropertyName
	) {
		if (aInterface == null || !aInterface.isInterface()) {
			return;
		}
		if (!aVisitedInterfaces.add(aInterface)) {
			return;
		}

		for (Method locMethod : aInterface.getMethods()) {
			AIaFieldLabel locAnnotation = locMethod.getAnnotation(AIaFieldLabel.class);
			if (locAnnotation == null) {
				continue;
			}

			String locPropertyNameOrNull = resolvePropertyNameFromMethod(locMethod);
			if (locPropertyNameOrNull == null || locPropertyNameOrNull.isBlank()) {
				continue;
			}

			validate(locAnnotation, "Interface method " + locMethod);

			AIcFieldMeta locMeta = buildMetaFromAnnotation(locAnnotation);

			/*
			 * Lowest precedence: only set if absent, because later collectors override.
			 */
			aDeclaredByPropertyName.putIfAbsent(locPropertyNameOrNull, locMeta);
		}

		for (Class<?> locParentInterface : aInterface.getInterfaces()) {
			collectFromInterfaceRecursive(locParentInterface, aVisitedInterfaces, aDeclaredByPropertyName);
		}
	}

	private static String resolvePropertyNameFromMethod(Method aMethod) {
		if (aMethod == null) {
			return null;
		}
		if (aMethod.getParameterCount() != 0) {
			return null;
		}
		if (aMethod.getReturnType() == Void.TYPE) {
			return null;
		}

		String locName = aMethod.getName();
		if (locName == null || locName.isBlank()) {
			return null;
		}

		if (locName.startsWith("get") && locName.length() > 3) {
			return decapitalize(locName.substring(3));
		}
		if (locName.startsWith("is") && locName.length() > 2) {
			Class<?> locReturnType = aMethod.getReturnType();
			if (locReturnType == boolean.class || locReturnType == Boolean.class) {
				return decapitalize(locName.substring(2));
			}
		}

		/*
		 * Record accessor or property-like method name.
		 */
		return locName;
	}

	private static String decapitalize(String aValue) {
		if (aValue == null || aValue.isBlank()) {
			return aValue;
		}
		char locFirst = aValue.charAt(0);
		char locLower = Character.toLowerCase(locFirst);
		if (locFirst == locLower) {
			return aValue;
		}
		return locLower + aValue.substring(1);
	}

	private static AIcFieldMeta buildMetaFromAnnotation(AIaFieldLabel ann) {
		Map<AIcLabelKey, String> labels = new HashMap<>();

		for (AIaFieldLabel.Entry e : ann.labels()) {
			String locPurposeCode = resolvePurposeCodeOrNull(e);
            if (locPurposeCode == null || locPurposeCode.isBlank()) locPurposeCode = AInRenderingOutputBuiltinPurpose.DEFAULT.code();

			String locFormatCodeOrNull = resolveFormatCodeOrNull(e);
			String locLocaleCodeOrNull = normalizeLocaleCodeOrNull(e.localeCode());

			labels.put(new AIcLabelKey(e.roleCode(), locPurposeCode, locFormatCodeOrNull, locLocaleCodeOrNull), e.label());
		}
		AIiFieldLabelResolver labelResolver = instantiateLabelResolverIfAny(ann);
		AIiRenderingOutputPurposeResolver purposeResolver = instantiatePurposeResolverIfAny(ann);
		AIiRenderingOutputFormatResolver formatResolver = instantiateFormatResolverIfAny(ann);
		AIiRenderingOutputLocaleResolver localeResolver = instantiateLocaleResolverIfAny(ann);

		return new AIcFieldMeta(
                ann.label(), ann.roleCode(),
				Collections.unmodifiableMap(labels),
				labelResolver,
				purposeResolver,
				formatResolver,
				localeResolver
		);
	}

	private static String resolvePurposeCodeOrNull(AIaFieldLabel.Entry aEntry) {
		if (aEntry == null) {
			return null;
		}
		if (aEntry.purpose() != AInRenderingOutputBuiltinPurpose.DEFAULT) {
			return aEntry.purpose().code();
		}
		if (aEntry.purposeCode() != null && !aEntry.purposeCode().isBlank()) {
			return aEntry.purposeCode();
		}
		return null;
	}

	private static String resolveFormatCodeOrNull(AIaFieldLabel.Entry aEntry) {
		if (aEntry == null) {
			return null;
		}
		if (aEntry.format() != AInRenderingOutputBuiltinFormat.DEFAULT) {
			return aEntry.format().code();
		}
		if (aEntry.formatCode() != null && !aEntry.formatCode().isBlank()) {
			return aEntry.formatCode();
		}
		return null;
	}

	private static String normalizeLocaleCodeOrNull(String aLocaleCode) {
		if (aLocaleCode == null || aLocaleCode.isBlank()) {
			return null;
		}
		return aLocaleCode.trim();
	}


    private static AIiFieldLabelResolver instantiateLabelResolverIfAny(AIaFieldLabel aAnnotation) {
        return resolver(aAnnotation.labelResolver(), AIiFieldLabelResolverFactory.class);
    }

    private static AIiRenderingOutputPurposeResolver instantiatePurposeResolverIfAny(AIaFieldLabel aAnnotation) {
        return resolver(aAnnotation.purposeResolver(), AIiRenderingOutputPurposeResolverFactory.class);
    }

    private static AIiRenderingOutputFormatResolver instantiateFormatResolverIfAny(AIaFieldLabel aAnnotation) {
        return resolver(aAnnotation.formatResolver(), AIiRenderingOutputFormatResolverFactory.class);
    }

    private static AIiRenderingOutputLocaleResolver instantiateLocaleResolverIfAny(AIaFieldLabel aAnnotation) {
        return resolver(aAnnotation.localeResolver(), AIiRenderingOutputLocaleResolverFactory.class);
    }

    private static <T> T resolver(AIaCategoryProviderFactoryDefinition aSelection,
            Class<? extends eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory<T, eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext>> aFactory) {
        if (aSelection.factoryCategory().isBlank() && aSelection.factoryProviderId().isBlank())
            return null;
        return AIsCategoryProviderDiscovery.getInstance(aSelection, aFactory, "default", null);
    }

	private static void validate(AIaFieldLabel aFieldLabelAnnotation, String aElementDescription) {
		boolean locHasLabels = aFieldLabelAnnotation.labels().length > 0;
		boolean locHasLabelResolver = !(aFieldLabelAnnotation.labelResolver().factoryProviderId().isBlank()
                && aFieldLabelAnnotation.labelResolver().factoryCategory().isBlank());

		int locLabelChoices = (locHasLabels ? 1 : 0) + (locHasLabelResolver ? 1 : 0);
		if (locLabelChoices > 1) {
			throw new IllegalArgumentException(
					aElementDescription + ": choose only one of labels/labelResolver"
			);
		}

		boolean locHasModeResolver = !(aFieldLabelAnnotation.purposeResolver().factoryProviderId().isBlank()
                && aFieldLabelAnnotation.purposeResolver().factoryCategory().isBlank());
		if (locHasModeResolver && locHasLabels) {
			/*
			 * This is allowed: purposeResolver selects active purpose, labels map purpose->label.
			 */
		}
	}

	private static AIcFieldMeta findMeta(Class<?> aOwnerClass, String aPropertyName) {
		AIcFieldMetaNode locNode = CACHE.get(aOwnerClass);
		while (locNode != null) {
			AIcFieldMeta locMeta = locNode.declaredByPropertyName.get(aPropertyName);
			if (locMeta != null) {
				return locMeta;
			}
			locNode = locNode.parentOrNull;
		}
		return null;
	}

	/**
	 * Example API for callers (toString code) to get a label for a field name.
	 * @param aOwnerClass class where the call is made
	 * @param aPropertyName property name used (field/record component/getter)
	 * @return label for the property name
	 */
	public static String findLabel(Class<?> aOwnerClass, String aPropertyName) {
		return findLabel(aOwnerClass, aPropertyName, null, null, null);
	}

	/**
	 * Example API for callers (toString code) to get a label for a field name.
	 *
	 * @param aOwnerClass class where the call is made
	 * @param aPropertyName property name used (field/record component/getter)
	 * @param aRenderingOutputPurpose to be used for label resolution
	 * @param aRenderingOutputFormat to be used for label resolution
	 * @param aRenderingOutputLocale to be used for label resolution
	 * @return label for the property name
	 */
	public static String findLabel(Class<?> aOwnerClass, String aPropertyName, AIiRenderingOutputPurpose aRenderingOutputPurpose,
			final AIiRenderingOutputFormat aRenderingOutputFormat, final Locale aRenderingOutputLocale) {
        return findLabel(aOwnerClass, aPropertyName, aRenderingOutputPurpose, aRenderingOutputFormat,
                aRenderingOutputLocale, AIaFieldLabel.DEFAULT_ROLE_CODE);
    }

    /** Resolve a given semantic text role while preserving independent rendering axes. */
    public static String findLabel(Class<?> aOwnerClass, String aPropertyName, AIiRenderingOutputPurpose aRenderingOutputPurpose,
            AIiRenderingOutputFormat aRenderingOutputFormat, Locale aRenderingOutputLocale, String aRoleCode) {
		AIcFieldMeta locMeta = findMeta(aOwnerClass, aPropertyName);
        boolean locDefaultRole = AIaFieldLabel.DEFAULT_ROLE_CODE.equals(aRoleCode);
		if (locMeta == null) return locDefaultRole ? aPropertyName : null;

		AIiRenderingOutputPurpose locResolvedPurpose = aRenderingOutputPurpose;
		if (locResolvedPurpose == null && locMeta.purposeResolverOrNull != null) {
			locResolvedPurpose = locMeta.purposeResolverOrNull.resolvePurpose();
		}
		if (locResolvedPurpose == null) {
			locResolvedPurpose = AIsRenderingOutputUtils.usedRenderingOutputPurpose();
		}

		AIiRenderingOutputFormat locResolvedFormat = aRenderingOutputFormat;
		if (locResolvedFormat == null && locMeta.formatResolverOrNull != null) {
			locResolvedFormat = locMeta.formatResolverOrNull.resolveFormat();
		}
		if (locResolvedFormat == null) {
			locResolvedFormat = AIsRenderingOutputUtils.usedRenderingOutputFormat();
		}

		Locale locResolvedLocale = aRenderingOutputLocale;
		if (locResolvedLocale == null && locMeta.localeResolverOrNull != null) {
			locResolvedLocale = locMeta.localeResolverOrNull.resolveLocale();
		}
		if (locResolvedLocale == null) {
			locResolvedLocale = AIsRenderingOutputUtils.usedRenderingOutputLocale();
		}

        if (locMeta.labelResolverOrNull != null) {
            String locResolved = locMeta.labelResolverOrNull.resolveLabel(aOwnerClass, aPropertyName,
                    aRoleCode, locResolvedPurpose, locResolvedFormat, locResolvedLocale);
			if (locResolved != null && !locResolved.isBlank()) {
				return locResolved;
			}
		}

		String locMapped = resolveMappedLabelOrNull(locMeta.labelsByKey, aRoleCode, locResolvedPurpose, locResolvedFormat, locResolvedLocale);
        if (locMapped != null && !locMapped.isBlank()) return locMapped;
        if (!locMeta.defaultLabel.isBlank() && aRoleCode.equals(locMeta.defaultRoleCode)) return locMeta.defaultLabel;
        return locDefaultRole ? aPropertyName : null;
	}






	/**
	 * Resolves a mapped label based on the resolved purpose/format/locale.
	 * <p>
	 * The matching is performed in the following order:
	 * </p>
	 * <ol>
	 *   <li>purpose + format + locale</li>
	 *   <li>purpose + format</li>
	 *   <li>purpose + locale</li>
	 *   <li>purpose</li>
	 * </ol>
	 *
	 * @param aLabelsByKey labels mapped by the label key
	 * @param aResolvedPurpose resolved rendering output purpose
	 * @param aResolvedFormat resolved rendering output format
	 * @param aResolvedLocale resolved rendering output locale
	 * @return mapped label or null
	 */
	private static String resolveMappedLabelOrNull(
			Map<AIcLabelKey, String> aLabelsByKey,
            String aRoleCode,
			AIiRenderingOutputPurpose aResolvedPurpose,
			AIiRenderingOutputFormat aResolvedFormat,
			Locale aResolvedLocale
	) {
		if (aLabelsByKey == null || aLabelsByKey.isEmpty()) {
			return null;
		}

		String locPurposeCode = (aResolvedPurpose != null) ? normalizeKeyPartOrNull(aResolvedPurpose.code()) : null;
		if (locPurposeCode == null) {
			return null;
		}

		String locFormatCodeOrNull = (aResolvedFormat != null) ? normalizeKeyPartOrNull(aResolvedFormat.code()) : null;
		Set<String> locLocaleCandidateCodes = buildLocaleCandidateCodes(aResolvedLocale);

		if (locFormatCodeOrNull != null && !locLocaleCandidateCodes.isEmpty()) {
			for (String locLocaleCode : locLocaleCandidateCodes) {
				String locFound = aLabelsByKey.get(new AIcLabelKey(aRoleCode, locPurposeCode, locFormatCodeOrNull, locLocaleCode));
				if (locFound != null) {
					return locFound;
				}
			}
		}

		if (locFormatCodeOrNull != null) {
			String locFound = aLabelsByKey.get(new AIcLabelKey(aRoleCode, locPurposeCode, locFormatCodeOrNull, null));
			if (locFound != null) {
				return locFound;
			}
		}

		if (!locLocaleCandidateCodes.isEmpty()) {
			for (String locLocaleCode : locLocaleCandidateCodes) {
				String locFound = aLabelsByKey.get(new AIcLabelKey(aRoleCode, locPurposeCode, null, locLocaleCode));
				if (locFound != null) {
					return locFound;
				}
			}
		}

        String locFound = aLabelsByKey.get(new AIcLabelKey(aRoleCode, locPurposeCode, null, null));
        if (locFound != null) return locFound;
        if (!AInRenderingOutputBuiltinPurpose.DEFAULT.code().equals(locPurposeCode))
            return resolveMappedLabelOrNull(aLabelsByKey, aRoleCode, AInRenderingOutputBuiltinPurpose.DEFAULT,
                    aResolvedFormat, aResolvedLocale);
        return null;
	}

	private static String normalizeKeyPartOrNull(String aCode) {
		if (aCode == null) {
			return null;
		}
		String locTrimmed = aCode.trim();
		return locTrimmed.isBlank() ? null : locTrimmed;
	}

	private static Set<String> buildLocaleCandidateCodes(Locale aLocale) {
		if (aLocale == null) {
			return Collections.emptySet();
		}

		Set<String> locCandidates = new java.util.LinkedHashSet<>();

		String locLanguageTag = normalizeKeyPartOrNull(aLocale.toLanguageTag());
		if (locLanguageTag != null && !"und".equalsIgnoreCase(locLanguageTag)) {
			addLocaleVariants(locCandidates, locLanguageTag);
		}

		String locToString = normalizeKeyPartOrNull(aLocale.toString());
		if (locToString != null) {
			addLocaleVariants(locCandidates, locToString);
		}

		String locLanguage = normalizeKeyPartOrNull(aLocale.getLanguage());
		if (locLanguage != null) {
			addLocaleVariants(locCandidates, locLanguage);
		}

		return locCandidates;
	}

	private static void addLocaleVariants(Set<String> aTarget, String aLocaleCode) {
		aTarget.add(aLocaleCode);

		String locDashToUnderscore = aLocaleCode.replace('-', '_');
		aTarget.add(locDashToUnderscore);

		String locUnderscoreToDash = aLocaleCode.replace('_', '-');
		aTarget.add(locUnderscoreToDash);
	}

	/**
	 * Validates that the given property name exists on the owner type.
	 * <p>
	 * The property is considered present if at least one of the following is found:
	 * </p>
	 * <ul>
	 *   <li>a field with the same name (searched on the class hierarchy)</li>
	 *   <li>a record component with the same name</li>
	 *   <li>a no-arg, non-void method that maps to the property name (record accessor or JavaBean getter), including interface methods</li>
	 * </ul>
	 *
	 * @param aOwnerClass the owning type
	 * @param aPropertyName property name (field/record component/accessor/getter derived name)
	 * @throws IllegalArgumentException when the property name is blank or not present on the owner type
	 */
	public static void requirePropertyExists(Class<?> aOwnerClass, String aPropertyName) {
	    if (aOwnerClass == null) {
	        throw new IllegalArgumentException("Owner class must not be null");
	    }
	    if (aPropertyName == null || aPropertyName.isBlank()) {
	        throw new IllegalArgumentException("Property name must not be blank");
	    }
	    if (!isPropertyPresent(aOwnerClass, aPropertyName)) {
	        throw new IllegalArgumentException("Unknown property '" + aPropertyName + "' for " + aOwnerClass.getName());
	    }
	}

	/**
	 * Checks whether the given property name exists on the owner type.
	 *
	 * @param aOwnerClass the owning type
	 * @param aPropertyName property name (field/record component/accessor/getter derived name)
	 * @return true if the property exists on the owner type; false otherwise
	 */
	public static boolean isPropertyPresent(Class<?> aOwnerClass, String aPropertyName) {
	    if (aOwnerClass == null || aPropertyName == null || aPropertyName.isBlank()) {
	        return false;
	    }

	    if (containsRecordComponentName(aOwnerClass, aPropertyName)) {
	        return true;
	    }
	    if (containsFieldNameInHierarchy(aOwnerClass, aPropertyName)) {
	        return true;
	    }
	    return containsPropertyMethodName(aOwnerClass, aPropertyName);
	}

	private static boolean containsRecordComponentName(Class<?> aOwnerClass, String aPropertyName) {
	    if (!aOwnerClass.isRecord()) {
	        return false;
	    }

	    RecordComponent[] locComponents = aOwnerClass.getRecordComponents();
	    if (locComponents == null || locComponents.length == 0) {
	        return false;
	    }

	    for (RecordComponent locComponent : locComponents) {
	        if (aPropertyName.equals(locComponent.getName())) {
	            return true;
	        }
	    }
	    return false;
	}

	private static boolean containsFieldNameInHierarchy(Class<?> aOwnerClass, String aPropertyName) {
	    Class<?> locType = aOwnerClass;
	    while (locType != null && locType != Object.class) {
	        try {
	            Field locField = locType.getDeclaredField(aPropertyName);
	            if (locField != null) {
	                return true;
	            }
	        } catch (NoSuchFieldException locException) {
	            /* Ignore. */
	        }
	        locType = locType.getSuperclass();
	    }
	    return false;
	}

	private static boolean containsPropertyMethodName(Class<?> aOwnerClass, String aPropertyName) {
	    for (Method locMethod : aOwnerClass.getMethods()) {
	        if (locMethod.getDeclaringClass() == Object.class) {
	            continue;
	        }
	        String locResolvedPropertyNameOrNull = resolvePropertyNameFromMethod(locMethod);
	        if (aPropertyName.equals(locResolvedPropertyNameOrNull)) {
	            return true;
	        }
	    }
	    return false;
	}
}
