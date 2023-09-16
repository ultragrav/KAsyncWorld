import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.papermc.paperweight.userdev") version "1.5.0"
}

group = "net.ultragrav"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    paperweightDevelopmentBundle("io.papermc.paper:dev-bundle:1.20.1-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
    api("net.ultragrav:McNBT:1.0.0")
    implementation("net.ultragrav:KSerializer:1.1.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}