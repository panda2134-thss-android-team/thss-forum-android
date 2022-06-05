package site.panda2134.thssforum.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        val drafts: View = binding.drafts
        val followingList: View = binding.followingList
        val editMyProfile: View = binding.editMyProfile
        // set jump
        drafts.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileDrafts)
        }
        followingList.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileFollowingList)
        }
        editMyProfile.setOnClickListener {
            requireView().findNavController().navigate(R.id.action_navigation_profile_to_profileEditMyProfile)
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
                val intent = Intent(activity, ProfileNotify::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}