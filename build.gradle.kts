import me.lusory.ostrich.gradle.addPublication

plugins {
    id("io.freefair.lombok") version "6.4.2" apply false
    `maven-publish`
    `java-library`
}

allprojects {
    group = "me.lusory.ostrich"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("io.freefair.lombok")
    }

    repositories {
        mavenCentral()
    }

    addPublication()

    java.withJavadocJar()
}
