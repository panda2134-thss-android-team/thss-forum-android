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
    ): View {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        // 把动态item中的每一项调用api填写
        api = APIService(requireActivity())

        val adapter = PostListRecyclerViewAdapter(api, false)
        binding.allPostsList.adapter = adapter
        adapter.setupRecyclerView(this.requireContext(), binding.allPostsList)
        binding.root.setOnRefreshListener {
            adapter.refresh {
                binding.root.isRefreshing = false
            }
        }
        return binding.root
    }
}