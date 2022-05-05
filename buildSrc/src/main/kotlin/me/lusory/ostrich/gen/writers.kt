package me.lusory.ostrich.gen

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.squareup.javapoet.*
import me.lusory.ostrich.gen.model.*
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
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
val QSTRUCT: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QStruct")
val CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Condition")
val CONDITION_TYPE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "ConditionType")
val FEATURE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Feature")
val LIST: ClassName = ClassName.get(java.util.List::class.java)
val TRANSFORM_UTILS: ClassName = ClassName.get("me.lusory.ostrich.qapi.util", "TransformUtils")

val NULLABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build()
val UNMODIFIABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Unmodifiable")).build()
val JSON_VALUE: AnnotationSpec = AnnotationSpec.builder(JsonValue::class.java).build()
val JSON_CREATOR: AnnotationSpec = AnnotationSpec.builder(JsonCreator::class.java).build()

val DATA_OF: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Data"))
    .addMember("staticConstructor", "\$S", "of")
    .build()
val ACCESSORS_PREFIX: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok.experimental", "Accessors"))
    .addMember("prefix", "{\$S}", "_")
    .build()

val NULL_CODEBLOCK: CodeBlock = CodeBlock.of("null")

private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance()

fun String.replaceReservedKeywords(): String = skewerToSnakeCase().let { s ->
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

fun String.formatJavadoc(): String {
    var replaced: String = replace("\n@", "\n").replace("\$", "\$\$")

    if (replaced.startsWith('@')) {
        replaced = replaced.drop(1)
    }
    return replaced
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
            builder.addJavadoc(enum0.docString.formatJavadoc())
        }

        enum0.data.forEach { value ->
            builder.addEnumConstant(
                value.name.toUpperCase().replaceReservedKeywords(),
                TypeSpec.anonymousClassBuilder("")
                    .writeCondition(value.`if`, "if_", Modifier.PRIVATE, Modifier.FINAL)
                    .apply {
                        if (value.`if` != null) {
                            addGetter("getIf", "if_", CONDITION, override_ = true)
                        }
                    }
                    .writeFeatures(value.features, "features", Modifier.PRIVATE, Modifier.FINAL)
                    .apply {
                        if (value.features.isNotEmpty()) {
                            addGetter("getFeatures", "features", ParameterizedTypeName.get(LIST, FEATURE), override_ = true)
                        }
                    }
                    .addToString(value.name, JSON_VALUE)
                    .build()
            )
        }

        builder.build().save(className)
    }

    fun writeStruct(struct: Struct) {
        val fullClassName: String = names[struct.name] ?: error("Could not get class name for ${struct.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val builder: TypeSpec.Builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(QSTRUCT)
            .addAnnotation(DATA_OF)
            .writeCondition(struct.`if`, "IF_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .writeFeatures(struct.features, "FEATURES_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addStringMethod("getName", struct.name, OVERRIDE)
            .writeStructMembers(struct.data)

        if (struct.docString != null) {
            builder.addJavadoc(struct.docString.formatJavadoc())
        }

        if (struct.base != null) {
            val fullBaseName: String = names[struct.base] ?: error("Could not get class name for ${struct.base}")

            builder.superclass(ClassName.get(
                fullBaseName.substringBeforeLast('.'),
                fullBaseName.substringAfterLast('.')
            ))
        }

        builder.build().save(className)
    }

    fun TypeSpec.save(className: ClassName) {
        JavaFile.builder(className.packageName(), this)
            .addFileComment("This file was generated with ostrich. Do not edit, changes will be overwritten!")
            // commented out due to me.lusory.ostrich.qapi.common.String
            // .skipJavaLangImports(true)
            .indent("    ") // 4 space indent
            .build()
            .writeTo(sourceDir)
    }

    fun TypeRef.toTypeName(): TypeName {
        var type: TypeName
        if (this.type.first != null) {
            type = when (this.type.first) {
                Double::class -> TypeName.DOUBLE
                Byte::class -> TypeName.BYTE
                Short::class -> TypeName.SHORT
                Int::class -> TypeName.INT
                Long::class -> TypeName.LONG
                Boolean::class -> TypeName.BOOLEAN
                Any::class -> TypeName.OBJECT
                else -> ClassName.get(this.type.first.java)
            }
        } else {
            val fullClassName: String = names[this.type.second] ?: error("Could not get class name for ${this.type.second}")

            type = ClassName.get(
                fullClassName.substringBeforeLast('.'),
                fullClassName.substringAfterLast('.')
            )
        }

        return if (isArray) ArrayTypeName.of(type) else type
    }

    fun TypeSpec.Builder.writeStructMembers(data: Map<String, StructMember>): TypeSpec.Builder = apply {
        addAnnotation(ACCESSORS_PREFIX)
        data.forEach { entry ->
            val isOptional: Boolean = entry.key.startsWith('*')
            val sanitizedName: String = (if (isOptional) entry.key.drop(1) else entry.key).skewerToLowerCamelCase().replaceReservedKeywords()
            addField(
                FieldSpec.builder(entry.value.type.toTypeName().let { if (isOptional) it.box() else it }, sanitizedName, Modifier.PRIVATE)
                    .apply {
                        if (isOptional) {
                            initializer("null")
                            addAnnotation(NULLABLE)
                        }
                        if (sanitizedName != entry.key) {
                            addAnnotation(
                                AnnotationSpec.builder(JsonProperty::class.java)
                                    .addMember("value", "\$S", if (isOptional) entry.key.drop(1) else entry.key)
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            writeCondition(entry.value.`if`, "IF_MEMBER_${sanitizedName.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            writeFeatures(entry.value.features, "FEATURES_MEMBER_${sanitizedName.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        }
    }
}

fun TypeSpec.Builder.writeCondition(condition: Condition?, fieldName: String, vararg modifiers: Modifier): TypeSpec.Builder = apply {
    if (condition != null) {
        addField(
            FieldSpec.builder(CONDITION, fieldName, *modifiers)
                .addAnnotation(NULLABLE)
                .initializer(condition.write())
                .build()
        )
    }
}

fun TypeSpec.Builder.writeFeatures(features: List<Feature>, fieldName: String, vararg modifiers: Modifier): TypeSpec.Builder = apply {
    if (features.isNotEmpty()) {
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
