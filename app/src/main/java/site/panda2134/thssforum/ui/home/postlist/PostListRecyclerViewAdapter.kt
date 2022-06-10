package site.panda2134.thssforum.ui.home.postlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.PostItemBinding
import site.panda2134.thssforum.databinding.RecyclerItemLoadingBinding
import site.panda2134.thssforum.models.Post
import site.panda2134.thssforum.ui.utils.RecyclerItemLoadingViewHolder
import java.time.Instant
import java.time.temporal.ChronoUnit

class PostListRecyclerViewAdapter(val api: APIService, val fetchFollowing: Boolean = false): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val POST_ITEM = 0
    private val LIST_LOADING = 1
    private val LIST_END = 2
    private var fetchTillInstant: Instant = Instant.now()
    private var ended: Boolean = false
    private val posts: ArrayList<Post> = arrayListOf()
    private val loadingLock = Mutex()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            POST_ITEM ->
                PostListRecyclerViewHolder(PostItemBinding.inflate(layoutInflater, parent, false), api)
            LIST_END ->
                RecyclerItemLoadingViewHolder(RecyclerItemLoadingBinding.inflate(layoutInflater, parent, false)).apply { setNoContent("只展示近14天动态") }
            LIST_LOADING ->
                RecyclerItemLoadingViewHolder(RecyclerItemLoadingBinding.inflate(layoutInflater, parent, false)).apply { setLoading() }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            POST_ITEM -> {
                if(holder !is PostListRecyclerViewHolder) return
                holder.setPost(posts[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == posts.size) {
            if (ended) LIST_END
            else LIST_LOADING
        }
        else POST_ITEM
    }

    fun fetchMorePosts () {

        val scope = MainScope()
        ended = (ChronoUnit.DAYS.between(fetchTillInstant, Instant.now()) > 7)
        if (!ended) {
            scope.launch(Dispatchers.IO) {
                loadingLock.withLock {
                    val postsToAdd =
                        api.getPosts(
                            end = fetchTillInstant,
                            following = fetchFollowing,
                            scope = scope
                        )
                    fetchTillInstant -= ChronoUnit.DAYS.duration // fetch 1 day each time
                    val insertedAt = posts.size
                    posts.addAll(postsToAdd)
                    withContext(Dispatchers.Main) {
                        notifyItemRangeInserted(insertedAt, postsToAdd.size)
                    }
                }
            }
        } else {
            MainScope().launch {
                notifyItemRemoved(posts.size)
                notifyItemInserted(posts.size)
            }
        }
    }
}