package site.panda2134.thssforum.ui.post

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.ActivityNavigator
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.PostAudioBinding
import site.panda2134.thssforum.databinding.PostPureTextBinding

class ActivityNewAudioPost : ActivityNewPost() {
    private lateinit var binding: PostAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}