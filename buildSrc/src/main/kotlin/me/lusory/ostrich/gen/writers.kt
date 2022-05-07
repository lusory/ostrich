package me.lusory.ostrich.gen

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
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
val QUNION: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QUnion")
val QALTERNATE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QAlternate")
val CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Condition")
val CONDITION_TYPE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "ConditionType")
val FEATURE: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Feature")
val LIST: ClassName = ClassName.get(java.util.List::class.java)
val ENUM: ClassName = ClassName.get(java.lang.Enum::class.java)
val TRANSFORM_UTILS: ClassName = ClassName.get("me.lusory.ostrich.qapi.util", "TransformUtils")
val CLASS_WILDCARD: TypeName = ParameterizedTypeName.get(ClassName.get(java.lang.Class::class.java), TypeVariableName.get("?"))

val NOT_NULL: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build()
val NULLABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build()
val UNMODIFIABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Unmodifiable")).build()
val JSON_VALUE: AnnotationSpec = AnnotationSpec.builder(JsonValue::class.java).build()
val JSON_CREATOR: AnnotationSpec = AnnotationSpec.builder(JsonCreator::class.java).build()
val JSON_IGNORE: AnnotationSpec = AnnotationSpec.builder(JsonIgnore::class.java).build()
val INCLUDE_ALWAYS: AnnotationSpec = AnnotationSpec.builder(JsonInclude::class.java).build()

val GETTER: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Getter")).build()
val SETTER: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Setter")).build()
val ALL_ARGS_CTOR: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor")).build()
val NO_ARGS_CTOR: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build()
val TO_STRING: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "ToString")).build()
val EQUALS_AND_HASH_CODE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "EqualsAndHashCode")).build()
val DATA: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Data")).build()
val DATA_OF: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Data"))
    .addMember("staticConstructor", "\$S", "of")
    .build()
val ACCESSORS_PREFIX: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok.experimental", "Accessors"))
    .addMember("prefix", "\$S", "_")
    .build()

val NULL_CODEBLOCK: CodeBlock = CodeBlock.of("null")

private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance()

