package me.lusory.ostrich.gen.cmd

import com.squareup.javapoet.*
import me.lusory.ostrich.gen.cmd.model.Stub
import me.lusory.ostrich.gen.qapi.*
import java.io.File
import javax.lang.model.element.Modifier

val CMD_BUILDER: ClassName = ClassName.get("me.lusory.ostrich.cmd", "CmdBuilder")

val ACCESS_LEVEL: ClassName = ClassName.get("lombok", "AccessLevel")
val SETTER_NONE: AnnotationSpec = AnnotationSpec.builder(SETTER.type as ClassName)
    .addMember("value", "\$T.NONE", ACCESS_LEVEL)
    .build()
val ACCESSORS_FLUENT_CHAIN: AnnotationSpec = AnnotationSpec.builder(ClassName.get("lombok.experimental", "Accessors"))
    .addMember("fluent", "true")
    .addMember("chain", "true")
    .build()

val CODE_REGEX: Regex = Regex("``([^`\n]+)``")
val ITALIC_REGEX: Regex = Regex("\\*([^*\n]+)\\*")
val VARARG_REGEX: Regex = Regex("([a-zA-Z0-9-_]+) \\[\\12 \\[...\\]\\]")
val GROUP_VARARG_REGEX: Regex = Regex("\\((.+)\\)\\.\\.\\.")

fun String.formatRst(): String = trim() // remove redundant surrounding whitespace
    .replace("\$", "\$\$") // escape dollar signs to not confuse javapoet
    .replace("<", "&lt;") // escape html
    .replace(">", "&gt;") // escape html
    .replace("\n", "<br>\n") // emphasize line breaks
    .replace(CODE_REGEX) { result -> "<code>${result.groupValues[1]}</code>" } // code block markup
    .replace(ITALIC_REGEX) { result -> "<i>${result.groupValues[1]}</i>" } // italic markup

fun <E> List<E>.split(delimiterElem: E): List<List<E>> {
    val result: MutableList<List<E>> = mutableListOf()
    var list: MutableList<E> = mutableListOf()

    forEach { elem ->
        if (elem == delimiterElem) {
            result.add(list)
            list = mutableListOf()
        } else {
            list.add(elem)
        }
    }
    result.add(list)

    return result
}

