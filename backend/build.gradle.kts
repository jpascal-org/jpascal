plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "org.jpascal"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":frontend-api"))
    implementation(project(":frontend"))
    implementation(project(":frontend-parser-antlr"))
    implementation(project(":stdlib"))

    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(libs.asm.util)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}