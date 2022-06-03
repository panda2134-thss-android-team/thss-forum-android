package site.panda2134.thssforum.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileDraftsBinding
import site.panda2134.thssforum.databinding.ProfileInterestListBinding

class ProfileInterestList : AppCompatActivity() {
    private lateinit var binding: ProfileInterestListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_interest_list")

        binding = ProfileInterestListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}