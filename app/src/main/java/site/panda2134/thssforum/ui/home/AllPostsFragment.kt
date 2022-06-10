package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.FragmentAllPostsBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter

const val EXTRA_MESSAGE = "site.panda2134.thssforum.MESSAGE"

class AllPostsFragment : Fragment() {
    private val _menu = null
    private lateinit var api: APIService
    private lateinit var binding: FragmentAllPostsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        // 把动态item中的每一项调用api填写
        api = APIService(requireActivity())

        val recyclerLayoutManager = LinearLayoutManager(requireActivity())
        val adapter = PostListRecyclerViewAdapter(api, false)
        adapter.fetchMorePosts()
        binding.allPostsList.adapter = adapter
        binding.allPostsList.layoutManager = recyclerLayoutManager
        binding.allPostsList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerLayoutManager.findLastVisibleItemPosition() == (recyclerView.adapter?.itemCount ?: 0) - 1) { // last item
                    (recyclerView.adapter as PostListRecyclerViewAdapter).fetchMorePosts()
                }
            }
        })
        return binding.root
    }
}