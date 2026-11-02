# Codegen qualified artifact inputs — shared bootstrap checkpoint

Date: 2026-11-01. Baseline: pub.lib.General r11.

The shared `devops/bootstrap` files are kept byte-identical to those in
pub.tool.General. Changes in this repository are limited to the shared
bootstrap scripts. No Java or Python library API or generated source was
manually modified. Source-mode bootstrap now exports the set of prepared
Modustro dependency identifiers so Codegen can reject undeclared internal
artifact inputs instead of using stale local binaries. The generation adapter
passes qualified keys directly rather than constructing obsolete CLI values.

The complete Gradle bootstrap could not be run here because Gradle 9.2.1 is
not cached and the distribution download fails DNS resolution. Run a full
source-mode bootstrap locally before treating this checkpoint as verified.
