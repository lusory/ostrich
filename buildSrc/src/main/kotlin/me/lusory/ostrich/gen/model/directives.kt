package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#include-directives
data class IncludeDirective(val file: String) : Schema

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#pragma-directives
data class PragmaDirective(
    val docRequired: Boolean? = null,
    val commandNameExceptions: List<String> = listOf(),
    val commandReturnsExceptions: List<String> = listOf(),
    val memberNameExceptions: List<String> = listOf()
) : Schema