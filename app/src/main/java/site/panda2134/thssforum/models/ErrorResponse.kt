package site.panda2134.thssforum.models

data class ErrorResponse (
    val message: String
)

data class ParsingError (
    val path: Array<String>,
    val message: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParsingError

        if (!path.contentEquals(other.path)) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.contentHashCode()
        result = 31 * result + message.hashCode()
        return result
    }
}

data class UnprocessableEntityResponse (
    val message: String,
    val errors: Array<ParsingError>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnprocessableEntityResponse

        if (message != other.message) return false
        if (!errors.contentEquals(other.errors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + errors.contentHashCode()
        return result
    }
}