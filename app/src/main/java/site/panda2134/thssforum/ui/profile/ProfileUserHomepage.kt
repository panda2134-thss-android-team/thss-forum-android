package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileUserHomepageBinding


class ProfileUserHomepage : ActivityProfileItem() {
    private lateinit var binding: ProfileUserHomepageBinding
    private var isView = true // 右上角的屏蔽与否：默认是不屏蔽
    private lateinit var menu: Menu
    private lateinit var uid : String // 本界面展示的作者的uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_user_homepage")

        binding = ProfileUserHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // post = intent.getSerializableExtra("post")
    }

    // activity的menubar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.following_eyeswitch_menuicon, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.eye_menu_item -> {
                if(isView) {
                    isView = false
                    menu.getItem(1)?.icon = ContextCompat.getDrawable(this, R.drawable.eye_off)
                }
                else {
                    isView = true
                    menu.getItem(1)?.icon = (ContextCompat.getDrawable(this, R.drawable.eye))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}