fun writeQemuImg(sourceDir: File, file: File, docFile: File) {
    val stubs: List<Stub> = stitchDocs(parseStubs(file), docFile, matchFully = false)

    val className: ClassName = ClassName.get("me.lusory.ostrich.cmd", "ImageBuilder")
    val builder: TypeSpec.Builder = TypeSpec.interfaceBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(CMD_BUILDER)

    fun TypeSpec.save(type: ClassName) {
        JavaFile.builder(type.packageName(), this)
            .addFileComment("This file was generated with ostrich. Do not edit, changes will be overwritten!")
            .skipJavaLangImports(true)
            .indent("    ") // 4 space indent
            .build()
            .writeTo(sourceDir)
    }

    stubs.forEach { stub ->
        val type: ClassName = ClassName.get("me.lusory.ostrich.cmd", "${stub.params[0].capitalize()}ImageBuilder")
        val doc: String = (stub.rst + "\n\n``${stub.params[2]}``").formatRst()

        builder.addMethod(
            MethodSpec.methodBuilder(stub.params[0])
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(type)
                .addJavadoc(doc)
                .addStatement("return new \$T()", type)
                .build()
        )
        val stubBuilder: TypeSpec.Builder = TypeSpec.classBuilder(type)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(className)
            .addAnnotation(GETTER)
            .addAnnotation(SETTER)
            .addAnnotation(NO_ARGS_CTOR)
            .addAnnotation(TO_STRING)
            .addAnnotation(EQUALS_AND_HASH_CODE)
            .addAnnotation(ACCESSORS_FLUENT_CHAIN)
            .addJavadoc(doc)

        val buildMethodBuilder: MethodSpec.Builder = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(ArrayTypeName.of(java.lang.String::class.java))
            .addAnnotation(OVERRIDE)
            .addStatement("final \$T<String> result = new \$T<>()", java.util.List::class.java, java.util.ArrayList::class.java)
            .addStatement("result.add(\$S)", stub.params[0])

        val desc: String = stub.params[2]
            .replace("[+ | -]", "") // unnecessary
            .replace(VARARG_REGEX) { result -> "${result.groupValues[1]}_vararg" }
            .replace(GROUP_VARARG_REGEX) { result -> "[VARARG ${result.groupValues[1]}]" }

        val args: List<Any> = parseBrackets(desc).toMutableList().apply { removeFirst() } // pop command name

        fun addNewRepeatableOption(argName: String, name: String) {
            stubBuilder
                .addField(
                    FieldSpec.builder(TypeName.INT, name, Modifier.PRIVATE)
                        .initializer("0")
                        .addAnnotation(SETTER_NONE)
                        .build()
                )
                .addMethod(
                    MethodSpec.methodBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(type)
                        .addStatement("this.$name++")
                        .addStatement("return this")
                        .build()
                )

            buildMethodBuilder.beginControlFlow("for (int i = 0; i < this.$name; i++)")
                .addStatement("result.add(\$S)", argName)
                .endControlFlow()
        }

        fun addNewRepeatableOptionWithArg(argName: String, name: String, elem: String) {
            stubBuilder
                .addField(
                    FieldSpec.builder(ParameterizedTypeName.get(LIST, STRING), name, Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new \$T<>()", java.util.ArrayList::class.java)
                        .build()
                )
                .addMethod(
                    MethodSpec.methodBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(type)
                        .addParameter(java.lang.String::class.java, elem)
                        .addStatement("this.$name.add($elem)")
                        .addStatement("return this")
                        .build()
                )

            buildMethodBuilder.beginControlFlow("for (final String value : this.$name)")
                .addStatement("result.add(\$S)", argName)
                .addStatement("result.add(value)")
                .endControlFlow()
        }

        fun addNewOption(argName: String, name: String) {
            stubBuilder
                .addField(
                    FieldSpec.builder(TypeName.BOOLEAN, name, Modifier.PRIVATE)
                        .initializer("false")
                        .build()
                )
                .addMethod(
                    MethodSpec.methodBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(type)
                        .addStatement("this.$name = true")
                        .addStatement("return this")
                        .build()
                )

            buildMethodBuilder.beginControlFlow("if (this.$name)")
                .addStatement("result.add(\$S)", argName)
                .endControlFlow()
        }

        fun addNewOptionWithArg(argName: String, name: String, arg: String? = null, optional: Boolean = false, joined: Boolean = false) {
            stubBuilder
                .addField(
                    FieldSpec.builder(java.lang.String::class.java, name, Modifier.PRIVATE)
                        .apply {
                            if (optional) {
                                initializer("null")
                                addAnnotation(NULLABLE)
                            } else {
                                addAnnotation(NOT_NULL)
                            }
                            if (arg != null) {
                                addJavadoc(arg)
                            }
                        }
                        .build()
                )

            if (optional) {
                buildMethodBuilder.beginControlFlow("if (this.$name != null)")
            }

            if (joined) {
                buildMethodBuilder.addStatement("result.add(\$S + this.$name)", "$argName=")
            } else {
                buildMethodBuilder.addStatement("result.add(\$S)", argName)
                    .addStatement("result.add(this.$name)")
            }

            if (optional) {
                buildMethodBuilder.endControlFlow()
            }
        }

        fun addPlainArg(name: String, optional: Boolean = false) {
            stubBuilder
                .addField(
                    FieldSpec.builder(java.lang.String::class.java, name, Modifier.PRIVATE)
                        .apply {
                            if (optional) {
                                initializer("null")
                                addAnnotation(NULLABLE)
                            } else {
                                addAnnotation(NOT_NULL)
                            }
                        }
                        .build()
                )

            if (optional) {
                buildMethodBuilder.beginControlFlow("if (this.$name != null)")
            }

            buildMethodBuilder.addStatement("result.add(this.$name)")

            if (optional) {
                buildMethodBuilder.endControlFlow()
            }
        }

        fun addVarargPlainArg(name: String) {
            stubBuilder
                .addField(
                    FieldSpec.builder(ParameterizedTypeName.get(LIST, STRING), name, Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("new \$T<>()", java.util.ArrayList::class.java)
                        .build()
                )
                .addMethod(
                    MethodSpec.methodBuilder(name)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(type)
                        .addParameter(java.lang.String::class.java, name)
                        .addStatement("this.$name.add($name)")
                        .addStatement("return this")
                        .build()
                )

            buildMethodBuilder.beginControlFlow("for (final String value : this.$name)")
                .addStatement("result.add(value)")
                .endControlFlow()
        }

        var skipNext = false
        args.forEachIndexed { i, arg ->
            if (skipNext) {
                skipNext = false
                return@forEachIndexed
            }

            if (arg is List<*>) { // optional or vararg
                if (arg[0] == "VARARG") { // vararg
                    // pop vararg id
                    val split: List<List<*>> = arg.toMutableList().apply { removeFirst() }.split("|")

                    split.forEach { list ->
                        val argName: String = list[0] as String
                        val name: String = (if (argName.startsWith("--")) argName.drop(2) else argName.drop(1)).skewerToLowerCamelCase().replaceReservedKeywords()

                        if (list.size == 1) {
                            addNewRepeatableOption(argName, name)
                        } else {
                            val elem: String = (list[1] as String).toLowerCase().skewerToLowerCamelCase()

                            addNewRepeatableOptionWithArg(argName, name, elem)
                        }
                    }
                } else { // optional
                    fun parseArgs(arg1: List<*>) {
                        val argElem: String = arg1[0] as String

                        if (arg1.size == 1) {
                            if ('=' in argElem) {
                                val split: List<String> = argElem.split('=', limit = 2)
                                val argName: String = split[0]
                                val name: String = (if (argName.startsWith('-')) (if (argName.startsWith("--")) argName.drop(2) else argName.drop(1)) else argName).skewerToLowerCamelCase().replaceReservedKeywords()

                                addNewOptionWithArg(argName, name, arg = split[1], optional = true, joined = true)
                            } else {
                                if (argElem.startsWith('-')) {
                                    val name: String = (if (argElem.startsWith("--")) argElem.drop(2) else argElem.drop(1)).skewerToLowerCamelCase().replaceReservedKeywords()

                                    addNewOptionWithArg(argElem, name, optional = true)
                                } else {
                                    addPlainArg(argElem.skewerToLowerCamelCase().replaceReservedKeywords(), optional = true)
                                }
                            }
                        } else if (arg1.size == 2) {
                            val name: String = (if (argElem.startsWith("--")) argElem.drop(2) else argElem.drop(1)).skewerToLowerCamelCase().replaceReservedKeywords()

                            addNewOptionWithArg(argElem, name, arg = arg1[1] as? String, optional = true)
                        } else {
                            splitArgs(arg1).forEach { parseArgs(it) }
                        }
                    }

                    parseArgs(arg.filter { it != "|" })
                }
            } else if (arg is String) {
                if ('=' in arg) {
                    val split: List<String> = arg.split('=', limit = 2)
                    val argName: String = split[0]
                    val name: String = (if (argName.startsWith('-')) (if (argName.startsWith("--")) argName.drop(2) else argName.drop(1)) else argName).skewerToLowerCamelCase().replaceReservedKeywords()

                    addNewOptionWithArg(argName, name, arg = split[1], joined = true)
                } else {
                    if (arg.startsWith('-')) {
                        val name: String = (if (arg.startsWith("--")) arg.drop(2) else arg.drop(1)).skewerToLowerCamelCase().replaceReservedKeywords()

                        val nextItem: String? = args[i + 1] as? String
                        if (nextItem?.startsWith('-') == true || nextItem?.contains('=') == true) { // is the next arg an option?
                            addNewOption(arg, name)
                        } else {
                            addNewOptionWithArg(arg, name, arg = nextItem)
                            skipNext = true
                        }
                    } else {
                        if (arg.endsWith("_vararg")) {
                            addVarargPlainArg(arg.replaceFirst("_vararg", "").skewerToLowerCamelCase().replaceReservedKeywords())
                        } else {
                            addPlainArg(arg.skewerToLowerCamelCase())
                        }
                    }
                }
            } else {
                error("Invalid argument type ${arg.javaClass.name}")
            }
        }

        stubBuilder
            .addMethod(
                buildMethodBuilder.addStatement("return result.toArray(new String[0])")
                    .build()
            )
            .build()
            .save(type)
    }

    builder.build()
        .save(className)
}

fun writeQemuSystem(sourceDir: File, file: File) {
    val stubs: List<Stub> = parseStubs(file)

    stubs.forEach { stub ->
        val optionName: String = stub.params[0]
        val hasArg: Boolean = stub.params[1] == "HAS_ARG"

        println("Option $optionName, has arg: $hasArg")
    }
}
