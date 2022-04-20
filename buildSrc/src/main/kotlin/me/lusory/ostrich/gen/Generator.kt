package me.lusory.ostrich.gen

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import me.lusory.ostrich.gen.model.SchemaFile
import me.lusory.ostrich.gen.model.SchemaType
import java.io.File

class Generator(files: List<File>, private val sourceDir: File) {
    companion object {
        private val MAPPER: ObjectMapper = jsonMapper {
            addModule(kotlinModule())
            enable(JsonParser.Feature.ALLOW_YAML_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        }
    }

    val files: MutableMap<String, SchemaFile> = mutableMapOf()

    init {
        for (file: File in files) {
            val split: List<String> = file.readText().split("\r\n", "\n")
            val descriptors: MutableList<Pair<String, String?>> = mutableListOf()

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
                        if (name != null) {
                            descriptors.add(Pair(
                                name!!,
                                builder.toString().ifEmpty { null }
                            ))
                        }
                        builder.setLength(0)
                    } else if (item == "#" || item.isEmpty()) {
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
                    if (split[i + 2].trim() == "#" || split[i + 2].trim().isEmpty()) {
                        skipTimes += 1
                    }
                    name = split[i + 1].substringAfterLast('@').dropLast(1)
                    iterating = true
                }
            }

            val fileName: String = file.nameWithoutExtension
            this.files[fileName] = SchemaFile(
                fileName,
                MAPPER.readValues(MAPPER.createParser(file), JsonNode::class.java).readAll()
                    .map { node ->
                        when (parseSchemaType(node)) {
                            SchemaType.ENUM -> parseEnum(node) { name -> descriptors.firstOrNull { it.first == name }?.second }
                            else -> throw UnsupportedOperationException()
                        } ?: throw IllegalStateException()
                    }
            )
        }
    }
}