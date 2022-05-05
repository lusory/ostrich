package me.lusory.ostrich.gen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.Arrays
import java.util.Collections
import javax.lang.model.element.Modifier

val OVERRIDE: AnnotationSpec = AnnotationSpec.builder(Override::class.java).build()

fun TypeSpec.Builder.addGetter(getterName: String, fieldName: String, fieldType: TypeName, override_: Boolean = false): TypeSpec.Builder = apply {
    addMethod(
        MethodSpec.methodBuilder(getterName)
            .addModifiers(Modifier.PUBLIC)
            .returns(fieldType)
            .addStatement("return this.$fieldName")
            .apply {
                if (override_) {
                    addAnnotation(OVERRIDE)
                }
            }
            .build()
    )
}

fun TypeSpec.Builder.addOneliner(name: String, returnType: TypeName, format: String, vararg args: Any, override_: Boolean = false): TypeSpec.Builder = apply {
    addMethod(
        MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .addStatement("return $format", *args)
            .apply {
                if (override_) {
                    addAnnotation(OVERRIDE)
                }
            }
            .build()
    )
}

fun TypeSpec.Builder.addToString(value: String, vararg annotations: AnnotationSpec): TypeSpec.Builder = apply {
    addStringMethod("toString", value, OVERRIDE, *annotations)
}

fun TypeSpec.Builder.addStringMethod(name: String, value: String, vararg annotations: AnnotationSpec): TypeSpec.Builder = apply {
    addMethod(
        MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC)
            .apply { annotations.forEach { addAnnotation(it) } }
            .returns(java.lang.String::class.java)
            .addStatement("return \$S", value)
            .build()
    )
}

fun CodeBlock.Builder.writeList(blocks: Collection<CodeBlock>): CodeBlock.Builder = apply {
    if (blocks.isEmpty()) {
        add("\$T.emptyList()", Collections::class.java)
    } else {
        if (blocks.size == 1) {
            add("\$T.singletonList(", Collections::class.java)
        } else {
            add("\$T.asList(", Arrays::class.java)
        }
        blocks.forEach { add(it) }
        add(")")
    }
}

// https://stackoverflow.com/questions/60010298/how-can-i-convert-a-camel-case-string-to-snake-case-and-back-in-idiomatic-kotlin

val camelRegex: Regex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex: Regex = "_[a-zA-Z]".toRegex()

fun String.camelToSnakeCase(): String = camelRegex.replace(this) { "_${it.value}" }.toLowerCase()

fun String.snakeToLowerCamelCase(): String =
    snakeRegex.replace(this) { it.value.replace("_","").toUpperCase() }

fun String.skewerToSnakeCase(): String = replace('-', '_')

fun String.snakeToSkewerCase(): String = replace('_', '-')

fun String.skewerToLowerCamelCase(): String = skewerToSnakeCase().snakeToLowerCamelCase()