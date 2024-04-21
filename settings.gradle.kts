plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "jpascal"
include("frontend-api")
include("backend")
include("frontend-parser-antlr")
include("frontend")
include("stdlib")
include("compiler")
