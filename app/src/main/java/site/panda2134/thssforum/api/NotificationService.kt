package site.panda2134.thssforum.api

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.kittinunf.fuel.core.FuelError
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.*
import io.ktor.serialization.gson.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import site.panda2134.thssforum.R
import site.panda2134.thssforum.models.NotificationType
import site.panda2134.thssforum.models.WSLoginRequest
import site.panda2134.thssforum.models.WSLoginResponse
import site.panda2134.thssforum.models.WSPushNotification
import java.nio.channels.ClosedChannelException
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class NotificationService : Service() {
    companion object {
        fun buildStartIntentFrom(context: Context, token: String): Intent {
            return Intent(context, NotificationService::class.java).putExtra("token", token).putExtra("ssaid", Settings.Secure.ANDROID_ID)
        }
    }

    class NotificationStringBuilder(val context: Context) {
        private val api = APIWrapper(context)
        data class NotificationStringTuple (val title: String, val content: String)

        suspend fun build(n: WSPushNotification) =
            n.run {
                when (type) {
                    NotificationType.followingUpdated -> {
                        var nickname = context.getString(R.string.unknown)
                        try {
                            nickname =
                                api.getUserInfo(uid!!).nickname
                        } catch (e: FuelError) {
                            e.printStackTrace()
                        }
                        NotificationStringTuple(context.getString(R.string.notification_channel_following_updated),
                            context.getString(
                                R.string.notification_following_updated,
                                nickname
                            )
                        )
                    }
                    NotificationType.postLiked -> {
                        var nickname = context.getString(R.string.unknown)
                        var postTitle = context.getString(R.string.unknown)
                        try {
                            nickname =
                                api.getUserInfo(uid!!).nickname
                            postTitle = api.getPostDetails(
                                postId ?: ""
                            ).postContent.getTitle()
                        } catch (e: FuelError) {
                            e.printStackTrace()
                        }
                        NotificationStringTuple(context.getString(R.string.notification_channel_post_liked),
                            context.getString(
                                R.string.notification_post_liked,
                                nickname,
                                postTitle
                            )
                        )
                    }
                    NotificationType.commentLiked -> {
                        var nickname = context.getString(R.string.unknown)
                        var commentContent = context.getString(R.string.unknown)
                        try {
                            nickname =
                                api.getUserInfo(uid!!).nickname
                            commentContent = api.getCommentInfo(
                                postId!!,
                                commentId!!
                            ).data.content
                        } catch (e: FuelError) {
                            e.printStackTrace()
                        }
                        NotificationStringTuple(context.getString(R.string.notification_channel_comment_liked),
                            context.getString(
                                R.string.notification_comment_liked,
                                nickname,
                                commentContent
                            )
                        )
                    }
                    NotificationType.postCommented -> {
                        var nickname = context.getString(R.string.unknown)
                        var postTitle = context.getString(R.string.unknown)
                        try {
                            nickname =
                                api.getUserInfo(uid!!).nickname
                            postTitle = api.getPostDetails(
                                postId ?: ""
                            ).postContent.getTitle()
                        } catch (e: FuelError) {
                            e.printStackTrace()
                        }
                        NotificationStringTuple(context.getString(R.string.notification_channel_post_commented),
                            context.getString(
                                R.string.notification_post_commented,
                                nickname,
                                postTitle
                            )
                        )
                    }
                    NotificationType.commentReplied -> {
                        var nickname = context.getString(R.string.unknown)
                        var commentContent = context.getString(R.string.unknown)
                        try {
                            nickname =
                                api.getUserInfo(uid!!).nickname
                            commentContent = api.getCommentInfo(
                                postId!!,
                                commentId!!
                            ).data.content
                        } catch (e: FuelError) {
                            e.printStackTrace()
                        }
                        NotificationStringTuple(context.getString(R.string.notification_channel_comment_replied),
                            context.getString(
                                R.string.notification_comment_replied,
                                nickname,
                                commentContent
                            )
                        )
                    }
                }
            }

    }

    data class NotificationItem(
        @SerializedName("content")
        val content: WSPushNotification,
        @SerializedName("read")
        var read: Boolean = false
    ) {
        @SerializedName("received_at")
        val receivedAt: Instant = Instant.now()
    }

    private val TAG = "THSSFORUM_NOTIFICATION_SERVICE"
    private lateinit var api: APIWrapper
    private val notifications = mutableSetOf<NotificationItem>()
    private val scope = MainScope()
    private val notificationCount = AtomicInteger(128)
    private lateinit var notificationManager: NotificationManagerCompat
    private var socketConnected = false
    private val client = HttpClient(OkHttp) {
        install(WebSockets) {
            pingInterval = 1800 * 1000 // 30min
            contentConverter = GsonWebsocketContentConverter()
        }
    }
    private fun buildNotificationChannel (channelType: NotificationType, resChannelName: Int, resChannelDescription: Int)
        = NotificationChannelCompat.Builder(channelType.value, NotificationManagerCompat.IMPORTANCE_HIGH)
        .setName(getString(resChannelName)).setDescription(getString(resChannelDescription)).build()
        .run {
            notificationManager.createNotificationChannel(this)
            channelType to this
        }
    private lateinit var channelMap: Map<NotificationType, NotificationChannelCompat>

    override fun onCreate() {
        api = APIWrapper(this)
        notificationManager = NotificationManagerCompat.from(this)
        channelMap = mapOf(
            buildNotificationChannel(
                NotificationType.followingUpdated,R.string.notification_channel_following_updated,
               R.string.notification_channel_following_updated_description),
            buildNotificationChannel(NotificationType.postLiked, R.string.notification_channel_post_liked,
                R.string.notification_channel_post_liked_description),
            buildNotificationChannel(NotificationType.commentLiked, R.string.notification_channel_comment_liked,
                R.string.notification_channel_comment_liked_description),
            buildNotificationChannel(NotificationType.postCommented, R.string.notification_channel_post_commented,
                R.string.notification_channel_post_commented_description),
            buildNotificationChannel(NotificationType.commentReplied, R.string.notification_channel_comment_replied,
                R.string.notification_channel_comment_replied_description)
        )
        val pushNotificationChannel = NotificationChannelCompat.Builder("push_notification", NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(getString(R.string.notification_channel_background)).setDescription(getString(R.string.notification_channel_background_description))
            .build()
            .apply {
                notificationManager.createNotificationChannel(this)
            }
        startForeground(1, NotificationCompat.Builder(this, pushNotificationChannel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_channel_background))
            .setContentText(getString(R.string.notification_background)).build())

        with (getSharedPreferences(getString(R.string.NOTIFICATION_SHARED_PREF), MODE_PRIVATE)) {
            notifications.addAll(all.values.map {
                if (it !is String) return@map null
                try {
                    api.gsonFireObject.fromJson(it, NotificationItem::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.d(javaClass.name, "failed to parse notification history")
                    null
                }
            }.filterNotNull())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            throw IllegalArgumentException("intent cannot be null")
        }
        if (socketConnected) {
            Log.d(TAG, "already started")
        } else {
            socketConnected = true

            Log.d(TAG, "notification: connecting to ws")
            scope.launch(Dispatchers.IO) {
                while (true) {
                    try {
                        client.webSocket(urlString = getString(R.string.WS_BASEPATH)) {
                            sendSerialized(
                                WSLoginRequest(
                                    intent.getStringExtra("token")!!,
                                    intent.getStringExtra("ssaid")!!
                                )
                            )
                            try {
                                val response = receiveDeserialized<WSLoginResponse>()
                                if (response.type == WSLoginResponse.WSLoginResponseType.error) {
                                    throw IllegalStateException()
                                }
                            } catch (e: Throwable) {
                                socketConnected = false
                                return@webSocket
                            }
                            Log.d(TAG,"connected")
                            while (true) {
                                try {
                                    val pushed = receiveDeserialized<WSPushNotification>()
                                    val notificationItem = NotificationItem(pushed)
                                    notifications.add(notificationItem)
                                    with (getSharedPreferences(getString(R.string.NOTIFICATION_SHARED_PREF), MODE_PRIVATE).edit()) {
                                        val j = api.gsonFireObject.toJson(notificationItem)
                                        putString(pushed.notificationId, j)
                                        apply()
                                    }
                                    val channel = channelMap[pushed.type] ?: continue
                                    val s = NotificationStringBuilder(this@NotificationService).build(pushed)
                                    val systemNotification = NotificationCompat
                                        .Builder(this@NotificationService, channel.id)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .apply {
                                            setContentTitle(s.title)
                                            setContentText(s.content)
                                        }
                                        .build()
                                    notificationManager.notify(
                                        notificationCount.getAndIncrement(),
                                        systemNotification
                                    )
                                } catch (e: ClosedReceiveChannelException) {
                                    socketConnected = false
                                    return@webSocket
                                } catch (e: WebsocketDeserializeException) {
                                    // ignore this frame
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@NotificationService,
                                R.string.TOAST_NOTIFICATION_SERVER_NO_CONNECTION,
                                Toast.LENGTH_LONG
                            )
                        }
                        e.printStackTrace()
                    }
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        scope.cancel()
    }

    inner class LocalBinder: Binder() {
        fun getNotifications() = notifications.toList()
        fun setRead(notificationId: String) {
            val result = notifications.find { it.content.notificationId == notificationId } ?: return
            result.read = true
        }
    }
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}