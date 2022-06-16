package site.panda2134.thssforum.ui.post

import android.os.Bundle
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostAudioBinding

class ActivityNewAudioPost : ActivityNewPost() {
    // TODO
    private lateinit var binding: PostAudioBinding
    private lateinit var api: APIWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}