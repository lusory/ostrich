package me.lusory.ostrich.gen.model

import kotlin.reflect.KClass

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#type-references-and-array-types
data class TypeRef(
    val type: Either<KClass<*>, String>,
    val isArray: Boolean = false
) {
    fun isNull(): Boolean = type.first == Unit::class
}
