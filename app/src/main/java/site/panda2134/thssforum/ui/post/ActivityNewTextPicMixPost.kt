package site.panda2134.thssforum.ui.post

import android.os.Bundle
import site.panda2134.thssforum.databinding.PostTextPicMixBinding

class ActivityNewTextPicMixPost : ActivityNewPost() {
    private lateinit var binding: PostTextPicMixBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PostTextPicMixBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}