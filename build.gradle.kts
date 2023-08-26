import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.papermc.paperweight.userdev") version "1.5.0"
}

group = "net.ultragrav.kasyncworld"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.1-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}