package site.panda2134.thssforum.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.coroutines.await
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.models.Comment
import java.io.InputStream

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
                val avatar = Fuel.download(comment.user.avatar).await(object: ResponseDeserializable<Bitmap> {
                    override fun deserialize(inputStream: InputStream): Bitmap {
                        return BitmapFactory.decodeStream(inputStream)
                    }
                })
                holder.rootView.findViewById<ImageView>(R.id.commenter_pic).setImageBitmap(avatar)
                holder.rootView.findViewById<TextView>(R.id.commenter_name).text = comment.data.content
            }
        }
        // holder.rootView.setOnClickListener { ctx.loadPostDetailActivity(post) }
    }

    override fun onViewRecycled(holder: PostCommentItemViewHolder) {
        super.onViewRecycled(holder)
    }

    override fun getItemCount() = comments.size
}
