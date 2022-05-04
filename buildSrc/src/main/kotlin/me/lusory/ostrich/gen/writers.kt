package me.lusory.ostrich.gen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import me.lusory.ostrich.gen.model.Enum0
import me.lusory.ostrich.gen.model.NamedSchema
import me.lusory.ostrich.gen.model.Schema
import me.lusory.ostrich.gen.model.SchemaFile
import java.io.File
import java.util.stream.Collectors
import javax.lang.model.element.Modifier

val RESERVED_KEYWORDS: Set<String> = setOf(
    "_",
    "abstract",
    "assert",
    "boolean",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "class",
    "const",
    "continue",
    "default",
    "do",
    "double",
    "else",
    "enum",
    "extends",
    "false",
    "final",
    "finally",
    "float",
    "for",
    "goto",
    "if",
    "implements",
    "import",
    "instanceof",
    "int",
    "interface",
    "Iterable",
    "long",
    "native",
    "new",
    "null",
    "Object",
    "package",
    "private",
    "protected",
    "public",
    "return",
    "RuntimeException",
    "short",
    "static",
    "static final",
    "strictfp",
    "super",
    "switch",
    "synchronized",
    "this",
    "throw",
    "throws",
    "transient",
    "true",
    "try",
    "undefined",
    "var",
    "void",
    "volatile",
    "while"
)

val QENUM: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QEnum")
val CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Condition")

fun String.replaceReservedKeywords(): String = replace('-', '_').let { s ->
    if (RESERVED_KEYWORDS.any { it == s }) "_$s" else s
}

fun makeWriterContext(sourceDir: File, schemas: List<SchemaFile>): WriterContext = WriterContext(
    sourceDir,
    names = schemas.stream()
        .flatMap { file ->
            val newName: String = file.name.replaceReservedKeywords()

            file.members.stream()
                .filter { it is NamedSchema }
                .map { newName to (it as NamedSchema) }
        }
        .collect(Collectors.toMap({ it.second.name }, { "me.lusory.ostrich.qapi.${it.first}.${it.second.name.replaceReservedKeywords()}" }))
)

data class WriterContext(
    val sourceDir: File,
    // short name - package name
    val names: Map<String, String>
) {
    fun writeEnum(enum0: Enum0) {
        val className: ClassName = ClassName.bestGuess(names[enum0.name])
        val builder: TypeSpec.Builder = TypeSpec.enumBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(QENUM)

        if (enum0.docString != null) {
            builder.addJavadoc(enum0.docString)
        }

        TODO("Finish this")
    }
}