import me.lusory.ostrich.gradle.addPublication

plugins {
    id("io.freefair.lombok") version "6.4.2" apply false
    `maven-publish`
    `java-library`
}

allprojects {
    group = "me.lusory.ostrich"
    version = "7.0.0"
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

    configure<PublishingExtension> {
        repositories {
            maven {
                url = if ((project.version as String).endsWith("-SNAPSHOT")) uri("https://repo.lusory.dev/snapshots")
                    else uri("https://repo.lusory.dev/releases")
                credentials {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }
        }
    }
}
