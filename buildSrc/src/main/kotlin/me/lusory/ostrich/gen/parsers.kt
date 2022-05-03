package me.lusory.ostrich.gen

import com.fasterxml.jackson.databind.JsonNode
import me.lusory.ostrich.gen.model.*
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

fun parseTypeRef(node: JsonNode): TypeRef = when {
    node.isArray -> TypeRef(parseType(node[0].asText()), true)
    node.isTextual -> TypeRef(parseType(node.asText()), false)
    else -> throw IllegalArgumentException("Invalid type ref form $node")
}

fun parseConditionType(node: JsonNode): ConditionType = when {
    node.has("all") -> ConditionType.ALL
    node.has("any") -> ConditionType.ANY
    node.has("not") -> ConditionType.NOT
    node.isTextual -> ConditionType.DEFAULT
    else -> throw IllegalArgumentException("Unknown condition type $node")
}

fun parseCondition(node: JsonNode): Condition? = node["if"]?.let { conditionNode ->
    if (conditionNode.isTextual) {
        return@let Condition(value = conditionNode.asText())
    }
    val conditionType: ConditionType = parseConditionType(conditionNode)
    return@let Condition(
        type = conditionType,
        conditions = if (conditionType != ConditionType.NOT) conditionNode[conditionType.name.toLowerCase()].map { parseCondition(it)!! } else listOf(),
        value = conditionNode["not"]?.asText()
    )
}

fun parseIncludeDirective(node: JsonNode): IncludeDirective? = node["include"]?.let { includeNode ->
    IncludeDirective(includeNode.asText())
}

fun parsePragmaDirective(node: JsonNode): PragmaDirective? = node["pragma"]?.let { pragmaNode ->
    PragmaDirective(
        docRequired = pragmaNode["doc-required"]?.asBoolean(),
        commandReturnsExceptions = pragmaNode["command-returns-exceptions"]?.map { it.asText() } ?: listOf(),
        commandNameExceptions = pragmaNode["command-name-exceptions"]?.map { it.asText() } ?: listOf(),
        memberNameExceptions = pragmaNode["member-name-exceptions"]?.map { it.asText() } ?: listOf()
    )
}

fun parseFeature(node: JsonNode): Feature = when {
    node.isTextual -> Feature(name = node.asText())
    node.has("name") -> Feature(
        name = node["name"].asText(),
        `if` = parseCondition(node)
    )
    else -> throw IllegalArgumentException("Malformed feature")
}

fun parseFeatures(node: JsonNode): List<Feature> = node["features"]?.map { parseFeature(it) } ?: listOf()

fun parseEnumValue(node: JsonNode): EnumValue = when {
    node.isTextual -> EnumValue(name = node.asText())
    node.has("name") -> EnumValue(
        name = node["name"].asText(),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
    else -> throw IllegalArgumentException("Malformed enum value")
}

fun parseEnumValues(node: JsonNode): List<EnumValue> = node["data"]?.map { parseEnumValue(it) } ?: listOf()

fun parseEnum(node: JsonNode, docString: (String) -> String? = { null }): Enum0? = node["enum"]?.asText()?.let { enumName ->
    Enum0(
        name = enumName,
        docString = docString(enumName),
        data = parseEnumValues(node),
        prefix = node["prefix"]?.asText(),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
}

fun parseStructMember(node: JsonNode): StructMember = when {
    node.isArray || node.isTextual -> StructMember(parseTypeRef(node))
    else -> StructMember(
        parseTypeRef(node["type"]),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
}

fun parseStructMembers(node: JsonNode): Map<String, StructMember> =
    node.fields().asSequence().associateBy({ it.key }, { parseStructMember(it.value) })

fun parseStruct(node: JsonNode, docString: (String) -> String? = { null }): Struct? = node["struct"]?.asText()?.let { structName ->
    Struct(
        name = structName,
        docString = docString(structName),
        data = parseStructMembers(node["data"]),
        base = node["base"]?.asText(),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
}

fun parseUnionBranch(node: JsonNode): UnionBranch = when {
    node.isArray || node.isTextual -> UnionBranch(parseTypeRef(node))
    else -> UnionBranch(
        parseTypeRef(node["type"]),
        `if` = parseCondition(node)
    )
}

fun parseUnionBranches(node: JsonNode): Map<String, UnionBranch> =
    node.fields().asSequence().associateBy({ it.key }, { parseUnionBranch(it.value) })

fun parseUnion(node: JsonNode, docString: (String) -> String? = { null }): Union? = node["union"]?.asText()?.let { unionName ->
    Union(
        name = unionName,
        docString = docString(unionName),
        base = node["base"].let { baseNode ->
            when {
                baseNode.isTextual -> null either baseNode.asText()
                else -> parseStructMembers(baseNode) either null
            }
        },
        discriminator = node["discriminator"].asText(),
        data = parseUnionBranches(node["data"]),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
}

fun parseAlternative(node: JsonNode): Alternative = when {
    node.isTextual -> Alternative(parseType(node.asText()))
    else -> Alternative(
        type = parseType(node["type"].asText()),
        `if` = parseCondition(node)
    )
}

fun parseAlternatives(node: JsonNode): Map<String, Alternative> =
    node.fields().asSequence().associateBy({ it.key }, { parseAlternative(it.value) })

fun parseAlternate(node: JsonNode, docString: (String) -> String? = { null }): Alternate? = node["alternate"]?.asText()?.let { alternateName ->
    Alternate(
        name = alternateName,
        docString = docString(alternateName),
        data = parseAlternatives(node["data"]),
        `if` = parseCondition(node),
        features = parseFeatures(node)
    )
}