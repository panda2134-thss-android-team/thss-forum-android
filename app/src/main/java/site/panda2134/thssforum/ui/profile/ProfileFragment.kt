package site.panda2134.thssforum.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.FragmentProfileBinding
import site.panda2134.thssforum.models.User


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private lateinit var api: APIWrapper

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private suspend fun loadUserInfo() {
        try {
            val user: User = api.getProfile()
            withContext(Dispatchers.Main) {
                binding.name.text = user.nickname
                binding.motto.text = user.intro
                Glide.with(binding.root).load(user.avatar)
                    .placeholder(R.drawable.ic_baseline_account_circle_24).into(binding.image)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun gotoMyHomepage() {
        val intent = Intent(binding.root.context, ProfileUserHomepage::class.java)
        .putExtra("author", api.currentUserId).putExtra("is_current_user", true)
        binding.root.context.startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val viewModel =
//            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textProfile
//        viewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        // set jump
        binding.myHomepage.setOnClickListener {
            api = APIWrapper(requireActivity())
            MainScope().launch(Dispatchers.IO) {
                gotoMyHomepage()
            }
        }
        binding.drafts.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileDrafts)
        }
        binding.followingList.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileFollowingList)
        }
        binding.editMyProfile.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileEditMyProfile)
        }
        binding.editMyPassword.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileChangePassword)
        }
        binding.logout.setOnClickListener {
            api.logout()
        }


        // 载入顶部：我的头像、昵称和简介
        api = APIWrapper(requireActivity())
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo()
        }


        return root
    }

    // 设置要不要显示menubar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    // 在顶栏加图标（因为是fragment所以写法不同）
    // 之后写点击事件的时候，直接对应重载就可以了
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.bell_menu -> {
                val intent = Intent(activity, ProfileNotificationList::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        MainScope().launch {
            loadUserInfo()
        }
    }
}