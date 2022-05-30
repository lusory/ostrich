package me.lusory.ostrich.gen.qapi

import com.fasterxml.jackson.annotation.*
import com.squareup.javapoet.*
import me.lusory.ostrich.gen.qapi.model.*
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.time.Instant
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
val QEVENT: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QEvent")
val QCOMMAND: ClassName = ClassName.get("me.lusory.ostrich.qapi", "QCommand")
val EMPTY_STRUCT: ClassName = ClassName.get("me.lusory.ostrich.qapi", "EmptyStruct")
val CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "Condition")
val FEATURE: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "Feature")
val FEATURES: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "Features")
val UNION_CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "UnionCondition")
val UNION_FEATURES: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "UnionFeatures")
val UNION_BRANCH_CONDITION: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "UnionBranchCondition")
val COMMAND: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "Command")
val RAW_NAME: ClassName = ClassName.get("me.lusory.ostrich.qapi.metadata.annotations", "RawName")
val LIST: ClassName = ClassName.get(java.util.List::class.java)
val MAP: ClassName = ClassName.get(java.util.Map::class.java)
val ENUM: ClassName = ClassName.get(java.lang.Enum::class.java)
val STRING: ClassName = ClassName.get(java.lang.String::class.java)
val INSTANT: ClassName = ClassName.get(Instant::class.java)
val TRANSFORM_UTILS: ClassName = ClassName.get("me.lusory.ostrich.qapi.util", "TransformUtils")
val CLASS_WILDCARD: TypeName = ParameterizedTypeName.get(ClassName.get(java.lang.Class::class.java), TypeVariableName.get("?"))

val NOT_NULL: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "NotNull")).build()
val NULLABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Nullable")).build()
val UNMODIFIABLE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("org.jetbrains.annotations", "Unmodifiable")).build()
val JSON_VALUE: AnnotationSpec = AnnotationSpec.builder(JsonValue::class.java).build()
val JSON_CREATOR: AnnotationSpec = AnnotationSpec.builder(JsonCreator::class.java).build()
val JSON_IGNORE: AnnotationSpec = AnnotationSpec.builder(JsonIgnore::class.java).build()
val INCLUDE_ALWAYS: AnnotationSpec = AnnotationSpec.builder(JsonInclude::class.java).build()
val JSON_UNWRAPPED: AnnotationSpec = AnnotationSpec.builder(JsonUnwrapped::class.java).build()

val GETTER: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Getter")).build()
val SETTER: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Setter")).build()
val ALL_ARGS_CTOR: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor")).build()
val NO_ARGS_CTOR: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build()
val TO_STRING: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "ToString")).build()
val EQUALS_AND_HASH_CODE: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "EqualsAndHashCode")).build()
val DATA_OF: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok", "Data"))
    .addMember("staticConstructor", "\$S", "of")
    .build()
val ACCESSORS_PREFIX: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok.experimental", "Accessors"))
    .addMember("prefix", "\$S", "_")
    .build()
val UTILITY_CLASS: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok.experimental", "UtilityClass")).build()

val NULL_CODEBLOCK: CodeBlock = CodeBlock.of("null")

val TYPE_REF_REGEX = Regex("@([a-zA-Z0-9_-]+)")

private val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance()

fun String.formatJavadoc(): String = trim() // remove redundant surrounding whitespace
    .replace("\$", "\$\$") // escape dollar signs to not confuse javapoet
    .replace("<", "&lt;") // escape html
    .replace(">", "&gt;") // escape html
    .replace("\n", "<br>\n") // emphasize line breaks
    .replace(TYPE_REF_REGEX) { result -> "<i>${result.groupValues[1]}</i>" } // italicize type references

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

