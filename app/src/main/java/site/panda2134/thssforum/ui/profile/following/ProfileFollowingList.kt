package site.panda2134.thssforum.ui.profile.following

import android.os.Bundle
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileFollowingListBinding
import site.panda2134.thssforum.ui.profile.ActivityProfileItem

class ProfileFollowingList : ActivityProfileItem() {
    private lateinit var binding: ProfileFollowingListBinding
    private lateinit var api: APIWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        api = APIWrapper(this)
        super.onCreate(savedInstanceState)

        binding = ProfileFollowingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.followingList.visibility = View.GONE
        binding.noContent.visibility = View.GONE

        MainScope().launch {
            val following = api.getFollowingUsers()
            withContext(Dispatchers.Main) {
                binding.followingListLoading.visibility = View.GONE
                if (following.size > 0) {
                    binding.followingList.visibility = View.VISIBLE
                    binding.followingList.adapter = ProfileFollowingRecyclerViewAdapter(following)
                } else {
                    binding.noContent.visibility = View.VISIBLE
                }
            }
        }
    }
}