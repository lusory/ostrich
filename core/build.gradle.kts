import me.lusory.ostrich.gen.WriterContext
import me.lusory.ostrich.gen.makeWriterContext
import me.lusory.ostrich.gen.parseSchemaFile
import me.lusory.ostrich.gen.model.SchemaFile
import me.lusory.ostrich.gen.model.Enum0
import me.lusory.ostrich.gradle.DependencyVersions

dependencies {
    compileOnly(group = "org.jetbrains", name = "annotations", version = DependencyVersions.JB_ANNOTATIONS)
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = DependencyVersions.JACKSON)
}

val sourcesWorkingDir: File = file("build/qemu")
val qapiWorkingDir: File = file("build/qemu/qapi")
val generatedSourceDir: File = file("build/generated/main/java")

sourceSets.main {
    java {
        srcDir(generatedSourceDir)
    }
}

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
        val schemas: List<SchemaFile> = qapiWorkingDir.walkTopDown()
            .filter { it.isFile }
            .map(::parseSchemaFile)
            .toList()

        val context: WriterContext = makeWriterContext(generatedSourceDir, schemas)

        schemas.forEach { schemaFile ->
            schemaFile.members.forEach { schema ->
                when (schema) {
                    is Enum0 -> context.writeEnum(schema)
                    else -> println("Skipping unsupported schema type generation ${schema::class.simpleName}")
                }
            }
        }
    }
}