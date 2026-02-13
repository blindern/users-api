import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.Instant

plugins {
  application
  id("org.jetbrains.kotlin.jvm") version "2.3.10"
  id("com.gradleup.shadow") version "9.3.1"
  id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
  id("com.github.ben-manes.versions") version "0.53.0"
}

group = "no.foreningenbs"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.http4k:http4k-core:6.28.1.0")
  implementation("org.http4k:http4k-server-jetty:6.28.1.0")
  implementation("org.http4k:http4k-format-moshi:6.28.1.0")
  implementation("com.natpryce:konfig:1.6.10.0")
  implementation("com.squareup.moshi:moshi:1.15.2")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
  implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10")
  implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")
  implementation("ch.qos.logback:logback-classic:1.5.29")
  implementation("io.github.oshai:kotlin-logging:7.0.14")
  testImplementation("io.kotest:kotest-assertions-core:6.1.3")
  testImplementation("io.mockk:mockk:1.14.9")
  testImplementation("io.kotest:kotest-runner-junit5:6.1.3")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation("com.karumi.kotlinsnapshot:core:2.3.0")
}

kotlin {
  jvmToolchain(21)
}

application {
  mainClass.set("no.foreningenbs.usersapi.MainKt")
}

tasks.withType<ShadowJar> {
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.register("dockerBuildProperties") {
  val props =
    mapOf(
      "build.timestamp" to Instant.now().toString(),
      "build.commit" to (System.getenv("GITHUB_SHA") ?: ""),
      "build.url" to (
        System.getenv("GITHUB_RUN_ID")?.let { runId ->
          "${System.getenv("GITHUB_SERVER_URL")}/${System.getenv("GITHUB_REPOSITORY")}/actions/runs/$runId"
        } ?: ""
      ),
    )

  inputs.properties(props)
  val outputFile = layout.buildDirectory.file("build.properties")
  outputs.file(outputFile)

  doLast {
    outputFile.get().asFile.bufferedWriter().use {
      props.toProperties().store(it, "Written by Gradle")
    }
  }
}

tasks.withType<DependencyUpdatesTask> {
  resolutionStrategy {
    componentSelection {
      all(
        Action<ComponentSelection> {
          val rejected =
            listOf("alpha", "beta", "rc", "cr", "m", "preview", "eap")
              .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
              .any { it.matches(candidate.version) }
          if (rejected) {
            reject("Release candidate")
          }
        },
      )
    }
  }
}
