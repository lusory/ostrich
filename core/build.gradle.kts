import me.lusory.ostrich.gen.Generator

sourceSets.create("generated") {
    java {
        srcDir("${project.buildDir}/generated/java")
    }
}

tasks.register("pullQemuSources") {
    val workingDir: File = file("${project.buildDir}/qemu")

    outputs.dir(workingDir)

    doFirst {
        workingDir.deleteRecursively()
        workingDir.mkdirs()
    }
    doLast {
        exec {
            this.workingDir = workingDir

            commandLine = listOf("git", "clone", "--branch", "master", "--no-checkout", "--depth=1", "https://github.com/qemu/qemu.git", ".")
        }
        exec {
            this.workingDir = workingDir

            commandLine = listOf("git", "checkout", "master", "*.json")
        }
    }
}

tasks.register("cleanQemuSources") {
    val workingDir: File = file("${project.buildDir}/qemu")

    doLast {
        workingDir.deleteRecursively()
    }
}

tasks.getByName("clean").dependsOn("cleanQemuSources")

tasks.register("generateQapiModels") {
    dependsOn("pullQemuSources")
    val workingDir: File = file("${project.buildDir}/qemu/qapi")

    doLast {
        Generator(workingDir.walkTopDown().filter { it.isFile }.toList(), sourceSets["generated"].java.srcDirs.first()).readers.forEach { println(it) }
    }
}