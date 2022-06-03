package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.HomeSearchPageBinding
import site.panda2134.thssforum.databinding.ProfileNotifySearchPageBinding


class ProfileNotifySearch : AppCompatActivity() {
    private lateinit var binding: ProfileNotifySearchPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("home_discover_menu_search")

        binding = ProfileNotifySearchPageBinding.inflate(layoutInflater)
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