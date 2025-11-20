plugins {
    kotlin("jvm") version "2.3.0-Beta2"
    application
}

repositories {
    mavenCentral()
}

dependencies {
//    implementation("io.ktor:ktor-client-core:1.5.4")
    implementation("io.ktor:ktor-client-cio:3.0.0-beta-1")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("com.google.code.gson:gson:2.9.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application { mainClass.set("MainKt") }
