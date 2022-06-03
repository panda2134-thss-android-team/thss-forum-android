package site.panda2134.thssforum.ui.post

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.PostPureTextBinding
import site.panda2134.thssforum.databinding.PostTextPicMixBinding

class PostTextPicMix : AppCompatActivity() {
    private lateinit var binding: PostTextPicMixBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("post_textpic_mix")

        binding = PostTextPicMixBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // activity的menubar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) return false

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