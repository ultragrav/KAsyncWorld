import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("io.papermc.paperweight.userdev") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
    paperweightDevelopmentBundle("net.midnightsky.satellite:dev-bundle:1.0-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(kotlin("test"))
    implementation("net.ultragrav:KSerializer:1.1.0")
    implementation("net.ultragrav:Commands:1.5.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("KAsyncWorld")
    archiveClassifier.set("SNAPSHOT")
    archiveVersion.set("1.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}