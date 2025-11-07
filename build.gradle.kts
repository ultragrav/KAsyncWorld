import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.tasks.RemapJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}
configurations.all {
    exclude(group = "me.lucko", module = "spark-paper")
}


group = "net.ultragrav"
version = "1.21.1-1.0.25"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    paperweightDevelopmentBundle("net.midnightsky.satellite:dev-bundle:1.21.1-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("net.ultragrav:Commands:1.5.3")

    testImplementation(kotlin("test"))
    compileOnly("net.ultragrav:KSerializer:1.1.8")
    implementation("org.lz4:lz4-java:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "20"
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

val reobfJar = tasks.named<RemapJar>("reobfJar")
publishing {
    publications.register<MavenPublication>("mavenJava") {
        setArtifacts(listOf(artifact(reobfJar) { builtBy(reobfJar) }))
    }
}