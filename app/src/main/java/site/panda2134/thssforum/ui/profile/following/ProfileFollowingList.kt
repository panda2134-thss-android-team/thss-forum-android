package site.panda2134.thssforum.ui.profile.following

import android.os.Bundle
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.databinding.ProfileFollowingListBinding
import site.panda2134.thssforum.ui.profile.ActivityProfileItem

class ProfileFollowingList : ActivityProfileItem() {
    private lateinit var binding: ProfileFollowingListBinding
    private val api = APIService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileFollowingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.followingList.visibility = View.GONE

        MainScope().launch {
            val following = api.getFollowingUsers()
            withContext(Dispatchers.Main) {
                binding.followingListLoading.visibility = View.GONE
                binding.followingList.visibility = View.VISIBLE
                binding.followingList.adapter = ProfileFollowingRecyclerAdapter(following)
            }
        }
    }
}