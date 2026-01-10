plugins {
    `java-library`
    `maven-publish`
    `java-gradle-plugin`
}

group = "com.github.at-boundary"
version = "0.1.0"

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
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "metatest-gradle-plugin"
        }
    }
}
