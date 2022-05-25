package me.lusory.ostrich.gen.qapi.model

data class Either<A : Any, B : Any>(
    val first: A?,
    val second: B?
) {
    init {
        check((first == null && second != null) || (first != null && second == null)) { "Either must have one value null" }
    }
}

infix fun <A : Any, B : Any> A?.either(other: B?): Either<A, B> = Either(this, other)
