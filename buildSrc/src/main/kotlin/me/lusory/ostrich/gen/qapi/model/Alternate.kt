package me.lusory.ostrich.gen.qapi.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#alternate-types
data class Alternate(
    override val name: String,
    val data: Map<String, Alternative>,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema

data class Alternative(
    val type: TypeRef,
    val `if`: Condition? = null
)