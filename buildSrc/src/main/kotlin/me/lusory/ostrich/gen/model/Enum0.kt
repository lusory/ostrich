package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#enumeration-types
data class Enum0 (
    override val name: String,
    val data: List<EnumValue>,
    val prefix: String? = null,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf()
) : NamedSchema

data class EnumValue(
    val name: String,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf()
)