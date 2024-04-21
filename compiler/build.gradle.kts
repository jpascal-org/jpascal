plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.jpascal"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":frontend"))
    implementation(project(":frontend-parser-antlr"))
    implementation(project(":frontend-api"))
    implementation(project(":backend"))

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.jpascal.compiler.JPascal"
    }
}