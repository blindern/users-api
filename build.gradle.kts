import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.fuerstenau.gradle.buildconfig.BuildConfigSourceSet
import java.io.ByteArrayOutputStream
import java.time.Instant
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  id("com.github.johnrengelman.shadow") version "5.2.0"
  id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
  id("com.github.ben-manes.versions") version "0.28.0"
  id("de.fuerstenau.buildconfig") version "1.1.8"
}

group = "no.foreningenbs"
version = "1.0-SNAPSHOT"

buildscript {
  dependencies {
    classpath("com.karumi.kotlinsnapshot:plugin:2.2.0")
  }
}

fun getGitHash(): String {
  val stdout = ByteArrayOutputStream()
  exec {
    commandLine("git", "rev-parse", "HEAD")
    standardOutput = stdout
  }
  return stdout.toString().trim()
}

configure<BuildConfigSourceSet> {
  buildConfigField("String", "BUILD_TIME", Instant.now().toString())
  buildConfigField("String", "GIT_COMMIT", getGitHash())
}

repositories {
  mavenCentral()
  jcenter()
  maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
  maven { setUrl("https://dl.bintray.com/spekframework/spek-dev") }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.http4k:http4k-core:3.243.0")
  implementation("org.http4k:http4k-server-jetty:3.242.0")
  implementation("org.http4k:http4k-format-moshi:3.242.0")
  implementation("com.natpryce:konfig:1.6.10.0")
  implementation("com.squareup.moshi:moshi:1.9.2")
  implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.72")
  implementation("com.github.ben-manes.caffeine:caffeine:2.8.1")
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
  implementation("de.gessnerfl.logback:logback-gson-formatter:0.1.0")
  implementation("io.github.microutils:kotlin-logging:1.7.9")
  testImplementation("org.amshove.kluent:kluent:1.60")
  testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.10")
  testImplementation("io.mockk:mockk:1.9.3")
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.10")
}

// The idea plugin makes generated BuildConfig resolved
apply(plugin = "idea")

apply(plugin = "com.karumi.kotlin-snapshot")

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

application {
  mainClassName = "no.foreningenbs.usersapi.MainKt"
}

tasks.withType<ShadowJar> {
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform {
    includeEngines("spek2")
  }
}

tasks.register("buildDocker", Exec::class.java) {
  dependsOn("shadowJar")
  environment(
    "CIRCLE_BRANCH" to "local-test",
    "CIRCLE_BUILD_NUM" to "0"
  )
  commandLine("./scripts/docker-build-image.sh")
}

tasks.register("runDocker", Exec::class.java) {
  dependsOn("buildDocker")
  commandLine("./scripts/docker-run-image.sh".split(" "))
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
