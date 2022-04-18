import me.lusory.ostrich.gen.Generator

val sourcesWorkingDir: File = file("build/qemu")
val qapiWorkingDir: File = file("build/qemu/qapi")
val generatedSourceDir: File = file("src/generated")

sourceSets.create("generated")

tasks.register("pullQemuSources") {
    outputs.dir(sourcesWorkingDir)

    outputs.upToDateWhen { sourcesWorkingDir.isDirectory }

    doFirst {
        sourcesWorkingDir.deleteRecursively()
        sourcesWorkingDir.mkdirs()
    }
    doLast {
        exec {
            workingDir = sourcesWorkingDir

            commandLine = listOf("git", "clone", "--branch", "master", "--no-checkout", "--depth=1", "https://github.com/qemu/qemu.git", ".")
        }
        exec {
            this.workingDir = sourcesWorkingDir

            commandLine = listOf("git", "checkout", "master", "*.json")
        }
    }
}

tasks.register("cleanQemuSources") {
    doLast {
        sourcesWorkingDir.deleteRecursively()
        generatedSourceDir.deleteRecursively()
    }
}

tasks.getByName("clean").dependsOn("cleanQemuSources")

tasks.register("generateQapiModels") {
    dependsOn("pullQemuSources")

    doFirst {
        generatedSourceDir.deleteRecursively()
    }
    doLast {
        Generator(qapiWorkingDir.walkTopDown().filter { it.isFile }.toList(), sourceSets["generated"].java.srcDirs.first())
            .generate()
    }
}