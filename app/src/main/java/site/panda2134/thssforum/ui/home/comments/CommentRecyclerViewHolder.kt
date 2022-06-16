package site.panda2134.thssforum.ui.home.comments

import android.content.Intent
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
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.profile.ProfileUserHomepage

class CommentRecyclerViewHolder(private val binding: PostCommentItemBinding,
                                private val api: APIWrapper): RecyclerView.ViewHolder(binding.root) {
    private lateinit var by: User
    private var replyTo: User? = null

    fun bindComment(postId: String, comment: Comment) {
        comment.run {
            Glide.with(binding.root).load(user.avatar)
                .placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.commenterAvatar)
            binding.nickname.text = user.nickname
            by = user
            replyTo = null
            binding.commentContent.text = data.content

            val parentCommentId = comment.data.parentCommentId
            if (parentCommentId == null) {
                binding.replyWrapper.visibility = View.GONE
            } else {
                binding.replyWrapper.visibility = View.VISIBLE
                MainScope().launch(Dispatchers.IO) {
                    val parentComment = api.getCommentInfo(postId, parentCommentId)
                    withContext(Dispatchers.Main) {
                        replyTo = parentComment.user
                        binding.nicknameReplyTo.text = parentComment.user.avatar
                    }
                }
            }
        }

        val gotoCommenterPage = { _: View ->
            val intent = Intent(binding.root.context, ProfileUserHomepage::class.java)
                .putExtra("author", by.uid)
            binding.root.context.startActivity(intent)
        }
        binding.nickname.setOnClickListener(gotoCommenterPage)
        binding.commenterAvatar.setOnClickListener(gotoCommenterPage)
        binding.nicknameReplyTo.setOnClickListener {
            replyTo?.run {
                val intent = Intent(binding.root.context, ProfileUserHomepage::class.java)
                    .putExtra("author", uid)
                binding.root.context.startActivity(intent)
            }
        }
    }
}