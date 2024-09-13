package hu.mcold.gordian.common

open class MyByteArray(
    val values: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyByteArray

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    operator fun get(idx: Int) = values[idx]
    val size get() = values.size

    override fun toString(): String {
        return buildString {
            append('[')
            values.forEach {
                append(it)
                append(',')
            }
            append(']')
        }
    }
}
