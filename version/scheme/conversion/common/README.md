# Version Conversion Common

Shared contracts for explicit conversion between version schemes.

`AIiVersionConverter` defines the conversion SPI and `AIrVersionConversionResult` reports both the converted value and conversion quality. Quality distinguishes exact, lossless-normalized, lossy, and unsupported conversions so callers do not silently change version semantics.
