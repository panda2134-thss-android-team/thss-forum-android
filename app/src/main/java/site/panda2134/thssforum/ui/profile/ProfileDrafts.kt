package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import androidx.core.view.isVisible
import site.panda2134.thssforum.databinding.ProfileDraftsBinding
import site.panda2134.thssforum.ui.post.ActivityNewPostWithDraft
import site.panda2134.thssforum.ui.profile.draft.DraftListRecyclerViewAdapter
import site.panda2134.thssforum.ui.profile.following.ProfileFollowingRecyclerViewAdapter

class ProfileDrafts : ActivityProfileItem() {
    private lateinit var binding: ProfileDraftsBinding
    private lateinit var adapter: DraftListRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileDraftsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DraftListRecyclerViewAdapter(mutableListOf(), binding.noContent)
        binding.draftList.adapter = adapter

        refresh()
    }

    private fun refresh() {
        val drafts = ActivityNewPostWithDraft.getDraftList(this)
        val removeCount = adapter.dataset.size

        adapter.dataset.clear()
        adapter.notifyItemRangeRemoved(0, removeCount)
        adapter.dataset.addAll(drafts)
        adapter.notifyItemRangeInserted(0, drafts.size)

        binding.noContent.isVisible = drafts.isEmpty()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}