package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.FragmentAllPostsBinding
import site.panda2134.thssforum.databinding.FragmentFollowedPostsBinding
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter

class FollowedPostsFragment: Fragment() {
    private lateinit var binding: FragmentFollowedPostsBinding
    private lateinit var api: APIService
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowedPostsBinding.inflate(inflater, container, false)

        // 把动态item中的每一项调用api填写
        api = APIService(requireActivity())

        val adapter = PostListRecyclerViewAdapter(api, true)
        binding.followedPostsList.adapter = adapter
        adapter.setupRecyclerView(this.requireContext(), binding.followedPostsList)
        return binding.root
    }
}