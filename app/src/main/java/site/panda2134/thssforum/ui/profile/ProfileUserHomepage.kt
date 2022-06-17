package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileUserHomepageBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter


class ProfileUserHomepage : ActivityProfileItem() {
    private lateinit var binding: ProfileUserHomepageBinding
    private var isView = true // 右上角的屏蔽与否：默认是不屏蔽
    private lateinit var menu: Menu
    private lateinit var uid : String // 本界面展示的作者的uid
    private lateinit var api: APIWrapper
    private lateinit var adapter: PostListRecyclerViewAdapter
    private var isCurrentUser = false // 如果是当前用户的话就没有 ”屏蔽与否/关注“，默认不是

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_user_homepage")

        binding = ProfileUserHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getSerializableExtra("author") as String
        // 载入顶部：那个用户的主页
        api = APIWrapper(this)
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(uid)
        }

        isCurrentUser = (uid == api.currentUserId)
        binding.followedButton.visibility = if (isCurrentUser) View.GONE else View.VISIBLE

        adapter = PostListRecyclerViewAdapter(api, uid = uid, activity = this, lifecycleOwner = this)
        binding.hpPostsList.adapter = adapter
        adapter.setupRecyclerView(this, binding.hpPostsList)

        binding.followedButton.setOnClickListener() {

        }
    }

    private suspend fun loadUserInfo(uid : String) {
        try {
            val user: User = api.getUserInfo(uid)
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

    // 不去看该用户内容
    private suspend fun notView(uid : String) {
        try {
            api.addBlacklistUser(uid)
            println("完成恢复白名单啦")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    // 看该用户内容
    private suspend fun viewAgain(uid : String) {
        try {
            api.delBlacklistUser(uid)
            println("完成加入黑名单啦")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    // activity的menubar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        if(!isCurrentUser) { // 如果不是当前用户
            inflater.inflate(R.menu.following_eyeswitch_menuicon, menu)
            this.menu = menu
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.eye_menu_item -> {
                if(isView) {
                    isView = false
                    menu.getItem(0).icon = ContextCompat.getDrawable(this, R.drawable.eye_off)
                    MainScope().launch(Dispatchers.IO) {
                        viewAgain(uid)
                    }

                }
                else {
                    isView = true
                    menu.getItem(0).icon = (ContextCompat.getDrawable(this, R.drawable.eye))
                    MainScope().launch(Dispatchers.IO) {
                        notView(uid)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}