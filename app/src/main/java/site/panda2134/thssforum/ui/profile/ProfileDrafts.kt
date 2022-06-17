package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import androidx.core.view.isVisible
import site.panda2134.thssforum.databinding.ProfileDraftsBinding
import site.panda2134.thssforum.ui.post.ActivityNewPostWithDraft
import site.panda2134.thssforum.ui.profile.draft.DraftListRecyclerViewAdapter
import site.panda2134.thssforum.ui.profile.following.ProfileFollowingRecyclerViewAdapter

class ProfileDrafts : ActivityProfileItem() {
    private lateinit var binding: ProfileDraftsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileDraftsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drafts = ActivityNewPostWithDraft.getDraftList(this)
        if (drafts.isNotEmpty()) {
            val adapter = DraftListRecyclerViewAdapter(drafts.toMutableList(), binding.noContent)

            binding.draftList.adapter = adapter
            binding.noContent.isVisible = false
        }
    }
}