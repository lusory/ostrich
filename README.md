# ostrich
[![Maven releases](https://repo.lusory.dev/api/badge/latest/releases/me/lusory/ostrich/core)](https://repo.lusory.dev/#/releases/me/lusory/ostrich/core)
[![Javadocs](https://img.shields.io/badge/javadocs-latest-yellow)](https://repo.lusory.dev/javadoc/releases/me/lusory/ostrich/core/latest)

A Java library for interfacing with QEMU.

## Features

* QAPI object POJOs generated from schemas (includes QGA)
* [Base implementation](https://github.com/lusory/ostrich/blob/master/core/src/main/java/me/lusory/ostrich/qapi/QAPISocket.java) of a QAPI socket
* Command-line wrappers of `qemu-system-*` and `qemu-img`

## Usage

Declare a Maven dependency through your build system (Gradle, Maven, ...) on a `me.lusory.ostrich:core:YOUR_QEMU_VERSION` artifact, hosted on `https://repo.lusory.dev/releases`.  

### Gradle (Kotlin DSL) example

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.lusory.dev/releases")
}

dependencies {
    implementation("me.lusory.ostrich:core:YOUR_QEMU_VERSION")
}
```

## Compiling

This library uses Gradle (Wrapper) to compile, so compiling is just as easy as running `gradlew shadowJar` in your terminal of choice. The built binary is placed in `./core/build/libs`.