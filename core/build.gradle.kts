@file:Suppress("PropertyName")

import me.lusory.ostrich.gen.cmd.writeQemuImg
import me.lusory.ostrich.gen.cmd.writeQemuSystem
import me.lusory.ostrich.gen.qapi.QAPIWriterContext
import me.lusory.ostrich.gen.qapi.makeQapiWriterContext
import me.lusory.ostrich.gen.qapi.model.*
import me.lusory.ostrich.gen.qapi.parseSchemaFile
import me.lusory.ostrich.gen.qapi.replaceReservedKeywords
import me.lusory.ostrich.gradle.DependencyVersions
import me.lusory.ostrich.gradle.enableTests
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

lombok {
    disableConfig.set(true)
}

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
    dependsOn("generateQapiModels", "generateCommandWrappers")

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

tasks.withType<JavaCompile> {
    dependsOn("generateQapiModels", "generateCommandWrappers")
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

            commandLine = listOf("git", "clone", "--branch", "v${(version as String).replace("-SNAPSHOT", "")}", "--depth=1", "https://github.com/qemu/qemu.git", ".")
        }
    }
}

tasks.register("generateQapiModels") {
    dependsOn("pullQemuSources")

    val qapiDir: File = generatedSourceDir.resolve("me/lusory/ostrich/qapi")
    outputs.dir(qapiDir)

    outputs.upToDateWhen { qapiDir.isDirectory }

    doFirst {
        if (!generatedSourceDir.isDirectory) {
            generatedSourceDir.mkdirs()
        }
    }
    doLast {
        generatedSourceDir.resolve("lombok.config").writeText(
            """
                config.stopbubbling = true
                lombok.equalsAndHashCode.callSuper = call
            """.trimIndent()
        )

        val schemas: MutableList<SchemaFile> = qapiWorkingDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .map(::parseSchemaFile)
            .toMutableList()

        schemas.add(parseSchemaFile(sourcesWorkingDir.resolve("qga/qapi-schema.json"), name = "qga"))

        val context: QAPIWriterContext = makeQapiWriterContext(generatedSourceDir, schemas)

        schemas.forEach { schemaFile ->
            schemaFile.members.forEach { schema ->
                when (schema) {
                    is Enum0 -> context.writeEnum(schema)
                    is Struct -> context.writeStruct(schema)
                    is Union -> context.writeUnion(schema)
                    is Alternate -> context.writeAlternate(schema)
                    is Event -> context.writeEvent(schema)
                    is Command -> context.writeCommand(schema)
                    else -> logger.info("Skipping unsupported schema type generation ${schema::class.simpleName}")
                }
            }
        }

        context.writeEventsMeta(schemas.flatMap { it.members }.filterIsInstance(Event::class.java))
    }
}

tasks.register("generateCommandWrappers") {
    dependsOn("pullQemuSources")

    val cmdDir: File = generatedSourceDir.resolve("me/lusory/ostrich/cmd")
    outputs.dir(cmdDir)

    outputs.upToDateWhen { cmdDir.isDirectory }

    doFirst {
        if (!generatedSourceDir.isDirectory) {
            generatedSourceDir.mkdirs()
        }
    }
    doLast {
        writeQemuImg(
            generatedSourceDir,
            sourcesWorkingDir.resolve("qemu-img-cmds.hx"),
            sourcesWorkingDir.resolve("docs/tools/qemu-img.rst")
        )
        writeQemuSystem(
            generatedSourceDir,
            sourcesWorkingDir.resolve("qemu-options.hx")
        )
    }
}

enableTests()
