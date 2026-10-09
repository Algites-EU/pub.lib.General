#!/usr/bin/env bash
set -euo pipefail
# Apply to the original Git checkout BEFORE overlaying the new 1.1 source tree.
# This script performs only tracked relocations; new descriptors are supplied by the ZIP.
cd "$(git rev-parse --show-toplevel)"
git diff --quiet || { echo "Working tree has unstaged changes" >&2; exit 1; }
git diff --cached --quiet || { echo "Working tree has staged changes" >&2; exit 1; }
if [[ -d naming/convention/intf || -d version/commonimpl ]]; then echo "Artifact migration appears already applied" >&2; exit 1; fi
mkdir -p naming/convention
git mv naming/convention/coreintf naming/convention/intf
mkdir -p naming/convention
git mv naming/convention/coreimpl naming/convention/impl
mkdir -p naming/conversion
git mv naming/conversion/coreintf naming/conversion/intf
mkdir -p naming/conversion
git mv naming/conversion/coreimpl naming/conversion/impl
mkdir -p naming/validation
git mv naming/validation/coreintf naming/validation/intf
mkdir -p naming/validation
git mv naming/validation/coreimpl naming/validation/impl
mkdir -p documentation/impl
git mv documentation/modustro-artifact.yml documentation/impl/modustro-artifact.yml
mkdir -p documentation/impl
git mv documentation/src documentation/impl/src
mkdir -p documentation
git mv documentation/README.md documentation/README.md.MIGRATION_TMP
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationBlockElement.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationBlockElement.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationBlockElementWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationBlockElementWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationCodeBlock.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationCodeBlock.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationCodeBlockWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationCodeBlockWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationContainerElement.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationContainerElement.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationContainerElementWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationContainerElementWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationDocument.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationDocument.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationDocumentWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationDocumentWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationElement.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationElement.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationElementWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationElementWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineCode.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineCode.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineCodeWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineCodeWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineElement.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineElement.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineElementWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationInlineElementWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLineBreak.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLineBreak.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLineBreakWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLineBreakWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLink.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLink.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLinkWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationLinkWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationParagraph.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationParagraph.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationParagraphWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationParagraphWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReference.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReference.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceTarget.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceTarget.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceTargetWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceTargetWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationReferenceWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationSection.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationSection.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationSectionWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationSectionWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTable.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTable.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableCell.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableCell.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableCellWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableCellWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableColumn.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableColumn.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableColumnWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableColumnWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableRow.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableRow.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableRowWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableRowWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTableWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationText.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationText.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTextWriteAccess.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/AIiDocumentationTextWriteAccess.java
mkdir -p documentation/intf/src/product/java/eu/algites/lib/common/documentation/model
git mv documentation/impl/src/product/java/eu/algites/lib/common/documentation/model/package-info.java documentation/intf/src/product/java/eu/algites/lib/common/documentation/model/package-info.java
mkdir -p documentation
git mv documentation/README.md.MIGRATION_TMP documentation/README.md
mkdir -p version
git mv version/core version/commonimpl
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcBuildAwareVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcBuildAwareVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcCalverLikeVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcCalverLikeVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcDefaultVersionCodec.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcDefaultVersionCodec.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcDefaultVersionFormatter.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcDefaultVersionFormatter.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcMavenLikeVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcMavenLikeVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcSemverLikeVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcSemverLikeVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcVersion.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcVersion.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcVersionSchemeDataType.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcVersionSchemeDataType.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIcVersionToken.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIcVersionToken.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionBound.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionBound.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionCodec.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionCodec.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionFormat.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionFormat.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionFormatter.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionFormatter.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionQualifier.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionQualifier.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionRequirement.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionRequirement.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionScheme.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionScheme.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeData.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeData.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeDataType.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeDataType.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeDataUidRecord.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeDataUidRecord.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeTextParts.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionSchemeTextParts.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIiVersionStructure.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIiVersionStructure.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionFormat.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionFormat.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionScheme.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionScheme.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionStructure.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInBuiltinVersionStructure.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInVersionBuildComparisonPolicy.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInVersionBuildComparisonPolicy.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInVersionBuildFormatPolicy.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInVersionBuildFormatPolicy.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInVersionQualifierKind.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInVersionQualifierKind.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AInVersionTokenType.java version/commonintf/src/product/java/eu/algites/lib/common/version/AInVersionTokenType.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIrVersionQualifier.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIrVersionQualifier.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIrVersionRequirementNormalization.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIrVersionRequirementNormalization.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIrVersionSchemeDataUidRecord.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIrVersionSchemeDataUidRecord.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIrVersionSchemeTextParts.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIrVersionSchemeTextParts.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIsVersionComparator.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIsVersionComparator.java
mkdir -p version/commonintf/src/product/java/eu/algites/lib/common/version
git mv version/commonimpl/src/product/java/eu/algites/lib/common/version/AIsVersionTokenizer.java version/commonintf/src/product/java/eu/algites/lib/common/version/AIsVersionTokenizer.java
mkdir -p version
git mv version/commonimpl/README.md version/README.md
echo "Git moves completed. Overlay the full 1.1 ZIP and run git add -A to stage new descriptors and updates."
