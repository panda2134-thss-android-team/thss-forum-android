package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.HomeSearchPageBinding
import site.panda2134.thssforum.databinding.HomeSearchResultBinding
import site.panda2134.thssforum.databinding.ProfileUserHomepageBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter
import site.panda2134.thssforum.ui.profile.ActivityProfileItem


class HomeSearchResult : ActivityProfileItem() {
    private lateinit var binding: HomeSearchResultBinding
    private lateinit var uid : String // 本界面展示的作者的uid
    private lateinit var api: APIWrapper
    private lateinit var adapter: PostListRecyclerViewAdapter
    private lateinit var title : String
    private lateinit var user_name : String
    private lateinit var types : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("home_search_result")

        binding = HomeSearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getSerializableExtra("uid") as String
        title = intent.getSerializableExtra("title") as String
        user_name = intent.getSerializableExtra("user_name") as String
        types = intent.getSerializableExtra("types") as String

        // TODO:这里的过滤还没有加
        adapter = PostListRecyclerViewAdapter(api, uid = uid, activity = this, lifecycleOwner = this)
        binding.hpPostsList.adapter = adapter
        adapter.setupRecyclerView(this, binding.hpPostsList)
    }


}