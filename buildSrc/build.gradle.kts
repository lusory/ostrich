plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

dependencies {
    implementation(group = "net.fabricmc", name = "javapoet", version = "0.1.0")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.14.0-rc2")
}