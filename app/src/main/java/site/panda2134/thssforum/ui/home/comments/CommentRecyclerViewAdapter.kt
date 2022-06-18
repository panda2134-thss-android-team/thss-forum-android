package site.panda2134.thssforum.ui.home.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostCommentItemBinding
import site.panda2134.thssforum.models.Comment

class CommentRecyclerViewAdapter(private val api: APIWrapper): RecyclerView.Adapter<CommentRecyclerViewHolder>() {
    private val dataset = arrayListOf<Comment>()
    private val COMMENTS_PER_LOAD = 1000000
    private var isEnded = false
    var postId: String? = null
    var commentClickedHandler: ((Comment, View)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PostCommentItemBinding.inflate(layoutInflater, parent, false)
        return CommentRecyclerViewHolder(binding, api).apply {
            onDeleteCallback = { _, _, bindingIndex ->
                dataset.removeAt(bindingIndex)
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: CommentRecyclerViewHolder, position: Int) {
        postId?.let {
            holder.bindComment(it, dataset[position])
            holder.binding.root.setOnClickListener {
                commentClickedHandler?.apply {
                    invoke(dataset[position], it)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun clear() {
        val removeCount = dataset.size
        dataset.clear()
        notifyDataSetChanged()
        isEnded = false
    }

    fun fetchComments(finishCallback: () -> Unit = {}) {
        if (isEnded) return
        val postIdLocal = postId ?: return
        MainScope().launch(Dispatchers.IO) {
            val newComments = api.getPostComments(postIdLocal, skip = dataset.size, limit = COMMENTS_PER_LOAD + 1,
                sortBy = APIWrapper.CommentSortBy.OLDEST_FIRST)
            if (newComments.size < COMMENTS_PER_LOAD + 1) {
                isEnded = true
            }
            val insertAt = dataset.size
            dataset.addAll(insertAt, newComments)
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
                finishCallback()
            }
        }
    }
}