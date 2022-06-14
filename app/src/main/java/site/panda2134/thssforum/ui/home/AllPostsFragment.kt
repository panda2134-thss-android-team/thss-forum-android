package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.FragmentAllPostsBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter


class AllPostsFragment : Fragment() {
    private lateinit var api: APIService
    private lateinit var binding: FragmentAllPostsBinding
    private lateinit var adapter: PostListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        // 把动态item中的每一项调用api填写
        api = APIService(requireActivity())

        adapter = PostListRecyclerViewAdapter(api, false)
        binding.allPostsList.adapter = adapter
        adapter.setupRecyclerView(this.requireContext(), binding.allPostsList)
        binding.root.setOnRefreshListener {
            adapter.refresh {
                binding.root.isRefreshing = false
            }
        }
        setHasOptionsMenu(true)
        return binding.root
    }

    private var isTimeSeq = true // 右上角的展示顺序：默认是时间顺序
    private lateinit var menu: Menu

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discover_searchswitch_menuicon, menu)
        this.menu = menu
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_menu_item -> {
                val intent = Intent(activity, DiscoverMenuSearch::class.java)
                startActivity(intent)
                true
            }
            R.id.seq_menu_item -> {
                isTimeSeq = !isTimeSeq
                menu.findItem(R.id.seq_menu_item).icon = (ContextCompat.getDrawable(requireActivity(),
                    if (isTimeSeq) R.drawable.ic_baseline_access_time_24 else R.drawable.ic_baseline_thumb_up_24))
                adapter.sortBy = if (isTimeSeq) APIService.PostsSortBy.Time else APIService.PostsSortBy.Like
                adapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}