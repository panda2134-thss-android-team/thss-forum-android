package site.panda2134.thssforum.ui.post

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.ActivityNavigator
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.PostPureTextBinding

class ActivityNewPureTextPost : ActivityNewPost() {
    private lateinit var binding: PostPureTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostPureTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}