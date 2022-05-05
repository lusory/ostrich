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

fun TypeSpec.Builder.addToString(value: String, vararg annotations: AnnotationSpec): TypeSpec.Builder = apply {
    addMethod(
        MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(OVERRIDE)
            .apply { annotations.forEach { addAnnotation(it) } }
            .returns(String::class.java)
            .addStatement("return \$S", value)
            .build()
    )
}

fun CodeBlock.Builder.writeList(blocks: Collection<CodeBlock>): CodeBlock.Builder = apply {
    if (blocks.isEmpty()) {
        add("\$T.emptyList()", Collections::class.java)
    } else {
        add("\$T.asList(", Arrays::class.java)
        blocks.forEach { add(it) }
        add(")")
    }
}