plugins {
    `maven-publish`
    `kotlin-dsl`
    `java-gradle-plugin`
}


java {
    sourceSets {
        val main by getting {
            java.setSrcDirs(listOf("src/product/java", "src/product/kotlin"))
            resources.setSrcDirs(listOf("src/product/resources","src/product/loader"))
        }
        val test by getting {
            java.setSrcDirs(listOf("src/develop/java", "src/develop/kotlin"))
            resources.setSrcDirs(listOf("src/develop/resources","src/develop/loader"))
        }
    }
}

val JAKARTA_ANNOTATION_VERSION = "3.0.0"
val ALGITES_PUB_LIB_GENERAL_VERSION = project.version.toString()

dependencies {
    implementation("jakarta.annotation:jakarta.annotation-api:" + JAKARTA_ANNOTATION_VERSION)
    api(project(":common"))
    testImplementation(project(":common"))
}

