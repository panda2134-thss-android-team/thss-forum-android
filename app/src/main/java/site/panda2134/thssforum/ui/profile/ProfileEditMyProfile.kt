package site.panda2134.thssforum.ui.profile

<<<<<<< HEAD
import android.R
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import com.github.kittinunf.fuel.gson.gsonDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.api.downloadImage
=======
import android.os.Bundle
>>>>>>> 1e5741329102017cc2283872557b4a0230693426
import site.panda2134.thssforum.databinding.ProfileEditMyProfileBinding
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
        val apiService = APIService(this)
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(apiService)
        }

        val save_button = binding.saveButton
        save_button.setOnClickListener() {
            fun onClick(v: View?) {
                // 修改个人信息
                val user: User
                val api = APIService(this)
                MainScope().launch(Dispatchers.IO) {
                    EditUserInfo(apiService)
                }
            }
        }

    }

    private suspend fun EditUserInfo(apiService: APIService) {
        try {
            var user: User
            user.nickname = binding.myName.text.toString()
            user.intro = binding.myIntro.text.toString()
            // TODO: 修改图片

            apiService.modifyProfile(user.toJson())
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private suspend fun loadUserInfo(apiService: APIService) {
        try {
            val user: User = apiService.getProfile()
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