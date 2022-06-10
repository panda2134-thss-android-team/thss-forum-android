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
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.interceptors.LogRequestAsCurlInterceptor
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.gson.jsonBody
import com.google.gson.Gson
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.models.*
import site.panda2134.thssforum.ui.LoginActivity
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*

@Suppress("unused")
class APIService(private val context: Context) {
    val noToken = "NO_TOKEN"

    private var token: String
        get() = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            return pref.getString(getString(R.string.PREF_KEY_TOKEN), noToken).toString() // don't use an empty string, avoiding 400
        }
        set(it) = with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            with(pref.edit()) {
                this.putString(context.getString(R.string.PREF_KEY_TOKEN), it)
                apply()
            }
        }
    val isLoggedIn: Boolean get() = (token != noToken)
    private var ossToken: UploadTokenResponse? = null

    suspend fun ensureLoggedIn() {
        if (!isLoggedIn) {
            gotoLoginActivity()
        }
        try {
            getProfile() // if the token expired, login activity will be launched
        } catch (e: FuelError) {
            if (e.response.statusCode == 401) {
                gotoLoginActivity()
            }
        }
    }

    private val fuel: FuelManager = FuelManager().apply {
        basePath = "https://lab.panda2134.site:20443"
        timeoutInMillisecond = 5000
        timeoutReadInMillisecond = 5000
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

    private fun gotoLoginActivity() {
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
            .awaitObject(gsonDeserializer<ArrayList<User>>())


    //获取当前用户被谁关注
    suspend fun getFollowers() =
        fuel.get("/profile/followers/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<ArrayList<User>>())


    //增加当前用户关注的人
    suspend fun followUser(uid: String) =
        fuel.post("/profile/following/")
            .authentication()
            .bearer(token)
            .jsonBody(Uid(uid))
            .awaitObject(gsonDeserializer<Uid>())


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
            .awaitObject(gsonDeserializer<ArrayList<User>>())


    //新增黑名单用户
    suspend fun addBlacklistUser(uid: String) =
        fuel.post("/profile/blacklist/")
            .authentication()
            .bearer(token)
            .jsonBody(Uid(uid))
            .awaitObject(gsonDeserializer<Uid>())


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
            .awaitObject(gsonDeserializer<User>())


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
            .awaitObject(gsonDeserializer<ArrayList<PostResponse>>())
        return posts.map { postResponse ->
            MainScope().async(Dispatchers.IO) {
                Post(this@APIService.getUserInfo(postResponse.by), PostContent.fromPostResponse(postResponse))
            }
        }.awaitAll()
    }


    //修改当前用户基本信息
    suspend fun modifyProfile(body: ModifyProfileRequest) =
        fuel.put("/profile/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<User>())


    // 获取当前用户基本信息
    suspend fun getProfile() =
        fuel.get("/profile")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<User>())


    //注册用户
    suspend fun register(body: RegisterRequest) =
        fuel.post("/auth/register")
            .jsonBody(body)
            .awaitObject(gsonDeserializer<Uid>())


    //登陆
    suspend fun login(body: LoginRequest): LoginResponse {
        val response = fuel.post("/auth/login")
            .jsonBody(body)
            .awaitObject(gsonDeserializer<LoginResponse>())
        token = response.token
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
            .awaitObject(gsonDeserializer<NumOfLikesResponse>())


    //给动态点赞
    suspend fun likeThisPost(post_id: String) =
        fuel.post("/posts/$post_id/like")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<NumOfLikesResponse>())


    //取消自己的点赞
    suspend fun unlikeThisPost(post_id: String) =
        fuel.delete("/posts/$post_id/like")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<NumOfLikesResponse>())


    //获得动态列表
    suspend fun getPosts(
        start: Instant? = null,
        end: Instant? = null,
        following: Boolean = false,
        scope: CoroutineScope = MainScope()
    ): List<Post> {
        val posts = fuel.get(
            "/posts/",
            listOf(
                "start" to start?.toString(),
                "end" to end?.toString(),
            ) + (if (following) listOf("following" to "True") else listOf())
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<ArrayList<PostResponse>>())
        return posts.map { postResponse ->
            scope.async(Dispatchers.IO) {
                Post(this@APIService.getUserInfo(postResponse.by), PostContent.fromPostResponse(postResponse))
            }
        }.awaitAll()
    }



    //获得动态详细信息
    suspend fun getPostDetails(postId: String) =
        fuel.get("/posts/$postId")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<PostResponse>())
            .run {
                Post(this@APIService.getUserInfo(by), PostContent.fromPostResponse(this))
            }


    //发布动态
    suspend fun newPost(content: PostContent) =
        fuel.post("/posts/")
            .authentication()
            .bearer(token)
            .jsonBody(content)
            .awaitObject(gsonDeserializer<NewPostResponse>())


    //修改动态
    suspend fun modifyPost(post_id: String, body: ModifyPostRequest) =
        fuel.put("/posts/$post_id")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<PostResponse>())


    //删除动态
    suspend fun deletePost(postId: String) {
        fuel.delete("/posts/$postId")
            .authentication()
            .bearer(token)
            .awaitUnit()
    }

    enum class SortBy(val value: String) {
        NEWEST_FIRST("newest"),
        OLDEST_FIRST("oldest")
    }


    //动态评论区
    suspend fun getPostComments(
        postId: String,
        skip: Int? = null,
        limit: Int? = null,
        sortBy: SortBy? = SortBy.NEWEST_FIRST,
        scope: CoroutineScope = MainScope()
        ) =
        fuel.get(
            "/posts/$postId/comments/",
            listOf("skip" to skip, "limit" to limit, "sort_by" to sortBy?.value)
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<ArrayList<CommentResponse>>())
            .map { commentResponse ->
                scope.async(Dispatchers.IO) {
                    Comment(this@APIService.getUserInfo(commentResponse.by), commentResponse)
                }
            }.awaitAll()


    //发布评论
    suspend fun newComment(postId: String, body: NewCommentRequest) =
        fuel.post("/posts/$postId/comments/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<NewCommentResponse>())


    //获得评论详细信息
    suspend fun getCommentInfo(postId: String, commentId: String) =
        fuel.get("/posts/$postId/comments/$commentId")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<CommentResponse>())
            .run {
                Comment(this@APIService.getUserInfo(by), this)
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
            .awaitObject(gsonDeserializer<UploadTokenResponse>())

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

