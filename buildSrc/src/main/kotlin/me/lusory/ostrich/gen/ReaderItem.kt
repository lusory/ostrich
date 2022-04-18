package me.lusory.ostrich.gen

import com.fasterxml.jackson.databind.JsonNode

data class ReaderItem(
    val file: String,
    val name: String,
    val doc: String,
    var json: JsonNode? = null
)
