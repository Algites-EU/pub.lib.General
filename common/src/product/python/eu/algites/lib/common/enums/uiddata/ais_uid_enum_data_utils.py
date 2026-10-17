from __future__ import annotations
import re
from eu.algites.lib.common.enums.uiddata.ain_uid_enum_data_origin import AInUidEnumDataOrigin
from eu.algites.lib.common.exception.aix_development_error_exception import AIxDevelopmentErrorException

class AIsUidEnumDataUtils:
    UID_PART_SEPARATOR = ':'
    UID_COMMON_PART_PATTERN = re.compile(r'^[A-Za-z0-9._-]*$')
    UID_CUSTOM_NAMESPACE_PART_PATTERN = re.compile(r'^[A-Za-z0-9._-]+$')
    ORIGIN_UID_POSITION = 0
    NAMESPACE_UID_POSITION = 1
    LAST_UID_HEADER_PART_POSITION = 1
    FIRST_UID_SPECIFIC_PART_POSITION = 2

    @staticmethod
    def _label(meta): return meta.displayLabelSupplier()()
    @staticmethod
    def _required(value, label):
        if value is None: raise ValueError(f'{label} must not be null')
        normalized = value.strip()
        if not normalized: raise ValueError(f'{label} must be non-empty')
        return normalized
    @staticmethod
    def _safe_part(segment, label):
        if AIsUidEnumDataUtils.UID_COMMON_PART_PATTERN.fullmatch(segment) is None:
            raise ValueError(f"Invalid {label} part '{segment}'")
    @staticmethod
    def _safe_namespace(segment):
        if AIsUidEnumDataUtils.UID_CUSTOM_NAMESPACE_PART_PATTERN.fullmatch(segment) is None:
            raise ValueError(f"Invalid namespace '{segment}'")

    @staticmethod
    def createUid(aOrigin, aUidNamespacePart, aSpecificUidParts, aSpecificUidPartsMetadata) -> str:
        if len(aSpecificUidParts) != len(aSpecificUidPartsMetadata):
            raise AIxDevelopmentErrorException('Number of specific parts must match number of metadata')
        if aOrigin.namespaceUsed():
            namespace = AIsUidEnumDataUtils._required(aUidNamespacePart, 'namespace')
            AIsUidEnumDataUtils._safe_namespace(namespace)
        else:
            if aUidNamespacePart is not None and aUidNamespacePart.strip():
                raise AIxDevelopmentErrorException('Namespace is not allowed for this origin')
            namespace = ''
        for part, meta in zip(aSpecificUidParts, aSpecificUidPartsMetadata):
            label = AIsUidEnumDataUtils._label(meta)
            normalized = (AIsUidEnumDataUtils._required(part, label) if meta.requiredForOrigin().get(aOrigin, False)
                          else (part or '').strip())
            AIsUidEnumDataUtils._safe_part(normalized, label)
        # Matches Java: checks normalized copies but serializes the passed raw specific parts.
        if any(part is None for part in aSpecificUidParts):
            raise ValueError('A UID part cannot be null')
        return ':'.join([aOrigin.code(), namespace, *aSpecificUidParts])

    @staticmethod
    def createBuiltinUid(aSpecificUidParts, aSpecificUidPartsMetadata) -> str:
        return AIsUidEnumDataUtils.createUid(AInUidEnumDataOrigin.BUILTIN,'',aSpecificUidParts,aSpecificUidPartsMetadata)

    @staticmethod
    def createCustomUid(aUidNamespacePart,aSpecificUidParts,aSpecificUidPartsMetadata) -> str:
        # Matches the current Java implementation, which uses BUILTIN even here.
        return AIsUidEnumDataUtils.createUid(AInUidEnumDataOrigin.BUILTIN,aUidNamespacePart,aSpecificUidParts,aSpecificUidPartsMetadata)

    @staticmethod
    def splitUidIntoParts(aEnumDataType, aUid) -> list[str]:
        if aEnumDataType is None or aUid is None: raise ValueError('UID and data type must not be null')
        if ':' not in aUid: raise ValueError(f"Invalid UID (missing ':' separators): '{aUid}'")
        parts = aUid.split(':')
        if len(parts) != aEnumDataType.getUidPartCount():
            raise ValueError(f'Invalid UID (expected {aEnumDataType.getUidPartCount()} segments): {aUid!r}')
        return parts

    @staticmethod
    def parseUid(aEnumDataType, aUid):
        parts = AIsUidEnumDataUtils.splitUidIntoParts(aEnumDataType, aUid)
        origin = aEnumDataType.getOriginGetter()(parts[0])
        ns = parts[1]
        if not origin.namespaceUsed():
            if ns: raise ValueError('Builtin UID namespace must be empty')
        else:
            if not ns: raise ValueError('Custom UID namespace must be non-empty')
            AIsUidEnumDataUtils._safe_namespace(ns)
        for part, metadata in zip(parts[2:], aEnumDataType.getSpecificUidPartsMetadata()):
            AIsUidEnumDataUtils._safe_part(part, AIsUidEnumDataUtils._label(metadata))
        return aEnumDataType.getUidRecordFactory()(aUid, parts)

    @staticmethod
    def validateUid(aEnumDataType, aUid) -> None:
        AIsUidEnumDataUtils.parseUid(aEnumDataType, aUid)

    @staticmethod
    def isValidOutputTypeUid(aEnumDataType, aUid) -> bool:
        try: AIsUidEnumDataUtils.validateUid(aEnumDataType, aUid); return True
        except (ValueError, RuntimeError, TypeError): return False

    @staticmethod
    def getOrigin(aEnumDataType, aUid): return AIsUidEnumDataUtils.parseUid(aEnumDataType, aUid).origin()
    @staticmethod
    def getNamespace(aEnumDataType, aUid): return AIsUidEnumDataUtils.parseUid(aEnumDataType, aUid).namespace()