fun String.replaceReservedKeywords(): String = skewerToSnakeCase().let { s ->
    if (s in RESERVED_KEYWORDS) {
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

fun makeWriterContext(sourceDir: File, schemas: List<SchemaFile>): WriterContext {
    val enums: List<String> = schemas.stream()
        .flatMap { file ->
            file.members.stream()
                .filter { it is Enum0 }
                .map { (it as Enum0).name }
        }
        .collect(Collectors.toList());

    return WriterContext(
        sourceDir,
        names = schemas.stream()
            .flatMap { file ->
                val newName: String = file.name.replaceReservedKeywords()

                file.members.stream()
                    .filter { it is NamedSchema }
                    .map { newName to (it as NamedSchema) }
            }
            .collect(Collectors.toMap({ it.second.name }, { "me.lusory.ostrich.qapi.${it.first}.${it.second.name.replaceReservedKeywords()}" })),
        structs = schemas.stream()
            .flatMap { file ->
                file.members.stream()
                    .filter { it is Struct }
                    .map { it as Struct }
            }
            .collect(Collectors.toMap({ it.name }, { it })),
        alternates = schemas.stream()
            .flatMap { file ->
                file.members.stream()
                    .filter { it is Alternate }
                    .map { (it as Alternate).name }
            }
            .collect(Collectors.toList()),
        structEnums = schemas.stream()
            .flatMap { file ->
                file.members.stream()
                    .filter { it is Struct }
                    .map { struct ->
                        struct as Struct

                        struct.name to struct.data.entries.stream()
                            .filter { field -> field.value.type.type.second?.let { it in enums } == true }
                            .collect(Collectors.toMap({ it.key }, { it.value.type.type.second!! }))
                    }
            }
            .collect(Collectors.toMap({ it.first }, { it.second }))
    )
}

data class WriterContext(
    val sourceDir: File,
    // short name - package name
    val names: Map<String, String>,
    val structs: Map<String, Struct>,
    val alternates: List<String>,
    val structEnums: Map<String, Map<String, String>>
) {
    fun writeEnum(enum0: Enum0) {
        val fullClassName: String = names[enum0.name] ?: error("Could not get class name for ${enum0.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val builder: TypeSpec.Builder = TypeSpec.enumBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(QENUM)
            .writeCondition(enum0.`if`, "IF", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .writeFeatures(enum0.features, "FEATURES", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addStringMethod("getRawName", enum0.name, isStatic = true)
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
            .addAnnotation(GETTER)
            .addAnnotation(SETTER)
            .addAnnotation(NO_ARGS_CTOR)
            .addAnnotation(EQUALS_AND_HASH_CODE)
            .addAnnotation(TO_STRING)
            .writeCondition(struct.`if`, "IF_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .writeFeatures(struct.features, "FEATURES_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addStringMethod("getRawName", struct.name, isStatic = true)
            .writeStructMembers(struct.data)

        if (struct.docString != null) {
            builder.addJavadoc(struct.docString.formatJavadoc())
        }

        if (struct.data.size != 0) {
            builder.addAnnotation(ALL_ARGS_CTOR)
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

    fun writeUnion(union: Union) {
        val fullClassName: String = names[union.name] ?: error("Could not get class name for ${union.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val builder: TypeSpec.Builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(QUNION)
            .addAnnotation(GETTER)
            .addAnnotation(SETTER)
            .addAnnotation(NO_ARGS_CTOR)
            .addAnnotation(EQUALS_AND_HASH_CODE)
            .addAnnotation(TO_STRING)
            .writeCondition(union.`if`, "IF_${union.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .writeFeatures(union.features, "FEATURES_${union.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addStringMethod("getRawName", union.name, isStatic = true)
            .addMethod(
                MethodSpec.methodBuilder("getDiscriminator")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(QENUM)
                    .addAnnotation(OVERRIDE)
                    .addAnnotation(JSON_IGNORE)
                    .addStatement("return this.get${union.discriminator.skewerToLowerCamelCase().capitalize()}()")
                    .build()
            )

        if (union.docString != null) {
            builder.addJavadoc(union.docString.formatJavadoc())
        }

        var discriminatorType0: String

        if (union.base.second != null) {
            val fullBaseName: String = names[union.base.second] ?: error("Could not get class name for ${union.base.second}")

            builder.superclass(ClassName.get(
                fullBaseName.substringBeforeLast('.'),
                fullBaseName.substringAfterLast('.')
            ))

            discriminatorType0 = structEnums[union.base.second]!![union.discriminator]!!.let { names[it] ?: error("Could not get class name for $it") }
        } else {
            builder.writeStructMembers(union.base.first!!)

            discriminatorType0 = union.base.first.entries.first { it.key == union.discriminator }.value.type.type.second!!.let { names[it] ?: error("Could not get class name for $it") }
        }

        val discriminatorType: TypeName = ClassName.get(
            discriminatorType0.substringBeforeLast('.'),
            discriminatorType0.substringAfterLast('.')
        )

        if (builder.fieldSpecs.size != 0) {
            builder.addAnnotation(ALL_ARGS_CTOR)
        }

        union.data.forEach { entry ->
            val struct: Struct = structs[entry.value.type.type.second ?: throw IllegalStateException()]  ?: error("Could not get struct ${entry.value.type.type.second}")

            val unionImplName: ClassName = ClassName.get(className.packageName(), "${className.simpleName()}${entry.key.skewerToLowerCamelCase().capitalize()}Branch")
            val unionImplBuilder: TypeSpec.Builder = TypeSpec.classBuilder(unionImplName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(className)
                .addAnnotation(GETTER)
                .addAnnotation(SETTER)
                .addAnnotation(ALL_ARGS_CTOR)
                .addAnnotation(NO_ARGS_CTOR)
                .addAnnotation(EQUALS_AND_HASH_CODE)
                .addAnnotation(TO_STRING)
                .writeCondition(entry.value.`if`, "IF_${union.name.toUpperCase()}_BRANCH_${entry.key.toUpperCase().skewerToSnakeCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .writeCondition(struct.`if`, "IF_${union.name.toUpperCase()}_STRUCT_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .writeFeatures(struct.features, "FEATURES_${union.name.toUpperCase()}_STRUCT_${struct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addStringMethod("getBaseName", struct.name, isStatic = true)
                .writeStructMembers(struct.data)
                .addInitializerBlock(
                    CodeBlock.of("this.set${union.discriminator.skewerToLowerCamelCase().capitalize()}(\$T.${entry.key.toUpperCase().replaceReservedKeywords()});\n", discriminatorType)
                )

            fun writeInnerStruct(innerStruct: Struct) {
                unionImplBuilder.writeCondition(innerStruct.`if`, "IF_${union.name.toUpperCase()}_STRUCT_${innerStruct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .writeFeatures(innerStruct.features, "FEATURES_${union.name.toUpperCase()}_STRUCT_${innerStruct.name.toUpperCase()}", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .writeStructMembers(innerStruct.data)

                if (innerStruct.base != null) {
                    writeInnerStruct(structs[innerStruct.base] ?: error("Could not get struct ${innerStruct.base}"))
                }
            }

            if (struct.base != null) {
                writeInnerStruct((structs[struct.base]  ?: error("Could not get struct ${struct.base}")).also { unionImplBuilder.addStringMethod("getStructName", it.name, isStatic = true) })
            }

            unionImplBuilder.build().save(unionImplName)
        }

        builder.build().save(className)
    }

    fun writeAlternate(alternate: Alternate) {
        val fullClassName: String = names[alternate.name] ?: error("Could not get class name for ${alternate.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val builder: TypeSpec.Builder = TypeSpec.interfaceBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(QALTERNATE)
            .writeCondition(alternate.`if`, "IF_${alternate.name.toUpperCase()}")
            .writeFeatures(alternate.features, "FEATURES_${alternate.name.toUpperCase()}")

        val types: MutableList<ClassName> = mutableListOf()

        alternate.data.forEach { entry ->
            if (entry.value.type.isNull()) {
                // skip null alternate types
                return@forEach
            }

            val alternateImplName: ClassName = ClassName.get(className.packageName(), "${className.simpleName()}${entry.key.skewerToLowerCamelCase().capitalize()}")

            types.add(alternateImplName)

            TypeSpec.classBuilder(alternateImplName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(className)
                .addAnnotation(DATA_OF)
                .writeCondition(entry.value.`if`, "IF_VALUE_${entry.key.skewerToSnakeCase().toUpperCase()}")
                .addField(entry.value.type.toTypeName(), "value", Modifier.PRIVATE)
                .build()
                .save(alternateImplName)
        }

        builder.addField(
            FieldSpec.builder(ArrayTypeName.of(CLASS_WILDCARD), "TYPES", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(
                    CodeBlock.builder()
                        .add("new \$T[] { ", CLASS_WILDCARD)
                        .add(CodeBlock.join(types.map { CodeBlock.of("\$T.class", it) }, ", "))
                        .add(" }")
                        .build()
                )
                .build()
        )

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

        return if (isArray) ParameterizedTypeName.get(LIST, type.box()) else type
    }

    fun TypeSpec.Builder.writeStructMembers(data: Map<String, StructMember>): TypeSpec.Builder = apply {
        data.forEach { entry ->
            val isOptional: Boolean = entry.key.startsWith('*')
            val droppedName: String = if (isOptional) entry.key.drop(1) else entry.key
            val sanitizedName: String = droppedName.skewerToLowerCamelCase().replaceReservedKeywords()
            val type: TypeName = entry.value.type.toTypeName()
            addField(
                FieldSpec.builder(type.let { if (isOptional) it.box() else it }, sanitizedName, Modifier.PRIVATE)
                    .apply {
                        if (isOptional) {
                            initializer("null")
                            addAnnotation(NULLABLE)
                        } else if (!type.isPrimitive) {
                            addAnnotation(NOT_NULL)
                        }
                        if (entry.value.type.type.second in alternates) {
                            addAnnotation(INCLUDE_ALWAYS)
                        }
                        if (sanitizedName != droppedName) {
                            addAnnotation(
                                AnnotationSpec.builder(JsonProperty::class.java)
                                    .addMember("value", "\$S", droppedName)
                                    .build()
                            )
                        }
                        if (sanitizedName.startsWith('_') && droppedName != "class") { // fix for PciDeviceClass
                            addAnnotation(ACCESSORS_PREFIX)
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
