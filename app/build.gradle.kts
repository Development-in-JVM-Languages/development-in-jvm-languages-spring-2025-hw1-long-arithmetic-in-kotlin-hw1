import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
plugins {
    // Adds support for Kotlin on the JVM.
    kotlin("jvm") version "2.1.10"
}

repositories {
    // Use Maven Central to resolve dependencies.
    mavenCentral()
}

dependencies {
    // Standard Kotlin library for the JVM.
    implementation(kotlin("stdlib"))
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

// Configure the test task to use JUnit Platform for unit tests.
tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget = JVM_21
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Set Java target version
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21" // Set Kotlin target version
    }
}