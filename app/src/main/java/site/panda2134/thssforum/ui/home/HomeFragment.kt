package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.FragmentHomeBinding
import site.panda2134.thssforum.models.User


class HomeFragment : Fragment() {
    private lateinit var tabAdapter: HomeTabAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var api: APIWrapper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 载入顶部：我的头像、昵称和简介
        api = APIWrapper(requireActivity())
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo()
        }

        return binding.root
    }

    private suspend fun loadUserInfo() {
        try {
            val user: User = api.getProfile()
            withContext(Dispatchers.Main) {
                binding.myName.text = user.nickname
                binding.myMotto.text = user.intro
            }
            // 画图
            withContext(Dispatchers.Main) {
                Glide.with(binding.root).load(user.avatar)
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(binding.myAvatar)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabAdapter = HomeTabAdapter(this)
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
}