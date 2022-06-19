package me.lusory.ostrich.gen.cmd

import me.lusory.ostrich.gen.cmd.model.Stub
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.File

private val LOGGER: Logger = Logging.getLogger("me.lusory.ostrich.gen.cmd.ParsersKt")
val STUB_REGEX = Regex("DEF\\(((?:(?!DEF\\().)*)\\)(?:\\r\\n|\\r|\\n)?SRST(?:\\r\\n|\\r|\\n)?((?:(?!ERST).)*)(?:\\r\\n|\\r|\\n)?ERST", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))

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
            if ((builder.length == 0 || builder.all { it.isWhitespace() }) && c == '\\') {
                continue
            }
            builder.append(c)
        }
    }
    if (builder.isNotEmpty()) {
        params.add(builder.toString().trim())
        builder.setLength(0)
    }

    return@map Stub(params, result.groupValues[2].trim())
}.toList()

fun searchDoc(toFind: String, content: String, matchFully: Boolean): String? {
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

        if ((line == toFind && matchFully) || (line.startsWith(toFind) && !matchFully)) {
            found = true
        }
    }

    return if (found) builder.toString().trim() else null
}

fun stitchDocs(stubs: List<Stub>, doc: File, matchFully: Boolean = true): List<Stub> {
    val content: String = doc.readText().replace("\\r", "")

    return stubs.map { stub ->
        val result: String? = if (matchFully) {
            searchDoc(stub.rst, content, matchFully)
        } else {
            searchDoc(".. option:: ${stub.params[0]}", content, matchFully)
        }

        if (result != null) {
            Stub(stub.params, result)
        } else {
            LOGGER.warn("Couldn't stitch $stub: doc not found")
            stub
        }
    }
}

fun parseBrackets(s: String): List<Any> {
    val result: MutableList<Any> = mutableListOf()
    val builder = StringBuilder()

    var openedBrackets = 0
    var appending = false
    for (c: Char in s.toCharArray()) {
        if (appending) {
            if (c == '[') {
                openedBrackets++
            } else if (c == ']') {
                if (openedBrackets == 0) {
                    result.add(parseBrackets(builder.toString()))
                    builder.setLength(0)
                    appending = false
                    continue
                }
                openedBrackets--
            }

            builder.append(c)
            continue
        }

        if (c == ' ') {
            if (builder.isNotEmpty()) {
                result.add(builder.toString())
                builder.setLength(0)
            }
        } else if (c == '[') {
            appending = true
        } else {
            builder.append(c)
        }
    }
    if (appending) {
        result.add(parseBrackets(builder.toString()))
    } else if (builder.isNotEmpty()) {
        result.add(builder.toString())
    }

    return result
}

// TODO: refactor this
fun splitArgs(args: List<*>): List<List<String>> {
    fun flattenAny(elem: Any?): List<*> = if (elem is List<*>) {
        elem.flatMap(::flattenAny)
    } else {
        if (elem == "|") {
            listOf()
        } else {
            listOf(elem)
        }
    }

    val result: MutableList<List<String>> = mutableListOf()
    val args0: MutableList<*> = args.flatMap(::flattenAny).toMutableList()
    var current: MutableList<String> = mutableListOf()
    var foundOption = false

    while (args0.isNotEmpty()) {
        val elem: String = args0.removeFirst() as String

        if (foundOption) {
            if (!elem.startsWith('-')) {
                current.add(elem)
                result.add(current)
                current = mutableListOf()
                foundOption = false
                continue
            }
            result.add(current)
            current = mutableListOf()
            foundOption = false
        }

        if (elem.startsWith('-')) {
            current.add(elem)
            foundOption = true
        } else {
            result.add(listOf(elem))
        }
    }

    return result
}