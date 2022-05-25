package me.lusory.ostrich.gen.qapi.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#include-directives
data class IncludeDirective(val file: String, override val docString: String? = null) : Schema

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#pragma-directives
data class PragmaDirective(
    val docRequired: Boolean? = null,
    val commandNameExceptions: List<String> = listOf(),
    val commandReturnsExceptions: List<String> = listOf(),
    val memberNameExceptions: List<String> = listOf(),
    override val docString: String? = null
) : Schema