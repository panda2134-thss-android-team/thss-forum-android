package site.panda2134.thssforum.ui.post

import android.os.Bundle
import site.panda2134.thssforum.databinding.PostAudioBinding

class ActivityNewAudioPost : ActivityNewPost() {
    private lateinit var binding: PostAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}