package me.lusory.ostrich.gen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

val OVERRIDE: AnnotationSpec = AnnotationSpec.builder(Override::class.java).build()

fun TypeSpec.Builder.addGetter(getterName: String, fieldName: String, fieldType: TypeName, override_: Boolean = false): TypeSpec.Builder {
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
    return this
}