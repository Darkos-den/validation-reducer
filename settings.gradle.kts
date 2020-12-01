pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
    }
}
rootProject.name = "validation-reducer"

enableFeaturePreview("GRADLE_METADATA")

include(":lib")

