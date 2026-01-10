plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.aspectj.post-compile-weaving") version "8.6"
}

group = "com.github.at-boundary"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "metatest"
        }
    }
}

dependencies {
    testImplementation(project(":lib"))
    implementation("org.aspectj:aspectjrt:1.9.22")
    api("org.aspectj:aspectjweaver:1.9.22")
    implementation("io.rest-assured:rest-assured:5.5.6")
    implementation("io.rest-assured:json-path:5.3.0")
    testImplementation("org.projectlombok:lombok:1.18.26")
    compileOnly("org.aspectj:aspectjtools:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
//    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.junit.platform:junit-platform-launcher:1.9.3")
    api("org.apache.commons:commons-math3:3.6.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.json:json:20250107")
    implementation("com.github.tomakehurst:wiremock:3.0.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    implementation("io.swagger.parser.v3:swagger-parser:2.1.22")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

// Gradle plugin has been moved to separate module: gradle-plugin/
// This avoids Groovy version conflicts in tests

tasks.test {
    useJUnitPlatform()
    val aspectjAgent = configurations.runtimeClasspath.get().find { it.name.contains("aspectjweaver") }?.absolutePath
    val runWithMetatest = System.getProperty("runWithMetatest") == "true"

    val jvmArguments = mutableListOf(
        "-Xmx2g",
        "-Xms512m"
    )

    if (runWithMetatest && aspectjAgent != null) {
        jvmArguments.add("-javaagent:${aspectjAgent}")
        // jvmArguments.addAll(listOf(
        //     "-Daj.weaving.verbose=true",
        //     "-Dorg.aspectj.weaver.showWeaveInfo=true",
        //     "-Dorg.aspectj.matcher.verbosity=5"
        // ))
    }
    jvmArguments.add("-DrunWithMetatest=${System.getProperty("runWithMetatest")}")

    jvmArgs = jvmArguments
}