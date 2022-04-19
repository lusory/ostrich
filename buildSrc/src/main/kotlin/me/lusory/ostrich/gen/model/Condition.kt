package me.lusory.ostrich.gen.model

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#configuring-the-schema
data class Condition(
    val type: ConditionType = ConditionType.DEFAULT,
    // not empty if type is ALL or ANY
    val conditions: List<Condition> = listOf(),
    // null if type is ALL or ANY
    val value: String?
)

enum class ConditionType {
    DEFAULT,
    ALL,
    ANY,
    NOT
}
