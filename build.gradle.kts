import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
  application
  kotlin("jvm") version "1.3.11"
  id("com.github.johnrengelman.shadow") version "4.0.2"
  id("org.jlleitschuh.gradle.ktlint") version "6.3.1"
}

group = "no.foreningenbs"
version = "1.0-SNAPSHOT"

buildscript {
  dependencies {
    classpath("com.karumi.kotlinsnapshot:plugin:2.0.0")
  }
}

repositories {
  mavenCentral()
  jcenter()
  maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
  maven { setUrl("https://dl.bintray.com/spekframework/spek-dev") }
  // maven { setUrl("https://plugins.gradle.org/m2/") }
  // maven { setUrl("https://dl.bintray.com/markusamshove/maven/") }
}

val spekVersion = "2.0.0-alpha.2"

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.http4k", "http4k-core", "3.94.1")
  implementation("org.http4k", "http4k-server-jetty", "3.94.1")
  implementation("org.http4k", "http4k-format-moshi", "3.94.1")
  implementation("com.natpryce", "konfig", "1.6.10.0")
  implementation("com.squareup.moshi", "moshi", "1.7.0")
  implementation("com.squareup.moshi", "moshi-kotlin", "1.7.0")
  implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin", "kotlin-reflect")
  implementation("com.github.ben-manes.caffeine", "caffeine", "2.6.2")
  implementation("ch.qos.logback", "logback-classic", "1.2.3")
  implementation("ch.qos.logback.contrib", "logback-json-classic", "0.1.5")
  implementation("de.gessnerfl.logback", "logback-gson-formatter", "0.1.0")
  testImplementation("org.amshove.kluent", "kluent", "1.45")
  testImplementation("org.spekframework.spek2", "spek-dsl-jvm", spekVersion)
  testImplementation("io.mockk", "mockk", "1.8.13.kotlin13")
  testRuntimeOnly("org.spekframework.spek2", "spek-runner-junit5", spekVersion)
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
