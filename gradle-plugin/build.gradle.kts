plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.metatest"
version = "0.1.0"

dependencies {
    implementation(gradleApi())
    compileOnly("org.slf4j:slf4j-api:2.0.9")
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
            description = "Gradle plugin for Metatest - REST API mutation testing"
        }
    }
}
