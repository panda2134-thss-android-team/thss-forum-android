package site.panda2134.thssforum

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseResultHandler
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import site.panda2134.thssforum.models.*

class APIService {
    companion object {
        private const val baseUrl = "https://lab.panda2134.site:20443"

        // 获取当前用户关注了哪些人
        fun getFollowingUsers (token:String, handler: ResponseResultHandler<Array<User>>) {
            val path = "$baseUrl/profile/following/"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获取当前用户被谁关注
        fun getFollowers (token:String, handler:ResponseResultHandler<Array<User>>) {
            val path = "$baseUrl/profile/followers/"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //增加当前用户关注的人
        fun followUser (token:String, body: Uid, handler: ResponseResultHandler<Uid>) {
            val path = "$baseUrl/profile/following/"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //当前用户取消关注
        fun unfollowUser (token:String, uid:String, handler: ResponseResultHandler<Empty>) {
            val path = "$baseUrl/profile/following/$uid"
            Fuel.delete(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得当前用户黑名单
        fun getBlacklist (token: String, handler: ResponseResultHandler<Array<User>>) {
            val path = "$baseUrl/profile/blacklist/"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //新增黑名单用户
        fun addBlacklistUser (token: String, body: Uid, handler:ResponseResultHandler<Uid>) {
            val path = "$baseUrl/profile/blacklist/"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //删除黑名单用户
        fun delBlacklistUser (token: String, uid:String, handler: ResponseResultHandler<Empty>) {
            val path = "$baseUrl/profile/blacklist/$uid"
            Fuel.delete(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得用户基本信息
        fun getUserInfo (token: String, uid: String, handler: ResponseResultHandler<User>) {
            val path = "$baseUrl/users/$uid"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得用户动态
        fun getUserPosts (token: String, uid: String, start: String? = null, end: String? = null, handler: ResponseResultHandler<Array<Post>>) {
            var path = "$baseUrl/users/$uid/posts?"
            start?.let { path = "${path}start=$start&" }
            end?.let { path = "${path}end=$end" }
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //修改当前用户基本信息
        fun modifyMyInfo (token: String, body: InlineObject5, handler: ResponseResultHandler<User>) {
            val path = "$baseUrl/profile/"
            Fuel.put(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        // 获取当前用户基本信息
        fun getMyInfo (token:String, handler: ResponseResultHandler<User>) {
            val path = "$baseUrl/profile"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //注册用户
        fun register (body: InlineObject7, handler: ResponseResultHandler<Uid>) {
            val path = "$baseUrl/auth/register"
            Fuel.post(path)
                .jsonBody(body)
                .responseObject(handler)
        }

        //登陆
        fun login (body: LoginInfo, handler: ResponseResultHandler<InlineResponse200>) {
            val path = "$baseUrl/auth/login"
            Fuel.post(path)
                .jsonBody(body)
                .responseObject(handler)
        }

        //修改当前用户密码
        fun changeMyPassword (token: String, body: InlineObject4, handler: ResponseResultHandler<Empty>) {
            val path = "$baseUrl/auth/change-password"
            Fuel.put(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //查看点赞人数目
        fun getNumOfLikes (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) {
            val path = "$baseUrl/posts/$post_id/like"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //给动态点赞
        fun likeThisPost (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) {
            val path = "$baseUrl/posts/$post_id/like"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //取消自己的点赞
        fun unlikeThisPost (token: String, post_id: String, handler: ResponseResultHandler<InlineResponse2011>) {
            val path = "$baseUrl/posts/$post_id/like"
            Fuel.delete(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得动态列表
        fun getPosts (token: String, start: String? = null, end: String? = null, following: String? = null, handler: ResponseResultHandler<Array<Post>>) {
            var path = "$baseUrl/posts/?"
            start?.let { path  = "${path}start=$start&"}
            end?.let { path = "${path}end=$end&" }
            following?.let { path = "${path}following=$following" }
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得动态详细信息
        fun getPostInfo (token: String, post_id: String, handler: ResponseResultHandler<Post>) {
            val path = "$baseUrl/posts/$post_id"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //发布动态
        fun newPost (token: String, body: Post, handler: ResponseResultHandler<InlineResponse2012>) {
            val path = "$baseUrl/posts/"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //修改动态
        fun modifyPost (token: String, post_id: String, body: InlineObject2, handler: ResponseResultHandler<Post>) {
            val path = "$baseUrl/posts/$post_id"
            Fuel.put(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //删除动态
        fun deletePost (token: String, post_id: String, handler: ResponseResultHandler<Empty>) {
            val path = "$baseUrl/posts/$post_id"
            Fuel.delete(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //动态评论区
        fun getPostComments (token: String, post_id: String, skip: Int? = null, limit: Int? = null, sort_by: String? = null, handler: ResponseResultHandler<Array<Comment>>) {
            var path = "$baseUrl/posts/$post_id/comments/?"
            skip?.let { path = "${path}skip=$skip&" }
            limit?.let { path = "${path}limit=$limit&" }
            sort_by?.let { path = "${path}sort_by=$sort_by" }
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //发布评论
        fun newComment (token: String, post_id: String, body: InlineObject6, handler: ResponseResultHandler<InlineResponse2014>) {
            val path = "$baseUrl/posts/$post_id/comments/"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .jsonBody(body)
                .responseObject(handler)
        }

        //获得评论详细信息
        fun getCommentInfo (token: String, post_id: String, comment_id: String, handler: ResponseResultHandler<Comment>) {
            val path = "$baseUrl/posts/$post_id/comments/$comment_id"
            Fuel.get(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //删除评论
        fun deleteComment (token: String, post_id: String, comment_id: String, handler: ResponseResultHandler<Empty>) {
            val path = "$baseUrl/posts/$post_id/comments/$comment_id"
            Fuel.delete(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }

        //获得阿里云OSS上传的临时token
        fun getUploadToken (token: String, handler: ResponseResultHandler<InlineResponse2001>) {
            val path = "$baseUrl/upload/token"
            Fuel.post(path)
                .authentication()
                .bearer(token)
                .responseObject(handler)
        }
    }
}

