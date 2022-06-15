package site.panda2134.thssforum.api

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.interceptors.LogRequestAsCurlInterceptor
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.gson.jsonBody
import com.google.gson.*
import io.gsonfire.DateSerializationPolicy
import io.gsonfire.GsonFireBuilder
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.models.*
import site.panda2134.thssforum.ui.LoginActivity
import java.lang.reflect.Type
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.collections.ArrayList


@Suppress("unused")
class APIWrapper(private val context: Context) {
    private val noToken = "NO_TOKEN"
    val gsonFireObject: Gson

    init {
        val gsonFireBuilder: GsonFireBuilder = GsonFireBuilder()
            .dateSerializationPolicy(DateSerializationPolicy.rfc3339)
        val gsonBuilder = gsonFireBuilder.createGsonBuilder()
            .registerTypeAdapter(Instant::class.java, object: JsonDeserializer<Instant> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): Instant {
                    if (json !is JsonPrimitive || ! json.isString) {
                        throw IllegalStateException("parsing Instant requires a string")
                    }
                    return Instant.parse(json.asString)
                }

            })
        gsonFireObject = gsonBuilder.create()
    }

    inline fun <reified T: Any> gsonFireDeserializer(): ResponseDeserializable<T> = gsonDeserializer<T>(gsonFireObject)

    var token: String
        get() = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            return pref.getString(getString(R.string.PREF_KEY_TOKEN), noToken).toString() // don't use an empty string, avoiding 400
        }
        private set(it) = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            with(pref.edit()) {
                this.putString(context.getString(R.string.PREF_KEY_TOKEN), it)
                apply()
            }
        }
    var currentUserId: String
        get() = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            return pref.getString(getString(R.string.PREF_KEY_UID), "").toString() // don't use an empty string, avoiding 400
        }
        private set(it) = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            with(pref.edit()) {
                this.putString(context.getString(R.string.PREF_KEY_UID), it)
                apply()
            }
        }
    val isLoggedIn: Boolean get() = (token != noToken)
    private var ossToken: UploadTokenResponse? = null

    fun logout() {
        token = noToken
        gotoLoginActivity()
    }

    private val fuel: FuelManager = FuelManager().apply {
        basePath = context.getString(R.string.API_BASEPATH)
        addRequestInterceptor(LogRequestAsCurlInterceptor)
        addResponseInterceptor { next ->
            { request, response ->
                val toastMessage = when (response.statusCode) {
                    401 -> {
                        token = noToken // reset token!
                        gotoLoginActivity()
                        context.getString(R.string.TOAST_NOT_LOGGED_IN)
                    }
                    403 -> {
                        val err = Gson().fromJson(String(response.data), ErrorResponse::class.java)
                        context.getString(R.string.TOAST_PERMISSION_DENIED) + "(${err.message})"
                    }
                    422 -> {
                        val err = Gson().fromJson(
                            String(response.data),
                            UnprocessableEntityResponse::class.java
                        )
                        val errorType = context.getString(R.string.TOAST_UNPROCESSABLE_ENTITY)
                        val errorMessage =
                            err.errors.joinToString { it.path.joinToString() + ':' + it.message }
                        "$errorType $errorMessage"
                    }
                    in 500..599 -> {
                        val err = Gson().fromJson(String(response.data), ErrorResponse::class.java)

                        context.getString(R.string.TOAST_INTERNAL_SERVER_ERROR) + "(${response.statusCode},${err.message})"
                    }
                    else -> null
                }
                if (toastMessage != null) {
                    MainScope().launch {
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                next(request, response)
            }
        }
    }

    fun gotoLoginActivity() {
        MainScope().launch {
            startActivity(context, Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }, Bundle())
        }
    }

    // 获取当前用户关注了哪些人
    suspend fun getFollowingUsers() =
        fuel.get("/profile/following/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<User>>())


    //获取当前用户被谁关注
    suspend fun getFollowers() =
        fuel.get("/profile/followers/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<User>>())


    //增加当前用户关注的人
    suspend fun followUser(uid: String) =
        fuel.post("/profile/following/")
            .authentication()
            .bearer(token)
            .jsonBody(Uid(uid))
            .awaitObject(gsonFireDeserializer<Uid>())


    //当前用户取消关注
    suspend fun unfollowUser(uid: String) =
        fuel.delete("/profile/following/$uid")
            .authentication()
            .bearer(token)
            .awaitUnit()


    //获得当前用户黑名单
    suspend fun getBlacklist() =
        fuel.get("/profile/blacklist/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<User>>())


    //新增黑名单用户
    suspend fun addBlacklistUser(uid: String) =
        fuel.post("/profile/blacklist/")
            .authentication()
            .bearer(token)
            .jsonBody(Uid(uid))
            .awaitObject(gsonFireDeserializer<Uid>())


    //删除黑名单用户
    suspend fun delBlacklistUser(uid: String) =
        fuel.delete("/profile/blacklist/$uid")
            .authentication()
            .bearer(token)
            .awaitUnit()


    //获得用户基本信息
    suspend fun getUserInfo(uid: String) =
        fuel.get("/users/$uid")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<User>())


    //获得用户动态
    suspend fun getUserPosts(
        uid: String,
        start: Instant? = null,
        end: Instant? = null
    ): List<Post> {
        val posts = fuel.get(
            "/users/$uid/posts?",
            listOf("start" to start?.toString(), "end" to end?.toString())
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<PostResponse>>())
        return posts.map { postResponse ->
            MainScope().async(Dispatchers.IO) {
                Post(this@APIWrapper.getUserInfo(postResponse.by), PostContent.fromPostResponse(postResponse))
            }
        }.awaitAll()
    }


    //修改当前用户基本信息
    suspend fun modifyProfile(body: ModifyProfileRequest) =
        fuel.put("/profile/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonFireDeserializer<User>())


    // 获取当前用户基本信息
    suspend fun getProfile() =
        fuel.get("/profile")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<User>())


    //注册用户
    suspend fun register(body: RegisterRequest) =
        fuel.post("/auth/register")
            .jsonBody(body)
            .awaitObject(gsonFireDeserializer<Uid>())


    //登陆
    suspend fun login(body: LoginRequest): LoginResponse {
        val response = fuel.post("/auth/login")
            .jsonBody(body)
            .awaitObject(gsonFireDeserializer<LoginResponse>())
        token = response.token
        currentUserId = response.uid
        return response
    }


    //修改当前用户密码
    suspend fun changePassword(body: ChangePasswordRequest) {
        fuel.put("/auth/change-password")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitUnit()
    }


    //查看点赞人数目
    suspend fun getNumOfLikes(post_id: String) =
        fuel.get("/posts/$post_id/like")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<LikeStatisticsResponse>())


    //给动态点赞
    suspend fun likeThisPost(post_id: String) =
        fuel.post("/posts/$post_id/like")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<NumOfLikesResponse>())


    //取消自己的点赞
    suspend fun unlikeThisPost(post_id: String) =
        fuel.delete("/posts/$post_id/like")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<NumOfLikesResponse>())


    enum class PostsSortBy(val value: String) {
        Time("time"),
        Like("like")
    }

    //获得动态列表
    suspend fun getPosts(
        start: Instant? = null,
        end: Instant? = null,
        sortBy: PostsSortBy = PostsSortBy.Time,
        skip: Int? = null,
        limit: Int? = null,
        types: List<PostType> = PostType.values().toList(),
        following: Boolean = false,
        scope: CoroutineScope = MainScope()
    ): List<Post> {
        val posts = fuel.get(
            "/posts/",
            listOf(
                "start" to start?.toString(),
                "end" to end?.toString(),
                "sort_by" to sortBy.value,
                "skip" to skip,
                "limit" to limit
            ) + (if (following) listOf("following" to "True") else listOf())
              + types.map { "type" to it.value }
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<PostResponse>>())
        return posts.map { postResponse ->
            scope.async(Dispatchers.IO) {
                Post(this@APIWrapper.getUserInfo(postResponse.by), PostContent.fromPostResponse(postResponse))
            }
        }.awaitAll()
    }



    //获得动态详细信息
    suspend fun getPostDetails(postId: String) =
        fuel.get("/posts/$postId")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<PostResponse>())
            .run {
                Post(this@APIWrapper.getUserInfo(by), PostContent.fromPostResponse(this))
            }


    //发布动态
    suspend fun newPost(content: PostContent) =
        fuel.post("/posts/")
            .authentication()
            .bearer(token)
            .jsonBody(content)
            .awaitObject(gsonFireDeserializer<NewPostResponse>())


    //修改动态
    suspend fun modifyPost(post_id: String, body: ModifyPostRequest) =
        fuel.put("/posts/$post_id")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonFireDeserializer<PostResponse>())


    //删除动态
    suspend fun deletePost(postId: String) {
        fuel.delete("/posts/$postId")
            .authentication()
            .bearer(token)
            .awaitUnit()
    }

    enum class CommentSortBy(val value: String) {
        NEWEST_FIRST("newest"),
        OLDEST_FIRST("oldest")
    }


    //动态评论区
    suspend fun getPostComments(
        postId: String,
        skip: Int? = null,
        limit: Int? = null,
        sortBy: CommentSortBy? = CommentSortBy.NEWEST_FIRST,
        scope: CoroutineScope = MainScope()
        ) =
        fuel.get(
            "/posts/$postId/comments/",
            listOf("skip" to skip, "limit" to limit, "sort_by" to sortBy?.value)
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<ArrayList<CommentResponse>>())
            .map { commentResponse ->
                scope.async(Dispatchers.IO) {
                    Comment(this@APIWrapper.getUserInfo(commentResponse.by), commentResponse)
                }
            }.awaitAll()


    //发布评论
    suspend fun newComment(postId: String, body: NewCommentRequest) =
        fuel.post("/posts/$postId/comments/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonFireDeserializer<NewCommentResponse>())


    //获得评论详细信息
    suspend fun getCommentInfo(postId: String, commentId: String) =
        fuel.get("/posts/$postId/comments/$commentId")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<CommentResponse>())
            .run {
                Comment(this@APIWrapper.getUserInfo(by), this)
            }


    //删除评论
    suspend fun deleteComment(postId: String, commentId: String) =
        fuel.delete("/posts/$postId/comments/$commentId")
            .authentication()
            .bearer(token)
            .awaitUnit()


    //获得阿里云OSS上传的临时token
    private suspend fun getUploadToken() =
        fuel.post("/upload/token")
            .authentication()
            .bearer(token)
            .awaitObject(gsonFireDeserializer<UploadTokenResponse>())

    suspend fun uploadFileToOSS(uri: Uri): Uri = this.uploadFileToOSS(uri, null)

    suspend fun uploadFileToOSS(uri: Uri, progressCallback: OSSProgressCallback<PutObjectRequest>?): Uri {
        var localOSSToken = ossToken
        var tokenExpired = true
        try {
            if (localOSSToken != null) {
                tokenExpired = Instant.parse(localOSSToken.expiresAt).isBefore(Instant.now())
            }
        } catch (e: DateTimeParseException) {
            // default to expired
        }
        val myProfile = getProfile()
        val uidReversed = myProfile.uid.reversed()

        if (localOSSToken == null || tokenExpired) {
            localOSSToken = getUploadToken()
            ossToken = localOSSToken
        }
        val ossEndpoint = context.getString(R.string.OSS_ENDPOINT)
        val bucketDomain = context.getString(R.string.OSS_BUCKET_DOMAIN)
        val credProvider = OSSStsTokenCredentialProvider(localOSSToken.accessKeyId, localOSSToken.accessKeySecret, localOSSToken.securityToken)
        val oss = OSSClient(context, ossEndpoint, credProvider)
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        val objectKey = "upload/${uidReversed.substring(0..1)}/${myProfile.uid}/${UUID.randomUUID()}.$extension"
        withContext(Dispatchers.IO) {
            val req = PutObjectRequest(
                context.getString(R.string.OSS_BUCKET),
                objectKey,
                uri
            )
            if (progressCallback != null) {
                req.setProgressCallback { request, currentSize, totalSize ->
                    MainScope().launch {
                        progressCallback.onProgress(request, currentSize, totalSize)
                    }
                }
            }
            oss.putObject(req)
        }
        return Uri.parse("https://$bucketDomain/$objectKey")
    }
}

