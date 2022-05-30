package site.panda2134.thssforum

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseResultHandler
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import site.panda2134.thssforum.models.*

class APIService {
    companion object {
        init {
            FuelManager.instance.basePath = "https://lab.panda2134.site:20443"
        }

        // 获取当前用户关注了哪些人
        fun getFollowingUsers (token:String, handler: ResponseResultHandler<Array<User>>) =
            Fuel.get("/profile/following/")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获取当前用户被谁关注
        fun getFollowers (token:String, handler:ResponseResultHandler<Array<User>>) =
            Fuel.get("/profile/followers/")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //增加当前用户关注的人
        fun followUser (token:String, body: Uid, handler: ResponseResultHandler<Uid>) =
            Fuel.post("/profile/following/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //当前用户取消关注
        fun unfollowUser (token:String, uid:String, handler: ResponseResultHandler<Empty>) =
            Fuel.delete("/profile/following/$uid")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得当前用户黑名单
        fun getBlacklist (token: String, handler: ResponseResultHandler<Array<User>>) =
            Fuel.get("/profile/blacklist/")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //新增黑名单用户
        fun addBlacklistUser (token: String, body: Uid, handler:ResponseResultHandler<Uid>) =
            Fuel.post("/profile/blacklist/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //删除黑名单用户
        fun delBlacklistUser (token: String, uid:String, handler: ResponseResultHandler<Empty>) =
            Fuel.delete("/profile/blacklist/$uid")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得用户基本信息
        fun getUserInfo (token: String, uid: String, handler: ResponseResultHandler<User>) =
            Fuel.get("/users/$uid")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得用户动态
        fun getUserPosts (token: String, uid: String, start: String? = null, end: String? = null, handler: ResponseResultHandler<Array<Post>>) =
            Fuel.get("/users/$uid/posts?", listOf("start" to start, "end" to end))
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //修改当前用户基本信息
        fun modifyMyInfo (token: String, body: InlineObject5, handler: ResponseResultHandler<User>) =
            Fuel.put("/profile/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        // 获取当前用户基本信息
        fun getMyInfo (token:String, handler: ResponseResultHandler<User>) =
            Fuel.get("/profile")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //注册用户
        fun register (body: InlineObject7, handler: ResponseResultHandler<Uid>) =
            Fuel.post("/auth/register")
                .jsonBody(body)
                .responseObject(handler)


        //登陆
        fun login (body: LoginInfo, handler: ResponseResultHandler<InlineResponse200>) =
            Fuel.post("/auth/login")
                .jsonBody(body)
                .responseObject(handler)


        //修改当前用户密码
        fun changeMyPassword (token: String, body: InlineObject4, handler: ResponseResultHandler<Empty>) =
            Fuel.put("/auth/change-password")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //查看点赞人数目
        fun getNumOfLikes (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) =
            Fuel.get("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //给动态点赞
        fun likeThisPost (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) =
            Fuel.post("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //取消自己的点赞
        fun unlikeThisPost (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) =
            Fuel.delete("/posts/$post_id/like")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得动态列表
        fun getPosts (token: String, start: String? = null, end: String? = null, following: String? = null, handler: ResponseResultHandler<Array<Post>>) =
            Fuel.get("/posts/?", listOf("start" to start, "end" to end, "following" to following))
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得动态详细信息
        fun getPostInfo (token: String, post_id: String, handler: ResponseResultHandler<Post>) =
            Fuel.get("/posts/$post_id")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //发布动态
        fun newPost (token: String, body: Post, handler: ResponseResultHandler<InlineResponse2012>) =
            Fuel.post("/posts/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //修改动态
        fun modifyPost (token: String, post_id: String, body: InlineObject2, handler: ResponseResultHandler<Post>) =
            Fuel.put("/posts/$post_id")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //删除动态
        fun deletePost (token: String, post_id: String, handler: ResponseResultHandler<Empty>) =
            Fuel.delete("/posts/$post_id")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //动态评论区
        fun getPostComments (token: String, post_id: String, skip: Int? = null, limit: Int? = null, sort_by: String? = null, handler: ResponseResultHandler<Array<Comment>>) =
            Fuel.get("/posts/$post_id/comments/", listOf("skip" to skip, "limit" to limit, "sort_by" to sort_by))
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //发布评论
        fun newComment (token: String, post_id: String, body: InlineObject6, handler: ResponseResultHandler<InlineResponse2014>) =
            Fuel.post("/posts/$post_id/comments/")
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)


        //获得评论详细信息
        fun getCommentInfo (token: String, post_id: String, comment_id: String, handler: ResponseResultHandler<Comment>) =
            Fuel.get("/posts/$post_id/comments/$comment_id")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //删除评论
        fun deleteComment (token: String, post_id: String, comment_id: String, handler: ResponseResultHandler<Empty>) =
            Fuel.delete("/posts/$post_id/comments/$comment_id")
                .authentication()
                .bearer(token)
                .responseObject(handler)


        //获得阿里云OSS上传的临时token
        fun getUploadToken (token: String, handler: ResponseResultHandler<InlineResponse2001>) =
            Fuel.post("/upload/token")
                .authentication()
                .bearer(token)
                .responseObject(handler)

    }
}

