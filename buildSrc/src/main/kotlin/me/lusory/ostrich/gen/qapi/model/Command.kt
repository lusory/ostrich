package me.lusory.ostrich.gen.qapi.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#commands
data class Command(
    override val name: String,
    val data: Either<Map<String, StructMember>, String> = mapOf<String, StructMember>() either null,
    val boxed: Boolean? = null,
    val returns: TypeRef? = null,
    val successResponse: Boolean? = null,
    val gen: Boolean? = null,
    val allowOob: Boolean? = null,
    val allowPreconfig: Boolean? = null,
    val coroutine: Boolean? = null,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema
