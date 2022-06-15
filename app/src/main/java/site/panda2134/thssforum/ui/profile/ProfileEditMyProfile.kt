package site.panda2134.thssforum.ui.profile

import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.api.downloadImage
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
        val user: User
        val apiService = APIWrapper(this)
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(apiService)
        }

        val save_button = binding.saveButton
        save_button.setOnClickListener() {
            // 修改个人信息
            val user: User
            val api = APIWrapper(this)
            MainScope().launch(Dispatchers.IO) {
                EditUserInfo(apiService)
            }
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }

    }

    private suspend fun EditUserInfo(apiWrapper: APIWrapper) {
        try {
            var user: ModifyProfileRequest
            // TODO: 修改图片
            apiWrapper.modifyProfile(ModifyProfileRequest(nickname=binding.myName.text.toString(),intro=binding.myIntro.text.toString()))

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun loadUserInfo(apiWrapper: APIWrapper) {
        try {
            val user: User = apiWrapper.getProfile()
            withContext(Dispatchers.Main) {

                binding.myName.text = user.nickname.toEditable()
                binding.myIntro.text = user.intro.toEditable()
            }
            // 画图

            val bmp = downloadImage(user.avatar)
            withContext(Dispatchers.Main) {
                binding.myImage.setImageBitmap(bmp)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    // 把string变成editable
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

}