package site.panda2134.thssforum.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileDraftsBinding
import site.panda2134.thssforum.databinding.ProfileEditMyProfileBinding

class ProfileEditMyProfile : ActivityProfileItem() {
    private lateinit var binding: ProfileEditMyProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_edit_my_profile")

        binding = ProfileEditMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}