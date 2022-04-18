package me.lusory.ostrich.gen

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.File

class Generator(files: List<File>, private val sourceDir: File) {
    companion object {
        private val MAPPER: ObjectMapper = jsonMapper {
            addModule(kotlinModule())
            enable(JsonParser.Feature.ALLOW_YAML_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        }
    }

    val readers: MutableMap<String, ReaderItem> = mutableMapOf()

    init {
        for (file: File in files) {
            val split: List<String> = file.readText().split("\r\n", "\n")
            val descriptors: MutableList<Pair<String, String>> = mutableListOf()

            var skipTimes = 0
            var iterating = false
            var name: String? = null
            val builder = StringBuilder()
            split.forEachIndexed { i, item ->
                if (skipTimes > 0) {
                    skipTimes--
                    return@forEachIndexed
                }

                if (iterating) {
                    if (item.startsWith("##")) {
                        iterating = false
                        descriptors.add(Pair(
                            name ?: throw IllegalStateException(),
                            builder.toString()
                        ))
                    } else if (item == "#" || item == "") {
                        builder.append("\n")
                    } else {
                        builder.append(item.substring(if (item.startsWith("# ")) 2 else 1)).append("\n")
                    }
                    return@forEachIndexed
                }

                if (item.startsWith("# ")) {
                    return@forEachIndexed
                }

                // element doc
                if (item.startsWith("##") && split[i + 1].startsWith("# @")) {
                    skipTimes += 1
                    if (split[i + 2].trim() == "#") {
                        skipTimes += 1
                    }
                    name = split[i + 1].substringAfterLast('@').dropLast(1)
                    iterating = true
                }
            }

            descriptors.forEach { readers[it.first] = ReaderItem(file.nameWithoutExtension, it.first, it.second) }

            MAPPER.readValues(MAPPER.createParser(file), JsonNode::class.java)
                .forEach { node ->
                    readers[node.schemaName ?: return@forEach]?.json = node
                }
        }
    }

    private val JsonNode.schemaType: String
        get() = when {
            has("command") -> "command"
            has("struct") -> "struct"
            has("type") -> "type"
            has("enum") -> "enum"
            has("union") -> "union"
            has("alternate") -> "alternate"
            has("event") -> "event"
            has("include") -> "include"
            has("pragma") -> "pragma"
            else -> throw IllegalArgumentException("Unknown schema type")
        }

    private val JsonNode.schemaName: String?
        get() = when (schemaType) {
            "pragma" -> null
            "include" -> null
            else -> get(schemaType)?.asText()
        }
}