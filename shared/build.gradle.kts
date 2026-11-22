import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Pure Kotlin Multiplatform math & logic (no platform-specific dependencies)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
