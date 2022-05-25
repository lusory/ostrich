package me.lusory.ostrich.gen.qapi.model

data class Feature(
    val name: String,
    val `if`: Condition? = null
)
