package me.lusory.ostrich.gen.cmd

import me.lusory.ostrich.gen.cmd.model.Stub
import java.io.File

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

    return@map Stub(params, result.groupValues[2].trim())
}.toList()