plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
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
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.jpascal.compiler.JPascal"
    }
}