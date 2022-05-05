package me.lusory.ostrich.gen

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import me.lusory.ostrich.gen.model.*
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.util.Arrays
import java.util.Locale
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
val CONDITION_TYPE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "ConditionType")
val FEATURE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Feature")
val LIST: ClassName = ClassName.get(java.util.List::class.java)
val TRANSFORM_UTILS: ClassName = ClassName.get("me.lusory.ostrich.qapi.util", "TransformUtils")

val NULLABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build()
val UNMODIFIABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Unmodifiable")).build()
val JSON_VALUE: AnnotationSpec = AnnotationSpec.builder(JsonValue::class.java).build()
val JSON_CREATOR: AnnotationSpec = AnnotationSpec.builder(JsonCreator::class.java).build()

val NULL_CODEBLOCK: CodeBlock = CodeBlock.of("null")

private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance()

fun String.replaceReservedKeywords(): String = replace('-', '_').let { s ->
    if (RESERVED_KEYWORDS.contains(s)) {
        return@let "_$s"
    }
    try {
        // sanitize numbers
        NUMBER_FORMAT.parse(s)
        return@let "_$s"
    } catch (ignored: ParseException) {
        // ignored
    }
    return@let s
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
        val fullClassName: String = names[enum0.name] ?: error("Could not get package name for ${enum0.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val builder: TypeSpec.Builder = TypeSpec.enumBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(QENUM)
            .writeCondition(enum0.`if`, "IF", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .writeFeatures(enum0.features, "FEATURES", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addMethod(
                MethodSpec.methodBuilder("valueOfRaw")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(className)
                    .addAnnotation(JSON_CREATOR)
                    .addParameter(java.lang.String::class.java, "name")
                    .addStatement("return valueOf(\$T.replaceReservedKeywords(name.toUpperCase(\$T.ROOT)))", TRANSFORM_UTILS, Locale::class.java)
                    .build()
            )

        if (enum0.docString != null) {
            builder.addJavadoc(enum0.docString)
        }

        enum0.data.forEach { value ->
            builder.addEnumConstant(
                value.name.toUpperCase().replaceReservedKeywords(),
                TypeSpec.anonymousClassBuilder("")
                    .writeCondition(value.`if`, "if_", Modifier.PRIVATE, Modifier.FINAL)
                    .addGetter("getIf", "if_", CONDITION, override_ = true)
                    .writeFeatures(value.features, "features", Modifier.PRIVATE, Modifier.FINAL)
                    .addGetter("getFeatures", "features", ParameterizedTypeName.get(LIST, FEATURE), override_ = true)
                    .addToString(value.name, JSON_VALUE)
                    .build()
            )
        }

        JavaFile.builder(className.packageName(), builder.build())
            .addFileComment("This file was generated with ostrich. Do not edit, changes will be overwritten!")
            .skipJavaLangImports(true)
            .indent("    ") // 4 space indent
            .build()
            .writeTo(sourceDir)
    }
}

fun TypeSpec.Builder.writeCondition(condition: Condition?, fieldName: String, vararg modifiers: Modifier): TypeSpec.Builder = apply {
    addField(
        FieldSpec.builder(CONDITION, fieldName, *modifiers)
            .addAnnotation(NULLABLE)
            .apply {
                if (condition != null) {
                    initializer(condition.write())
                } else {
                    initializer(NULL_CODEBLOCK)
                }
            }
            .build()
    )
}

fun TypeSpec.Builder.writeFeatures(features: List<Feature>, fieldName: String, vararg modifiers: Modifier): TypeSpec.Builder = apply {
    addField(
        FieldSpec.builder(ParameterizedTypeName.get(LIST, FEATURE), fieldName, *modifiers)
            .addAnnotation(UNMODIFIABLE)
            .initializer(
                CodeBlock.builder()
                    .writeList(features.map { it.write() })
                    .build()
            )
            .build()
    )
}

fun Condition.write(): CodeBlock = CodeBlock.builder()
    .add("\$T.of(\$T.${type.name}, ", CONDITION, CONDITION_TYPE)
    .writeList(conditions.map { it.write() })
    .add(", \$S)", value)
    .build()

fun Feature.write(): CodeBlock = CodeBlock.builder()
    .add("\$T.of(\$S, ", FEATURE, name)
    .add(`if`?.write() ?: NULL_CODEBLOCK)
    .add(")")
    .build()