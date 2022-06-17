package site.panda2134.thssforum.ui.home.postlist

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostItemBinding
import site.panda2134.thssforum.databinding.RecyclerItemLoadingBinding
import site.panda2134.thssforum.models.Post
import site.panda2134.thssforum.ui.home.comments.CommentRecyclerViewAdapter
import site.panda2134.thssforum.ui.utils.RecyclerItemLoadingViewHolder
import java.lang.Integer.min

class UserHomepageRecyclerViewAdapter(val api: APIWrapper, val user_id: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val POST_ITEM = 0
    private val LIST_LOADING = 1
    private val LIST_END = 2
    private var recyclerView: RecyclerView? = null
    private val posts: ArrayList<Post> = arrayListOf()
    private val loadingLock = Mutex()
    private var isEnded = false
    private val POSTS_PER_FETCH = 5

    var sortBy: APIWrapper.PostsSortBy = APIWrapper.PostsSortBy.Time

    fun refresh(finishCallback: ()->Unit = {}) {
        val removeCount = posts.size
        posts.clear()
        notifyItemRangeRemoved(0, removeCount) // list-end!
        isEnded = false
        fetchMorePosts(finishCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            POST_ITEM -> {
                val binding =
                    PostItemBinding.inflate(layoutInflater, parent, false)
                val commentAdapter = CommentRecyclerViewAdapter(api)
                binding.commentView.adapter = commentAdapter
                binding.commentView.layoutManager = LinearLayoutManager(parent.context)
                commentAdapter.fetchComments()
                PostListRecyclerViewHolder(binding, api).apply {
                    onDeleteCallback = { _, bindingAdapterPosition ->
                        posts.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                    }
                }
            }
            in listOf(LIST_END, LIST_LOADING) ->
                RecyclerItemLoadingViewHolder(RecyclerItemLoadingBinding.inflate(layoutInflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            POST_ITEM -> {
                if(holder !is PostListRecyclerViewHolder) return
                holder.setPost(posts[position])
            }
            LIST_END -> {
                if(holder !is RecyclerItemLoadingViewHolder) return
                holder.setNoContent()
            }
            LIST_LOADING -> {
                if(holder !is RecyclerItemLoadingViewHolder) return
                holder.setLoading()
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is PostListRecyclerViewHolder) {
            holder.binding.audioPlayer.pause()
            holder.binding.videoPlayer.pause()
        }
    }

    override fun getItemCount() = posts.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == posts.size) {
            if (isEnded) LIST_END
            else LIST_LOADING
        }
        else POST_ITEM
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchMorePosts (finishCallback: ()->Unit = {}) {
        val scope = MainScope()
        if (!isEnded) {
            scope.launch(Dispatchers.IO) {
                loadingLock.withLock {
                    Log.d("PostLoad", "loading...")
                    val initial = posts.size == 0
                    var postsToAdd: List<Post> = api.getUserPosts( // +1 is necessary!
                        uid = user_id
                    )
                    val insertedAt = posts.size
                    if (postsToAdd.size <= POSTS_PER_FETCH) {
                        isEnded = true
                    }
                    postsToAdd = postsToAdd.subList(0, min(POSTS_PER_FETCH, postsToAdd.size))
                    posts.addAll(insertedAt, postsToAdd)
                    withContext(Dispatchers.Main) {
                        if (initial) { // initial
                            notifyDataSetChanged()
                        } else {
                            notifyItemRangeInserted(insertedAt, postsToAdd.size)
                        }
                        if (isEnded) {
                            notifyItemChanged(posts.size)
                        }
                    }
                }
                withContext(Dispatchers.IO) {
                    finishCallback()
                }
            }
        } else {
            notifyItemChanged(posts.size)
            finishCallback()
        }
    }

    fun setupRecyclerView (context: Context, recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.adapter = this
        recyclerView.layoutManager = LinearLayoutManager(context)

        registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    recyclerView.layoutManager!!.scrollToPosition(0)
                }
            }
        })
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            private var isLoading = false
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading) {
                    if ((recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == (recyclerView.adapter?.itemCount
                            ?: 0) - 1
                    ) { // last item
                        isLoading = true
                        this@UserHomepageRecyclerViewAdapter.fetchMorePosts {
                            isLoading = false
                        }
                    }
                }
                for (index in 0..itemCount) {
                    val holder = recyclerView.findViewHolderForAdapterPosition(index) ?: continue
                    if (holder !is PostListRecyclerViewHolder) continue
                    holder.mediaController?.hide()
                }
            }
        })
    }
}