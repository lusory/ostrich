package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#struct-types
data class Struct(
    override val name: String,
    val data: Map<String, StructMember>,
    val base: String? = null,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema

data class StructMember(
    val type: TypeRef,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf()
)