fun makeQapiWriterContext(sourceDir: File, schemas: List<SchemaFile>): QAPIWriterContext {
    val enums: List<String> = schemas.stream()
        .flatMap { file ->
            file.members.stream()
                .filter { it is Enum0 }
                .map { (it as Enum0).name }
        }
        .collect(Collectors.toList());

    return QAPIWriterContext(
        sourceDir,
        names = schemas.stream()
            .flatMap { file ->
                val newName: String = file.name.replaceReservedKeywords()

                file.members.stream()
                    .filter { it is NamedSchema }
                    .map { newName to (it as NamedSchema) }
            }
            .collect(Collectors.toMap({ it.second.name }, {
                val replacedName: String = it.second.name.replaceReservedKeywords().let { name ->
                    when (it.second) {
                        is Event -> name.toLowerCase().snakeToLowerCamelCase().capitalize() + "Event"
                        is Command -> name.snakeToLowerCamelCase().capitalize() + "Command"
                        else -> name
                    }
                }
                // mitigate naming conflicts
                if (has("java.lang.$replacedName")) {
                    "me.lusory.ostrich.qapi.${it.first}.${replacedName}0"
                } else {
                    "me.lusory.ostrich.qapi.${it.first}.$replacedName"
                }
            })),
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

data class QAPIWriterContext(
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
            .writeCondition(enum0.`if`)
            .writeFeatures(enum0.features)
            .writeRawName(enum0.name)
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
                    .writeCondition(value.`if`)
                    .writeFeatures(value.features)
                    // .addToString(value.name, JSON_VALUE)
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
            .writeCondition(struct.`if`)
            .writeFeatures(struct.features)
            .writeRawName(struct.name)
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
            .writeCondition(union.`if`, UNION_CONDITION)
            .writeFeatures(union.features, UNION_FEATURES)
            .writeRawName(union.name)
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
                .writeCondition(entry.value.`if`, UNION_BRANCH_CONDITION)
                .writeCondition(struct.`if`)
                .writeFeatures(struct.features)
                .writeRawName(struct.name)
                .writeStructMembers(struct.data)
                .addInitializerBlock(
                    CodeBlock.of("this.set${union.discriminator.skewerToLowerCamelCase().capitalize()}(\$T.${entry.key.toUpperCase().replaceReservedKeywords()});\n", discriminatorType)
                )

            tailrec fun writeInnerStruct(innerStruct: Struct) {
                // not writing the features and condition here, I find it unnecessary
                unionImplBuilder.writeStructMembers(innerStruct.data)

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
            .writeCondition(alternate.`if`)
            .writeFeatures(alternate.features)
            .writeRawName(alternate.name)

        if (alternate.docString != null) {
            builder.addJavadoc(alternate.docString.formatJavadoc())
        }

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
                .writeCondition(entry.value.`if`)
                .writeRawName(entry.key)
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

    fun writeEvent(event: Event) {
        val fullClassName: String = names[event.name] ?: error("Could not get class name for ${event.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val dataClassName: ClassName? = if (event.data.first != null) {
            if (event.data.first.isNotEmpty()) {
                ClassName.get(className.packageName(), className.simpleName(), "Data")
            } else {
                null
            }
        } else {
            val fullDataClassName: String = names[event.data.second] ?: error("Could not get class name for ${event.data.second}")

            ClassName.get(
                fullDataClassName.substringBeforeLast('.'),
                fullDataClassName.substringAfterLast('.')
            )
        }
        val builder: TypeSpec.Builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(QEVENT, dataClassName ?: EMPTY_STRUCT))
            .addAnnotation(GETTER)
            .addAnnotation(SETTER)
            .addAnnotation(NO_ARGS_CTOR)
            .addAnnotation(ALL_ARGS_CTOR)
            .addAnnotation(EQUALS_AND_HASH_CODE)
            .addAnnotation(TO_STRING)
            .writeCondition(event.`if`)
            .writeFeatures(event.features)
            .writeRawName(event.name)
            .addField(INSTANT, "timestamp", Modifier.PRIVATE)

        if (event.docString != null) {
            builder.addJavadoc(event.docString.formatJavadoc())
        }

        if (dataClassName != null) {
            builder.addField(dataClassName, "data", Modifier.PRIVATE)

            if (event.data.first != null) {
                builder
                    .addType(
                        TypeSpec.classBuilder("Data")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addSuperinterface(QSTRUCT)
                            .addAnnotation(GETTER)
                            .addAnnotation(SETTER)
                            .addAnnotation(NO_ARGS_CTOR)
                            .addAnnotation(EQUALS_AND_HASH_CODE)
                            .addAnnotation(TO_STRING)
                            .writeStructMembers(event.data.first)
                            .apply {
                                if (event.data.first.size != 0) {
                                    addAnnotation(ALL_ARGS_CTOR)
                                }
                            }
                            .build()
                    )
            }
        }

        builder.build().save(className)
    }

    fun writeEventsMeta(events: List<Event>) {
        val className: ClassName = ClassName.get("me.lusory.ostrich.qapi", "Events")

        TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(UTILITY_CLASS)
            .addField(
                FieldSpec.builder(ParameterizedTypeName.get(MAP, STRING, CLASS_WILDCARD), "TYPES", Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(UNMODIFIABLE)
                    .initializer(
                        CodeBlock.builder()
                            .add("\$T.unmodifiableMap(new \$T<\$T, \$T>() {\n", Collections::class.java, java.util.HashMap::class.java, STRING, CLASS_WILDCARD)
                            .indent()
                            .add("{\n")
                            .indent()
                            .apply {
                                events.forEach { event ->
                                    val fullEventClassName: String = names[event.name] ?: error("Could not get class name for ${event.name}")
                                    val eventClassName: ClassName = ClassName.get(
                                        fullEventClassName.substringBeforeLast('.'),
                                        fullEventClassName.substringAfterLast('.')
                                    )

                                    add("put(\$S, \$T.class);\n", event.name, eventClassName)
                                }
                            }
                            .unindent()
                            .add("}\n")
                            .unindent()
                            .add("})")
                            .build()
                    )
                    .build()
            )
            .build()
            .save(className)
    }

    fun writeCommand(command: Command) {
        val fullClassName: String = names[command.name] ?: error("Could not get class name for ${command.name}")
        val className: ClassName = ClassName.get(
            fullClassName.substringBeforeLast('.'),
            fullClassName.substringAfterLast('.')
        )
        val responseTypeName: TypeName = command.returns?.toTypeName() ?: EMPTY_STRUCT
        val dataClassName: ClassName? = if (command.data.first != null) {
            if (command.data.first.isNotEmpty()) {
                ClassName.get(className.packageName(), className.simpleName(), "Data")
            } else {
                null
            }
        } else {
            val fullDataClassName: String = names[command.data.second] ?: error("Could not get class name for ${command.data.second}")

            ClassName.get(
                fullDataClassName.substringBeforeLast('.'),
                fullDataClassName.substringAfterLast('.')
            )
        }
        val builder: TypeSpec.Builder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(QCOMMAND, dataClassName ?: EMPTY_STRUCT, responseTypeName.box()))
            .addAnnotation(NO_ARGS_CTOR)
            .addAnnotation(EQUALS_AND_HASH_CODE)
            .addAnnotation(TO_STRING)
            .addAnnotation(
                AnnotationSpec.builder(COMMAND)
                    .apply {
                        if (responseTypeName is ParameterizedTypeName) {
                            addMember("responseType", "\$T.class", responseTypeName.typeArguments[0])
                            addMember("respondsWithArray", "true")
                        } else {
                            addMember("responseType", "\$T.class", responseTypeName)
                        }
                        if (command.successResponse != null) {
                            addMember("successResponse", "\$L", command.successResponse)
                        }
                        if (command.allowOob != null) {
                            addMember("allowOob", "\$L", command.allowOob)
                        }
                        if (command.allowPreconfig != null) {
                            addMember("allowPreconfig", "\$L", command.allowPreconfig)
                        }
                        if (command.coroutine != null) {
                            addMember("coroutine", "\$L", command.coroutine)
                        }
                    }
                    .build()
            )
            .writeCondition(command.`if`)
            .writeFeatures(command.features)
            .writeRawName(command.name)

        if (command.docString != null) {
            builder.addJavadoc(command.docString.formatJavadoc())
        }

        if (dataClassName != null) {
            builder.addField(dataClassName, "data", Modifier.PRIVATE)
                .addAnnotation(GETTER)
                .addAnnotation(SETTER)
                .addAnnotation(ALL_ARGS_CTOR)

            if (command.data.first != null) {
                builder
                    .addType(
                        TypeSpec.classBuilder("Data")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addSuperinterface(QSTRUCT)
                            .addAnnotation(GETTER)
                            .addAnnotation(SETTER)
                            .addAnnotation(NO_ARGS_CTOR)
                            .addAnnotation(EQUALS_AND_HASH_CODE)
                            .addAnnotation(TO_STRING)
                            .writeStructMembers(command.data.first)
                            .apply {
                                if (command.gen == false) {
                                    addField(
                                        FieldSpec.builder(ParameterizedTypeName.get(MAP, STRING, TypeName.OBJECT), "extraProps", Modifier.PRIVATE)
                                            .addAnnotation(JSON_UNWRAPPED)
                                            .build()
                                    )
                                }
                                if (command.data.first.size != 0) {
                                    addAnnotation(ALL_ARGS_CTOR)
                                }
                            }
                            .build()
                    )
            }
        }

        builder.build().save(className)
    }

    fun TypeSpec.save(className: ClassName) {
        JavaFile.builder(className.packageName(), this)
            .addFileComment("This file was generated with ostrich. Do not edit, changes will be overwritten!")
            .skipJavaLangImports(true)
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
                        if (entry.value.`if` != null) {
                            addAnnotation(
                                entry.value.`if`!!.write(CONDITION)
                            )
                        }
                        if (entry.value.features.isNotEmpty()) {
                            addAnnotation(
                                entry.value.features.write(FEATURES)
                            )
                        }
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
        }
    }
}

fun TypeSpec.Builder.writeRawName(name: String): TypeSpec.Builder = addAnnotation(
    AnnotationSpec.builder(RAW_NAME)
        .addMember("value", "\$S", name)
        .build()
)

fun TypeSpec.Builder.writeCondition(condition: Condition?, klass: ClassName = CONDITION): TypeSpec.Builder = apply {
    if (condition != null) {
        addAnnotation(condition.write(klass))
    }
}

fun TypeSpec.Builder.writeFeatures(features: Collection<Feature>, klass: ClassName = FEATURES): TypeSpec.Builder = apply {
    if (features.isNotEmpty()) {
        addAnnotation(features.write(klass))
    }
}

fun Condition.compact(): String = when (type) {
    ConditionType.DEFAULT -> "default:$value"
    ConditionType.NOT -> "not:$value"
    ConditionType.ANY -> "any:[${conditions.map(Condition::compact).joinToString(",")}]"
    ConditionType.ALL -> "all:[${conditions.map(Condition::compact).joinToString(",")}]"
}

fun Condition.write(klass: ClassName): AnnotationSpec = AnnotationSpec.builder(klass)
    .addMember("value", "\$S", compact())
    .build()

fun Feature.write(klass: ClassName): AnnotationSpec = AnnotationSpec.builder(klass)
    .addMember("name", "\$S", name)
    .apply {
        if (`if` != null) {
            addMember("_if", "\$S", `if`.compact())
        }
    }
    .build()

fun Collection<Feature>.write(klass: ClassName): AnnotationSpec = AnnotationSpec.builder(klass)
    .addMember("value", "{ \$L }", stream().map { CodeBlock.of("\$L", it.write(FEATURE)) }.collect(CodeBlock.joining(", ")))
    .build()
