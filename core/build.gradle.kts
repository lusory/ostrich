@file:Suppress("PropertyName")

import me.lusory.ostrich.gen.qapi.WriterContext
import me.lusory.ostrich.gen.qapi.makeWriterContext
import me.lusory.ostrich.gen.qapi.model.*
import me.lusory.ostrich.gen.qapi.parseSchemaFile
import me.lusory.ostrich.gen.qapi.replaceReservedKeywords
import me.lusory.ostrich.gradle.DependencyVersions
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.internal.JvmPluginsHelper
import java.util.Collections

dependencies {
    api(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = DependencyVersions.JACKSON)
    compileOnly(group = "org.jetbrains", name = "annotations", version = DependencyVersions.JB_ANNOTATIONS)
}

val sourcesWorkingDir: File = file("build/qemu")
val qapiWorkingDir: File = file("build/qemu/qapi")
val generatedSourceDir: File = file("build/generatedQemu/main/java")

val VAR_ASSIGN_REGEX = Regex("this.([a-zA-Z_0-9]+) = ([a-zA-Z_0-9]+);")
val CTOR_PARAM_REGEX = Regex("final ([a-zA-Z_0-9]+) ([a-zA-Z_0-9]+)")
val IF_REGEX = Regex("if \\(([a-zA-Z_0-9]+) == null\\) \\{")

sourceSets.main {
    java {
        srcDir(generatedSourceDir)
    }

    // register delombok sourcesJar task
    JvmPluginsHelper.configureDocumentationVariantWithArtifact(
        JavaPlugin.SOURCES_ELEMENTS_CONFIGURATION_NAME,
        null,
        DocsType.SOURCES,
        Collections.emptyList(),
        sourcesJarTaskName,
        tasks.getByName("delombok").outputs,
        JvmPluginsHelper.findJavaComponent(components),
        configurations,
        tasks,
        objects,
        (project as ProjectInternal).fileResolver
    )
    tasks.getByName(sourcesJarTaskName).dependsOn("delombok")
}

// make the javadoc tool less sensitive to broken references
tasks.getByName<Javadoc>(JavaPlugin.JAVADOC_TASK_NAME) {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

tasks.getByName("delombok") {
    dependsOn("generateQapiModels")

    // manually replace reserved keywords in delombok output (OSTR-1)
    doLast {
        outputs.files.asFileTree.files.forEach { file ->
            if (file.isFile && file.extension == "java") {
                file.writeText(
                    file.readText()
                        .replace(CTOR_PARAM_REGEX) { result -> "final ${result.groupValues[1]} ${result.groupValues[2].replaceReservedKeywords()}" }
                        .replace(VAR_ASSIGN_REGEX) { result -> "this.${result.groupValues[1]} = ${result.groupValues[2].replaceReservedKeywords()};" }
                        .replace(IF_REGEX) { result -> "if (${result.groupValues[1].replaceReservedKeywords()} == null) {" }
                )
            }

            // delete lombok.config files, they are not needed and break javadoc generation
            if (file.name == "lombok.config") {
                file.delete()
            }
        }
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
            workingDir = sourcesWorkingDir

            // checkout only qapi schemas, command stubs and documentation
            commandLine = listOf("git", "checkout", "master", "*.json", "*.hx", "*.rst")
        }
    }
}

tasks.register("generateQapiModels") {
    dependsOn("pullQemuSources")

    doFirst {
        generatedSourceDir.deleteRecursively()
    }
    doLast {
        generatedSourceDir.mkdirs()

        generatedSourceDir.resolve("lombok.config").writeText(
            """
                config.stopbubbling = true
                lombok.equalsAndHashCode.callSuper = call
            """.trimIndent()
        )

        val schemas: List<SchemaFile> = qapiWorkingDir.walkTopDown()
            .filter { it.isFile }
            .map(::parseSchemaFile)
            .toList()

        val context: WriterContext = makeWriterContext(generatedSourceDir, schemas)

        schemas.forEach { schemaFile ->
            schemaFile.members.forEach { schema ->
                when (schema) {
                    is Enum0 -> context.writeEnum(schema)
                    is Struct -> context.writeStruct(schema)
                    is Union -> context.writeUnion(schema)
                    is Alternate -> context.writeAlternate(schema)
                    else -> println("Skipping unsupported schema type generation ${schema::class.simpleName}")
                }
            }
        }
    }
}
