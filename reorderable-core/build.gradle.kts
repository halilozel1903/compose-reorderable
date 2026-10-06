import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.kotlin.test)
}

mavenPublishing {
    publishToMavenCentral()
    // Sign only when a key is configured (Maven Central); JitPack and local builds stay unsigned.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    // JitPack serves artifacts under com.github.<user>.<repo>.
    val jitpackGroup = "com.github.halilozel1903.compose-reorderable".takeIf { System.getenv("JITPACK") == "true" }
    coordinates(groupId = jitpackGroup, artifactId = "compose-reorderable-core")
    pom {
        name.set("Compose Reorderable Core")
        description.set("Platform independent reordering logic behind compose-reorderable: list moves, locked items, drop target computation for lists and grids, auto-scroll speed, keyboard moves and accessibility action labels.")
    }
}
