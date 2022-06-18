package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileUserHomepageBinding
import site.panda2134.thssforum.ui.home.postlist.PostListRecyclerViewAdapter


class ProfileUserHomepage : ActivityProfileItem() {
    private lateinit var binding: ProfileUserHomepageBinding
    private var isBlocked = MutableLiveData<Boolean>(false) // 右上角的屏蔽与否：默认是不屏蔽
    private var menu: Menu? = null
    private lateinit var uid : String // 本界面展示的作者的uid
    private lateinit var api: APIWrapper
    private lateinit var adapter: PostListRecyclerViewAdapter
    private var isCurrentUser = false // 如果是当前用户的话就没有 ”屏蔽与否/关注“，默认不是

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileUserHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getSerializableExtra("author") as String
        // 载入顶部：那个用户的主页
        api = APIWrapper(this)
        MainScope().launch {
            loadUserInfo()
            loadIsBlocked()
        }
        isBlocked.observe(this) {
            menu?.apply {
                findItem(R.id.eye_menu_item).icon =
                    ContextCompat.getDrawable(this@ProfileUserHomepage,
                        if (it) R.drawable.eye_off else R.drawable.eye)
            }
        }

        isCurrentUser = (uid == api.currentUserId)
        binding.followedButton.visibility = if (isCurrentUser) View.GONE else View.VISIBLE

        adapter = PostListRecyclerViewAdapter(api, uid = uid, activity = this, lifecycleOwner = this)
        binding.hpPostsList.adapter = adapter
        adapter.setupRecyclerView(this, binding.hpPostsList)

        binding.followedButton.setOnClickListener {

        }
    }

    private suspend fun loadIsBlocked() {
        val blockList = withContext(Dispatchers.IO) { api.getBlacklist() }
        isBlocked.value = uid in blockList.map { it.uid }
    }

    private suspend fun loadUserInfo() {
        try {
            val user = withContext(Dispatchers.IO) {
                api.getUserInfo(uid)
            }
            withContext(Dispatchers.Main) {
                binding.myName.text = user.nickname
                binding.myMotto.text = user.intro
                Glide.with(binding.root).load(user.avatar)
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(binding.myAvatar)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    // 不去看该用户内容
    private suspend fun blockThisUser() {
        withContext(Dispatchers.IO) {
            try {
                api.addBlacklistUser(uid)
                println("完成加入黑名单啦")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        withContext(Dispatchers.Main) {
            isBlocked.value = true
        }
    }

    // 看该用户内容
    private suspend fun unblockThisUser() {
        withContext(Dispatchers.IO) {
            try {
                api.delBlacklistUser(uid)
                println("完成恢复白名单啦")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        withContext(Dispatchers.Main) {
            isBlocked.value = false
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
                MainScope().launch {
                    if(isBlocked.value == true) {
                        unblockThisUser()
                    } else {
                        blockThisUser()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}