package site.panda2134.thssforum.ui.home

import android.os.Bundle
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.HomeSearchResultBinding
import site.panda2134.thssforum.models.PostType
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter
import site.panda2134.thssforum.ui.profile.ActivityProfileItem


class HomeSearchResult : ActivityProfileItem() {
    private lateinit var binding: HomeSearchResultBinding
    private lateinit var api: APIWrapper
    private lateinit var adapter: PostListRecyclerViewAdapter
    private lateinit var searchText : String
    private lateinit var searchNickname : String
    private var postType : PostType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)

        binding = HomeSearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchText = intent.getStringExtra("searchText") ?: ""
        searchNickname = intent.getStringExtra("searchNickname") ?: ""
        postType = intent.getIntExtra("postType", -1).let { if (it >= 0) PostType.values()[it] else null }

        binding.searchResultHint.text = getString(R.string.search_result_for, searchText)

        adapter = PostListRecyclerViewAdapter(api, activity = this, lifecycleOwner = this)
        adapter.searchText = searchText
        adapter.searchNickname = searchNickname
        adapter.types = postType?.let { listOf(it) } ?: PostType.values().toList()
        binding.hpPostsList.adapter = adapter
        adapter.setupRecyclerView(this, binding.hpPostsList)
    }
}