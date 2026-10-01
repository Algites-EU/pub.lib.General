plugins {
    `maven-publish`
    `java-library`
}

java {
    sourceSets {
        val main by getting {
            java.setSrcDirs(listOf("src/product/java"))
        }
        val test by getting {
            java.setSrcDirs(listOf("src/develop/java"))
        }
    }
}

val JAKARTA_ANNOTATION_VERSION = "3.0.0"

dependencies {
    implementation("jakarta.annotation:jakarta.annotation-api:" + JAKARTA_ANNOTATION_VERSION)
    api(project(":version:core"))
}
