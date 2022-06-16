package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileEditMyProfileBinding
import site.panda2134.thssforum.models.ModifyProfileRequest
import site.panda2134.thssforum.models.User


class ProfileEditMyProfile : ActivityProfileItem() {
    private lateinit var binding: ProfileEditMyProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_edit_my_profile")

        binding = ProfileEditMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 载入顶部：我的头像、昵称和简介
        val apiService = APIWrapper(this)
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(apiService)
        }

        binding.saveButton.setOnClickListener() {
            // 修改个人信息
            MainScope().launch(Dispatchers.IO) {
                try {
                    editUserInfo(apiService)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileEditMyProfile, getString(R.string.save_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

        }

    }

    private suspend fun editUserInfo(apiWrapper: APIWrapper) {
        // TODO: 修改图片
        apiWrapper.modifyProfile(ModifyProfileRequest(nickname=binding.myName.text.toString(),intro=binding.myIntro.text.toString()))
    }

    private suspend fun loadUserInfo(apiWrapper: APIWrapper) {
        try {
            val user: User = apiWrapper.getProfile()
            withContext(Dispatchers.Main) {

                binding.myName.text = user.nickname.toEditable()
                binding.myIntro.text = user.intro.toEditable()
            }
            // 画图

            withContext(Dispatchers.Main) {
                Glide.with(binding.root).load(user.avatar)
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(binding.myImage)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    // 把string变成editable
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

}