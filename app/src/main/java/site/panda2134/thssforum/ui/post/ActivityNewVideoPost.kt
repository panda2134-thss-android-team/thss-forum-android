package site.panda2134.thssforum.ui.post

import android.os.Bundle
import site.panda2134.thssforum.databinding.PostVideoBinding

class ActivityNewVideoPost : ActivityNewPost() {
    private lateinit var binding: PostVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}