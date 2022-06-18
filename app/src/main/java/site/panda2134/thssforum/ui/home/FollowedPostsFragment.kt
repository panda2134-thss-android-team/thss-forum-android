package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.FragmentFollowedPostsBinding
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter

class FollowedPostsFragment: Fragment() {
    private lateinit var binding: FragmentFollowedPostsBinding
    private lateinit var api: APIWrapper
    private lateinit var adapter: PostListRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowedPostsBinding.inflate(inflater, container, false)

        // 把动态item中的每一项调用api填写
        api = APIWrapper(requireActivity())

        adapter = PostListRecyclerViewAdapter(api, fetchFollowing = true, activity = requireActivity(), lifecycleOwner = this)
        binding.followedPostsList.adapter = adapter
        adapter.setupRecyclerView(this.requireContext(), binding.followedPostsList)
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
        updateMenuIcon()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_menu_item -> {
                val intent = Intent(activity, HomeMenuSearch::class.java)
                startActivity(intent)
                true
            }
            R.id.seq_menu_item -> {
                isTimeSeq = !isTimeSeq
                updateMenuIcon()
                adapter.sortBy = if (isTimeSeq) APIWrapper.PostsSortBy.Time else APIWrapper.PostsSortBy.Like
                adapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMenuIcon() {
        menu.findItem(R.id.seq_menu_item).icon = (ContextCompat.getDrawable(
            requireActivity(),
            if (isTimeSeq) R.drawable.ic_baseline_access_time_24 else R.drawable.ic_baseline_thumb_up_24
        ))
    }
}