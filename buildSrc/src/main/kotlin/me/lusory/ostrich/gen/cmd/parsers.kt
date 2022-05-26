package me.lusory.ostrich.gen.cmd

import me.lusory.ostrich.gen.cmd.model.Stub
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.File

private val LOGGER: Logger = Logging.getLogger("me.lusory.ostrich.gen.cmd.ParsersKt")
val STUB_REGEX = Regex("DEF\\(((?:(?!DEF\\().)*)\\)\\n?SRST\\n?((?:(?!ERST).)*)\\n?ERST")

fun parseStubs(file: File): List<Stub> = STUB_REGEX.findAll(file.readText()).map { result ->
    val params: MutableList<String> = mutableListOf()
    val builder = StringBuilder()
    var parsingQuote = false
    var skipNext = false

    for (c: Char in result.groupValues[1].toCharArray()) {
        if (skipNext) {
            skipNext = false
            continue
        }
        if (parsingQuote) {
            if (c == '"') {
                parsingQuote = false
                continue
            }
            builder.append(c)
            continue
        }

        if (c == '"') {
            parsingQuote = true
            continue
        } else if (c == ',') {
            params.add(builder.toString().trim())
            builder.setLength(0)
            skipNext = true
        } else {
            builder.append(c)
        }
    }
    if (builder.isNotEmpty()) {
        params.add(builder.toString().trim())
        builder.setLength(0)
    }

    return@map Stub(params, result.groupValues[2].trim())
}.toList()

fun searchDoc(toFind: String, content: String): String? {
    val builder = StringBuilder()
    var found = false

    for (line: String in content.split("\n")) {
        if (found) {
            if (line.isEmpty() || line.startsWith("  ")) { // two spaces
                builder.appendLine(line.trim())
                continue
            }
            break
        }

        if (line == toFind) {
            found = true
        }
    }

    return if (found) builder.toString().trim() else null
}

fun stitchDocs(stubs: List<Stub>, doc: File): List<Stub> {
    val content: String = doc.readText()

    return stubs.map { stub ->
        val result: String? = searchDoc(stub.rst, content)

        if (result != null) {
            Stub(stub.params, result)
        } else {
            LOGGER.warn("Couldn't stitch $stub: doc not found")
            stub
        }
    }
}