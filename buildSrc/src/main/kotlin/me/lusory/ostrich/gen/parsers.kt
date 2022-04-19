package me.lusory.ostrich.gen

import me.lusory.ostrich.gen.model.Enum0
import me.lusory.ostrich.gen.model.Feature
import me.lusory.ostrich.gen.model.PragmaDirective
import me.lusory.ostrich.gen.model.IncludeDirective
import me.lusory.ostrich.gen.model.Condition
import com.fasterxml.jackson.databind.JsonNode
import me.lusory.ostrich.gen.model.ConditionType
import kotlin.reflect.KClass

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#schema-syntax
fun parseSchemaType(node: JsonNode): SchemaType = when {
    node.has("command") -> SchemaType.COMMAND
    node.has("struct") -> SchemaType.STRUCT
    node.has("enum") -> SchemaType.ENUM
    node.has("union") -> SchemaType.UNION
    node.has("alternate") -> SchemaType.ALTERNATE
    node.has("event") -> SchemaType.EVENT
    node.has("include") -> SchemaType.INCLUDE
    node.has("pragma") -> SchemaType.PRAGMA
    else -> throw IllegalArgumentException("Unknown schema type")
}

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#built-in-types
fun parseType(type: String): KClass<*> = when (type) {
    "str" -> String::class
    "number" -> Double::class
    "int8" -> Byte::class
    "int16" -> Short::class
    "int32" -> Int::class
    "int64" -> Long::class
    "uint8" -> Short::class
    "uint16" -> Int::class
    "uint32" -> Long::class
    "uint64" -> Long::class
    "size" -> Long::class
    "bool" -> Boolean::class
    "null" -> Unit::class
    "any" -> Any::class
    // QType left out
    else -> throw IllegalArgumentException("Unknown type $type")
}

fun parseConditionType(node: JsonNode): ConditionType = when {
    node.has("all") -> ConditionType.ALL
    node.has("any") -> ConditionType.ANY
    node.has("not") -> ConditionType.NOT
    node.isTextual -> ConditionType.DEFAULT
    else -> throw IllegalArgumentException("Unknown condition type $node")
}

fun parseCondition(node: JsonNode): Condition? = node.get("if")?.let { conditionNode ->
    if (conditionNode.isTextual) {
        return@let Condition(value = conditionNode.asText())
    }
    val conditionType: ConditionType = parseConditionType(conditionNode)
    return@let Condition(
        type = conditionType,
        conditions = if (conditionType != ConditionType.NOT) conditionNode.get(conditionType.name.toLowerCase()).map { parseCondition(it)!! } else listOf(),
        value = conditionNode.get("not")?.asText()
    )
}

fun parseIncludeDirective(node: JsonNode): IncludeDirective? = node.get("include")?.let { includeNode ->
    IncludeDirective(includeNode.asText())
}

fun parsePragmaDirective(node: JsonNode): PragmaDirective? = node.get("pragma")?.let { pragmaNode ->
    PragmaDirective(
        docRequired = pragmaNode.get("doc-required")?.asBoolean(),
        commandReturnsExceptions = pragmaNode.get("command-returns-exceptions")?.map { it.asText() } ?: listOf(),
        commandNameExceptions = pragmaNode.get("command-name-exceptions")?.map { it.asText() } ?: listOf(),
        memberNameExceptions = pragmaNode.get("member-name-exceptions")?.map { it.asText() } ?: listOf()
    )
}

fun parseFeature(node: JsonNode): Feature = when {
    node.isTextual -> Feature(name = node.asText())
    node.has("name") -> Feature(
        name = node.get("name").asText(),
        `if` = parseCondition(node)
    )
    else -> throw IllegalArgumentException("Malformed feature")
}

fun parseFeatures(node: JsonNode): List<Feature> = node.get("features")?.map { parseFeature(it) } ?: listOf()

fun parseEnum(node: JsonNode): Enum0? = Enum0(
    name = node.get("enum")?.asText() ?: throw IllegalArgumentException("Malformed enum"),
    data = listOf() // TODO: finish this
)