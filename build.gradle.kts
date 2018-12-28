import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.fuerstenau.gradle.buildconfig.BuildConfigSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar
import java.io.ByteArrayOutputStream
import java.time.Instant

plugins {
  application
  kotlin("jvm") version "1.3.11"
  id("com.github.johnrengelman.shadow") version "4.0.3"
  id("org.jlleitschuh.gradle.ktlint") version "6.3.1"
  id("de.fuerstenau.buildconfig") version "1.1.8"
}

group = "no.foreningenbs"
version = "1.0-SNAPSHOT"

buildscript {
  dependencies {
    classpath("com.karumi.kotlinsnapshot:plugin:2.0.0")
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
  maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
  maven { setUrl("https://dl.bintray.com/spekframework/spek-dev") }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.http4k", "http4k-core", "3.103.2")
  implementation("org.http4k", "http4k-server-jetty", "3.103.2")
  implementation("org.http4k", "http4k-format-moshi", "3.103.2")
  implementation("com.natpryce", "konfig", "1.6.10.0")
  implementation("com.squareup.moshi", "moshi", "1.8.0")
  implementation("com.squareup.moshi", "moshi-kotlin", "1.8.0")
  implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.11")
  implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.3.11")
  implementation("com.github.ben-manes.caffeine", "caffeine", "2.6.2")
  implementation("ch.qos.logback", "logback-classic", "1.2.3")
  implementation("ch.qos.logback.contrib", "logback-json-classic", "0.1.5")
  implementation("de.gessnerfl.logback", "logback-gson-formatter", "0.1.0")
  implementation("io.github.microutils", "kotlin-logging", "1.6.22")
  testImplementation("org.amshove.kluent", "kluent", "1.45")
  testImplementation("org.spekframework.spek2", "spek-dsl-jvm", "2.0.0-rc.1")
  testImplementation("io.mockk", "mockk", "1.8.13.kotlin13")
  testRuntimeOnly("org.spekframework.spek2", "spek-runner-junit5", "2.0.0-rc.1")
}

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
