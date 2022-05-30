package site.panda2134.thssforum

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.*
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.gson.jsonBody
import site.panda2134.thssforum.models.*
import site.panda2134.thssforum.utils.toISOString

class APIService {
    companion object {
        init {
            FuelManager.instance.apply {
                basePath = "https://lab.panda2134.site:20443"
                timeoutInMillisecond = 5000
                timeoutReadInMillisecond = 5000
            }
        }

        // 获取当前用户关注了哪些人
        suspend fun getFollowingUsers(token: String) =
            Fuel.get("/profile/following/")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<User>>())


        //获取当前用户被谁关注
        suspend fun getFollowers(token: String) =
            Fuel.get("/profile/followers/")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<User>>())


        //增加当前用户关注的人
        suspend fun followUser(token: String, uid: String) =
            Fuel.post("/profile/following/")
                .authentication()
                .bearer(token)
                .jsonBody(Uid(uid))
                .awaitObject(gsonDeserializer<Uid>())


        //当前用户取消关注
        suspend fun unfollowUser(token: String, uid: String) =
            Fuel.delete("/profile/following/$uid")
                .authentication()
                .bearer(token)
                .awaitStringResponse()


        //获得当前用户黑名单
        suspend fun getBlacklist(token: String) =
            Fuel.get("/profile/blacklist/")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<User>>())


        //新增黑名单用户
        suspend fun addBlacklistUser(token: String, uid: String) =
            Fuel.post("/profile/blacklist/")
                .authentication()
                .bearer(token)
                .jsonBody(Uid(uid))
                .awaitObject(gsonDeserializer<Uid>())


        //删除黑名单用户
        suspend fun delBlacklistUser(token: String, uid: String) =
            Fuel.delete("/profile/blacklist/$uid")
                .authentication()
                .bearer(token)
                .awaitString()


        //获得用户基本信息
        suspend fun getUserInfo(token: String, uid: String) =
            Fuel.get("/users/$uid")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<User>())


        //获得用户动态
        suspend fun getUserPosts(
            token: String,
            uid: String,
            start: java.util.Date? = null,
            end: java.util.Date? = null
        ): Array<Post> {
            return Fuel.get("/users/$uid/posts?", listOf("start" to start?.toISOString(), "end" to end?.toISOString()))
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<Post>>())
        }


        //修改当前用户基本信息
        suspend fun modifyProfile (token: String, body: ModifyProfileRequest) =
            Fuel.put("/profile/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .awaitObject(gsonDeserializer<User>())


        // 获取当前用户基本信息
        suspend fun getProfile(token: String) =
            Fuel.get("/profile")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<User>())


        //注册用户
        suspend fun register(body: RegisterRequest) =
            Fuel.post("/auth/register")
                .jsonBody(body)
                .awaitObject(gsonDeserializer<Uid>())


        //登陆
        suspend fun login(body: LoginRequest) =
            Fuel.post("/auth/login")
                .jsonBody(body)
                .awaitObject(gsonDeserializer<LoginResponse>())


        //修改当前用户密码
        suspend fun changePassword(token: String, body: ChangePasswordRequest) {
            Fuel.put("/auth/change-password")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .awaitString()
        }


        //查看点赞人数目
        suspend fun getNumOfLikes(token: String, post_id: String) =
            Fuel.get("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<NumOfLikesResponse>())


        //给动态点赞
        suspend fun likeThisPost(token: String, post_id: String) =
            Fuel.post("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<NumOfLikesResponse>())


        //取消自己的点赞
        suspend fun unlikeThisPost(token: String, post_id: String) =
            Fuel.delete("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<NumOfLikesResponse>())


        //获得动态列表
        suspend fun getPosts(
            token: String,
            start: java.util.Date? = null,
            end: java.util.Date? = null,
            following: String? = null
        ) {
            Fuel.get("/posts/", listOf("start" to start?.toISOString(), "end" to end?.toISOString(), "following" to following))
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<Post>>())
        }


        //获得动态详细信息
        suspend fun getPostInfo(token: String, post_id: String) =
            Fuel.get("/posts/$post_id")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Post>())


        //发布动态
        suspend fun newPost(token: String, body: Post) =
            Fuel.post("/posts/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .awaitObject(gsonDeserializer<NewPostResponse>())


        //修改动态
        suspend fun modifyPost(token: String, post_id: String, body: ModifyPostRequest) =
            Fuel.put("/posts/$post_id")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .awaitObject(gsonDeserializer<Post>())


        //删除动态
        suspend fun deletePost(token: String, post_id: String) {
            Fuel.delete("/posts/$post_id")
                .authentication()
                .bearer(token)
                .awaitString()
        }

        enum class SortBy (val value: String) {
            NEWEST_FIRST("newest"),
            OLDEST_FIRST("oldest")
        }


        //动态评论区
        suspend fun getPostComments(
            token: String,
            post_id: String,
            skip: Int? = null,
            limit: Int? = null,
            sort_by: SortBy? = SortBy.NEWEST_FIRST
        ) =
            Fuel.get(
                "/posts/$post_id/comments/",
                listOf("skip" to skip, "limit" to limit, "sort_by" to sort_by?.value)
            )
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Array<Comment>>())


        //发布评论
        suspend fun newComment(token: String, postId: String, body: NewCommentRequest) =
            Fuel.post("/posts/$postId/comments/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .awaitObject(gsonDeserializer<NewCommentResponse>())


        //获得评论详细信息
        suspend fun getCommentInfo(token: String, post_id: String, comment_id: String) =
            Fuel.get("/posts/$post_id/comments/$comment_id")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<Comment>())


        //删除评论
        suspend fun deleteComment(token: String, post_id: String, comment_id: String) =
            Fuel.delete("/posts/$post_id/comments/$comment_id")
                .authentication()
                .bearer(token)
                .awaitString()


        //获得阿里云OSS上传的临时token
        suspend fun getUploadToken(token: String) =
            Fuel.post("/upload/token")
                .authentication()
                .bearer(token)
                .awaitObject(gsonDeserializer<UploadTokenResponse>())

    }
}

