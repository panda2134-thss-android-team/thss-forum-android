package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileNotifySearchPageBinding


class ProfileNotifySearch : ActivityProfileItem() {
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