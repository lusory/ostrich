package me.lusory.ostrich.gen.model

data class Feature(
    val name: String,
    val `if`: Condition? = null
)
