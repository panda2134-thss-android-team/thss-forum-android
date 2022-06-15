package site.panda2134.thssforum.utils

import androidx.appcompat.app.AppCompatDelegate
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.time.Instant
import java.util.*

fun Instant.toTimeAgo(): String {
    val timeAgoMessages = AppCompatDelegate.getApplicationLocales()[0].let { firstLocale ->
        TimeAgoMessages.Builder().withLocale(
            Locale.forLanguageTag(firstLocale?.language ?: "en")
        )
    }.build()

    return TimeAgo.using(toEpochMilli(), timeAgoMessages)
}