package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.HomeSearchPageBinding
import site.panda2134.thssforum.models.PostType


class HomeMenuSearch : AppCompatActivity() {

    private lateinit var binding: HomeSearchPageBinding
    private lateinit var api: APIWrapper
    private var filterPostType: PostType? = null // null=全部，否则筛选某类

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("home_menu_search")

        binding = HomeSearchPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 载入顶部：我的头像、昵称和简介
        api = APIWrapper(this)

        binding.rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_all ->
                    // 全部
                    filterPostType = null
                R.id.rb_normal ->
                    // 纯文字 和 图文混合
                    filterPostType = PostType.normal
                R.id.rb_audio ->
                    // 音频
                    filterPostType = PostType.audio
                R.id.rb_video ->
                    // 视频
                    filterPostType = PostType.video
            }
        }
    }

    // activity的menubar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.send_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                val intent = Intent(this, HomeSearchResult::class.java)
                    .putExtra("searchText", binding.searchText.text.toString())
                    .putExtra("searchNickname", binding.searchNickname.text.toString())
                    .putExtra("postType", filterPostType?.ordinal)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}