package me.lusory.ostrich.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

fun Project.addPublication() {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    name.set("ostrich")
                    description.set("A Java library for interfacing with QEMU")
                    url.set("https://github.com/lusory/ostrich")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://github.com/lusory/ostrich/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("zlataovce")
                            name.set("Matouš Kučera")
                            email.set("mk@kcra.me")
                        }
                        developer {
                            id.set("tlkh40") // troll
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/lusory/ostrich.git")
                        developerConnection.set("scm:git:ssh://github.com/lusory/ostrich.git")
                        url.set("https://github.com/lusory/ostrich/tree/master")
                    }
                }
            }
        }
    }
}

fun Project.enableTests() {
    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter-api:${DependencyVersions.JUNIT}")
        add("testImplementation", "org.mockito:mockito-junit-jupiter:${DependencyVersions.MOCKITO}")
        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:${DependencyVersions.JUNIT}")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}