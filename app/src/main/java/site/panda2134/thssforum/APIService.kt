package site.panda2134.thssforum

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import com.example.campusforum.R
import com.google.gson.Gson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.interceptors.LogRequestAsCurlInterceptor
import com.github.kittinunf.fuel.coroutines.*
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.gson.jsonBody
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import site.panda2134.thssforum.models.*
import site.panda2134.thssforum.utils.toISOString

class APIService(private val context: Context) {
    private var token: String

    init {
        with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            token = pref.getString(getString(R.string.PREF_KEY_TOKEN), "").toString()
        }
    }

    val fuel: FuelManager = FuelManager().apply {
        basePath = "https://lab.panda2134.site:20443"
        timeoutInMillisecond = 5000
        timeoutReadInMillisecond = 5000
        addRequestInterceptor(LogRequestAsCurlInterceptor)
        addResponseInterceptor { next ->
            { request, response ->
                val toastMessage = when (response.statusCode) {
                    401 -> {
                        token = "" // reset token!
//                        TODO("跳转到登录，并且清空返回栈")
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

    // 获取当前用户关注了哪些人
    suspend fun getFollowingUsers() =
        fuel.get("/profile/following/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Array<User>>())


    //获取当前用户被谁关注
    suspend fun getFollowers() =
        fuel.get("/profile/followers/")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Array<User>>())


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
            .awaitObject(gsonDeserializer<Array<User>>())


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
        start: java.util.Date? = null,
        end: java.util.Date? = null
    ): Array<Post> {
        return fuel.get(
            "/users/$uid/posts?",
            listOf("start" to start?.toISOString(), "end" to end?.toISOString())
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Array<Post>>())
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
        with(context) {
            val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
            with(pref.edit()) {
                this.putString(context.getString(R.string.PREF_KEY_TOKEN), response.token)
                apply()
            }
        }
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
        start: java.util.Date? = null,
        end: java.util.Date? = null,
        following: String? = null
    ) {
        fuel.get(
            "/posts/",
            listOf(
                "start" to start?.toISOString(),
                "end" to end?.toISOString(),
                "following" to following
            )
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Array<Post>>())
    }


    //获得动态详细信息
    suspend fun getPostInfo(post_id: String) =
        fuel.get("/posts/$post_id")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Post>())


    //发布动态
    suspend fun newPost(body: Post) =
        fuel.post("/posts/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<NewPostResponse>())


    //修改动态
    suspend fun modifyPost(post_id: String, body: ModifyPostRequest) =
        fuel.put("/posts/$post_id")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<Post>())


    //删除动态
    suspend fun deletePost(post_id: String) {
        fuel.delete("/posts/$post_id")
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
        post_id: String,
        skip: Int? = null,
        limit: Int? = null,
        sort_by: SortBy? = SortBy.NEWEST_FIRST
    ) =
        fuel.get(
            "/posts/$post_id/comments/",
            listOf("skip" to skip, "limit" to limit, "sort_by" to sort_by?.value)
        )
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Array<Comment>>())


    //发布评论
    suspend fun newComment(postId: String, body: NewCommentRequest) =
        fuel.post("/posts/$postId/comments/")
            .authentication()
            .bearer(token)
            .jsonBody(body)
            .awaitObject(gsonDeserializer<NewCommentResponse>())


    //获得评论详细信息
    suspend fun getCommentInfo(post_id: String, comment_id: String) =
        fuel.get("/posts/$post_id/comments/$comment_id")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<Comment>())


    //删除评论
    suspend fun deleteComment(post_id: String, comment_id: String) =
        fuel.delete("/posts/$post_id/comments/$comment_id")
            .authentication()
            .bearer(token)
            .awaitUnit()


    //获得阿里云OSS上传的临时token
    suspend fun getUploadToken() =
        fuel.post("/upload/token")
            .authentication()
            .bearer(token)
            .awaitObject(gsonDeserializer<UploadTokenResponse>())

}

