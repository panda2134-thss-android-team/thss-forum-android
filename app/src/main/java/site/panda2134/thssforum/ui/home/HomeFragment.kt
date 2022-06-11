package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.data.CommentItemDataSource
import site.panda2134.thssforum.databinding.FragmentHomeBinding
import site.panda2134.thssforum.models.CommentResponse
import site.panda2134.thssforum.models.User


class HomeFragment : Fragment() {
    private lateinit var tabAdapter: TabAdapter
    private lateinit var binding: FragmentHomeBinding

    // CommentItem的// TODO:之后删
    private val hasNext = true
    private val dataSource = CommentItemDataSource()
    private lateinit var dataset: MutableList<CommentResponse>
    // CommentItem的

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var bitmap: Bitmap? = null

    private var is_time_seq = true // 右上角的展示顺序：默认是时间顺序
    private var menu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val viewModel =
//            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 载入顶部：我的头像、昵称和简介
        val user: User
        val apiService = APIService(requireActivity())
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(apiService)
        }

        return binding.root
    }

    private suspend fun loadUserInfo(apiService: APIService) {
        try {
            val user: User = apiService.getProfile()
            withContext(Dispatchers.Main) {
                binding.myName.text = user.nickname
                binding.myMotto.text = user.intro
                Glide.with(requireActivity()).load(user.avatar).placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.myAvatar)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabAdapter = TabAdapter(this)
        binding.pager.adapter = tabAdapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> context?.getString(R.string.all_posts)
                1 -> context?.getString(R.string.followed_posts)
                else -> throw IllegalArgumentException("unknown tab! position can only be 0 or 1")
            }
        }.attach()
    }

    // 设置要不要显示menubar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    // 在顶栏加图标（因为是fragment所以写法不同）
    // 之后写点击事件的时候，直接对应重载就可以了
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
                if(is_time_seq) {
                    is_time_seq = false
                    menu?.getItem(1)?.icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_thumb_up_24);
                }
                else {
                    is_time_seq = true
                    menu?.getItem(1)?.icon = (ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_access_time_24));
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}