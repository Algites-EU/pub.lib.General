# Maven to Gradle Version Conversion

Converts Maven-native dependency requirements into Gradle rich version constraints.

Simple numeric Maven ranges are normalized into Gradle range syntax. Qualifier-bearing ranges are explicitly reported as lossy because Maven and Gradle can order qualifier versions differently.
