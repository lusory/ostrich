package me.lusory.ostrich.gen.cmd

import com.squareup.javapoet.*
import me.lusory.ostrich.gen.cmd.model.Stub
import me.lusory.ostrich.gen.qapi.GETTER
import me.lusory.ostrich.gen.qapi.SETTER
import me.lusory.ostrich.gen.qapi.NO_ARGS_CTOR
import me.lusory.ostrich.gen.qapi.TO_STRING
import me.lusory.ostrich.gen.qapi.EQUALS_AND_HASH_CODE
import java.io.File
import javax.lang.model.element.Modifier

val CMD_BUILDER: ClassName = ClassName.get("me.lusory.ostrich.cmd", "CmdBuilder")

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

        val desc: String = stub.params[2]
            .replace("[+ | -]", "") // unnecessary
            .replace(VARARG_REGEX) { result -> "${result.groupValues[1]}_vararg" }
            .replace(GROUP_VARARG_REGEX) { result -> "[VARARG ${result.groupValues[1]}]" }

        val args: List<Any> = parseBrackets(desc)
            .toMutableList().apply { removeAt(0) } // pop command name

        args.forEach { arg ->
            val optional: Boolean = arg is List<*>
            val isVararg: Boolean = (arg is List<*> && arg[0] == "VARARG") || (arg is String && arg.endsWith("_vararg"))

            // TODO: add fields
        }

        stubBuilder.build().save(type)
    }

    builder.build().save(className)
}
