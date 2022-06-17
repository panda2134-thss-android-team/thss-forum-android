package site.panda2134.thssforum.ui.home.comments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.core.FuelError
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

class CommentRecyclerViewHolder(val binding: PostCommentItemBinding,
                                private val api: APIWrapper): RecyclerView.ViewHolder(binding.root) {
    private lateinit var by: User
    private var replyTo: User? = null
    var onDeleteCallback: ((postId: String, comment: Comment, bindingPosition: Int)->Unit)? = null

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
                    // parnet comment can be removed; check for this!
                    try {
                        val parentComment = api.getCommentInfo(postId, parentCommentId)
                        withContext(Dispatchers.Main) {
                            replyTo = parentComment.user
                            binding.nicknameReplyTo.text = parentComment.user.nickname
                        }
                    } catch (e: FuelError) {
                        if (e.response.statusCode != 404) throw e
                        else {
                            withContext(Dispatchers.Main) {
                                replyTo = null
                                binding.nicknameReplyTo.text = binding.root.context.getString(R.string.unknown)
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
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
        binding.root.setOnLongClickListener {
            PopupMenu(binding.root.context, binding.root).apply {
                menuInflater.inflate(R.menu.comment_context_menu, menu)
                if (api.currentUserId != comment.user.uid) {
                    menu.findItem(R.id.delete).isVisible = false
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.copy_to_clipboard -> {
                            val clipboard = getSystemService(binding.root.context, ClipboardManager::class.java)
                            val clip = ClipData.newPlainText("THSS_FORUM_COMMENT", comment.data.content)
                            clipboard?.setPrimaryClip(clip)
                            true
                        }
                        R.id.delete -> {
                            MainScope().launch(Dispatchers.IO) {
                                try {
                                    api.deleteComment(postId, comment.data.id)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(binding.root.context, R.string.delete_success, Toast.LENGTH_SHORT).show()
                                        onDeleteCallback?.invoke(postId, comment, bindingAdapterPosition)
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            }.show()
            true
        }
    }
}