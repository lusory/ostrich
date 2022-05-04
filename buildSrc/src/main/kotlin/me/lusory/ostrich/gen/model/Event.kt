package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#events
data class Event(
    override val name: String,
    val data: Either<Map<String, StructMember>, String> = mapOf<String, StructMember>() either null,
    val boxed: Boolean? = null,
    val `if`: Condition? = null,
    val features: List<Feature> = listOf(),
    override val docString: String? = null
) : NamedSchema
