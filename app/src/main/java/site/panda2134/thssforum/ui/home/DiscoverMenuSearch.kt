package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.HomeSearchPageBinding


class DiscoverMenuSearch : AppCompatActivity() {
    private lateinit var binding: HomeSearchPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("home_discover_menu_search")

        binding = HomeSearchPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                    }
                R.id.rb_pure_text ->
                    if (checked) {
                        // 纯文字
                    }
                R.id.rb_text_pic_mix ->
                    if (checked) {
                        // 图文混合
                    }
                R.id.rb_audio ->
                    if (checked) {
                        // 音频
                    }
                R.id.rb_video ->
                    if (checked) {
                        // 视频
                    }
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
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}