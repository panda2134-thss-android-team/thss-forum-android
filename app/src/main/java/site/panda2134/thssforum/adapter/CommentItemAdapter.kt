package site.panda2134.thssforum.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.models.Comment

class CommentItemAdapter(private val comments: List<Comment>):
    RecyclerView.Adapter<CommentItemAdapter.PostCommentItemViewHolder>() {
    class PostCommentItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val rootView: LinearLayout = view.findViewById(R.id.comment_item)
        val mainScope = MainScope() + CoroutineName("PostCommentItemViewHolder")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCommentItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_comment_item, parent, false)
        return PostCommentItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: PostCommentItemViewHolder, position: Int) {
        val comment = comments[position]
        with (holder) {
            mainScope.launch {
                val avatar = downloadImage(comment.user.avatar)
                holder.rootView.findViewById<ImageView>(R.id.commenter_pic).setImageBitmap(avatar)
                holder.rootView.findViewById<TextView>(R.id.following_nickname).text = comment.data.content
            }
        }
        // holder.rootView.setOnClickListener { ctx.loadPostDetailActivity(post) }
    }

    override fun onViewRecycled(holder: PostCommentItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemCount() = comments.size
}
