package site.panda2134.thssforum.ui.home.comments

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostCommentItemBinding
import site.panda2134.thssforum.models.Comment

class CommentRecyclerViewHolder(private val binding: PostCommentItemBinding,
                                private val api: APIWrapper): RecyclerView.ViewHolder(binding.root) {
    fun bindComment(postId: String, comment: Comment) {
        comment.run {
            Glide.with(binding.root).load(user.avatar)
                .placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.commenterAvatar)
            binding.nickname.text = user.nickname
            binding.commentContent.text = data.content

            val parentCommentId = comment.data.parentCommentId
            if (parentCommentId == null) {
                binding.replyWrapper.visibility = View.GONE
            } else {
                binding.replyWrapper.visibility = View.VISIBLE
                MainScope().launch(Dispatchers.IO) {
                    val parentComment = api.getCommentInfo(postId, parentCommentId)
                    withContext(Dispatchers.Main) {
                        binding.nicknameReplyTo.text = parentComment.user.avatar
                    }
                }
            }
        }
    }
}