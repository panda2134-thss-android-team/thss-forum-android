package site.panda2134.thssforum.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileBellPageBinding

class ProfileNotify : ActivityProfileItem() {
    private lateinit var binding: ProfileBellPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_menu_bell")

        binding = ProfileBellPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // activity的menubar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.profile_notify_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_menu_item -> {
                val intent = Intent(this, ProfileNotifySearch::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}