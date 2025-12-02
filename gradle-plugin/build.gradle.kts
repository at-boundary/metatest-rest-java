import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    `maven-publish`
    `java-gradle-plugin`
}

val gitCommitHash: Provider<String> = project.provider {
    try {
        val process = ProcessBuilder("git", "rev-parse", "--short=7", "HEAD").start()
        val outputStream = ByteArrayOutputStream()
        process.inputStream.copyTo(outputStream)
        process.waitFor()
        outputStream.toString().trim()
    } catch (e: Exception) {
        println("Error getting git commit hash: ${e.message}")
        "unknown"
    }
}

group = "io.metatest"
version = if (gitCommitHash.get() != "unknown") "1.0.0-dev-${gitCommitHash.get()}" else "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // DO NOT depend on :lib here - that would pull all dependencies into plugin classpath
    // The library is a separate dependency that users add to their testImplementation

    // Gradle API is automatically added by java-gradle-plugin
    implementation(gradleApi())

    // SLF4J for logging (provided by Gradle at runtime)
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    // Lombok for cleaner code
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

gradlePlugin {
    plugins {
        create("metatestPlugin") {
            id = "io.metatest"
            implementationClass = "metatest.gradle.MetatestPlugin"
            displayName = "Metatest Plugin"
            description = "Gradle plugin for Metatest that automatically configures AspectJ weaving for REST API mutation testing"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/at-boundary/metatest-rest-java")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        mavenLocal()
    }
}
