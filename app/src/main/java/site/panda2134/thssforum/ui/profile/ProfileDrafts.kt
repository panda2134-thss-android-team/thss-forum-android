package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import site.panda2134.thssforum.databinding.ProfileDraftsBinding

class ProfileDrafts : ActivityProfileItem() {
    private lateinit var binding: ProfileDraftsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_drafts")

        binding = ProfileDraftsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}