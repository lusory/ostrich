package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#union-types
data class Union(
    override val name: String,
    val base: Either<Map<String, StructMember>, String>,
    val discriminator: String,
    val data: Map<String, UnionBranch>,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema

data class UnionBranch(
    val type: TypeRef,
    val `if`: Condition? = null
)