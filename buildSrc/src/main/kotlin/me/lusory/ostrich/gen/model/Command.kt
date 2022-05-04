package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#commands
data class Command(
    override val name: String,
    val data: Either<Map<String, StructMember>, String> = mapOf<String, StructMember>() either null,
    val boxed: Boolean? = null,
    val returns: TypeRef? = null,
    val successResponse: Boolean = false,
    val gen: Boolean = false,
    val allowOob: Boolean = true,
    val allowPreconfig: Boolean = true,
    val coroutine: Boolean = true,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema
