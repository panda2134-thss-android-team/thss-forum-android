package site.panda2134.thssforum.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.PostCommentItemBinding
import site.panda2134.thssforum.models.Comment

class CommentItemAdapter(private val comments: List<Comment>):
    RecyclerView.Adapter<CommentItemAdapter.PostCommentItemViewHolder>() {
    class PostCommentItemViewHolder(val binding: PostCommentItemBinding): RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCommentItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PostCommentItemViewHolder(PostCommentItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PostCommentItemViewHolder, position: Int) {
        val comment = comments[position]
        with (holder) {
            binding.commentContent.text = comment.data.content
            Glide.with(binding.root).load(comment.user.avatar)
                .placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.commenterAvatar)
        }
        // holder.rootView.setOnClickListener { ctx.loadPostDetailActivity(post) }
    }

    override fun getItemCount() = comments.size
}
