package site.panda2134.thssforum.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.toISOString(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'h:m:ssZZZZZ", Locale.US)
    return fmt.format(this).replace("(\\d\\d)(\\d\\d)$", "$1:$2")
}