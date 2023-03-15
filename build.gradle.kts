import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

plugins {
  application
  id("org.jetbrains.kotlin.jvm") version "1.8.10"
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
  id("com.github.ben-manes.versions") version "0.42.0"
}

group = "no.foreningenbs"
version = "1.0-SNAPSHOT"

buildscript {
  dependencies {
    classpath("com.karumi.kotlinsnapshot:plugin:2.3.0")
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.http4k:http4k-core:4.40.1.0")
  implementation("org.http4k:http4k-server-jetty:4.40.1.0")
  implementation("org.http4k:http4k-format-moshi:4.40.1.0")
  implementation("com.natpryce:konfig:1.6.10.0")
  implementation("com.squareup.moshi:moshi:1.14.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.22")
  implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
  implementation("ch.qos.logback:logback-classic:1.4.5")
  implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
  implementation("de.gessnerfl.logback:logback-gson-formatter:0.1.0")
  implementation("io.github.microutils:kotlin-logging:2.1.23")
  testImplementation("io.kotest:kotest-assertions-core:5.5.5")
  testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.19")
  testImplementation("io.mockk:mockk:1.13.4")
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.19")
}

apply(plugin = "com.karumi.kotlin-snapshot")

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}

application {
  mainClass.set("no.foreningenbs.usersapi.MainKt")
}

tasks.withType<ShadowJar> {
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform {
    includeEngines("spek2")
  }
}

tasks.register("dockerBuildProperties") {
  val props = mapOf(
    "build.timestamp" to Instant.now().toString(),
    "build.commit" to (System.getenv("GITHUB_SHA") ?: ""),
    "build.url" to (
      System.getenv("GITHUB_RUN_ID")?.let { runId ->
        "${System.getenv("GITHUB_SERVER_URL")}/${System.getenv("GITHUB_REPOSITORY")}/actions/runs/$runId"
      } ?: ""
      )
  )

  inputs.properties(props)
  outputs.file("$buildDir/build.properties")

  doLast {
    File("$buildDir/build.properties").bufferedWriter().use {
      props.toProperties().store(it, "Written by Gradle")
    }
  }
}

tasks.withType<DependencyUpdatesTask> {
  resolutionStrategy {
    componentSelection {
      all {
        val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "eap")
          .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
          .any { it.matches(candidate.version) }
        if (rejected) {
          reject("Release candidate")
        }
      }
    }
  }
}
