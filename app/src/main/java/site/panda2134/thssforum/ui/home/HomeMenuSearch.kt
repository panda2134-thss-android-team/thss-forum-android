package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.HomeSearchPageBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.profile.ProfileNotificationList


class HomeMenuSearch : AppCompatActivity() {
    private lateinit var binding: HomeSearchPageBinding
    private lateinit var select_type : String
    private lateinit var uid: String
    private lateinit var api: APIWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("home_menu_search")

        binding = HomeSearchPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 载入顶部：我的头像、昵称和简介
        api = APIWrapper(this)
        MainScope().launch(Dispatchers.IO) {
            getUid()
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rb_all ->
                    if (checked) {
                        // 全部
                        select_type = "all"
                    }
                R.id.rb_normal ->
                    if (checked) {
                        // 纯文字 和 图文混合
                        select_type = "normal"
                    }
                R.id.rb_audio ->
                    if (checked) {
                        // 音频
                        select_type = "audio"
                    }
                R.id.rb_video ->
                    if (checked) {
                        // 视频
                        select_type = "video"
                    }
            }
        }
    }

    private suspend fun getUid() {
        try {
            val user: User = api.getProfile()
            withContext(Dispatchers.Main) {
                uid = user.uid
            }
        } catch (e: Throwable) {
            e.printStackTrace()
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
                                .putExtra("uid", uid)
                                .putExtra("title", binding.title.text)
                                .putExtra("user_name", binding.userName.text)
                                .putExtra("types", select_type)
                println("before_enter_result")
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}