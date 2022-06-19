package site.panda2134.thssforum.ui.profile

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.ProfileEditMyProfileBinding
import site.panda2134.thssforum.models.ModifyProfileRequest
import site.panda2134.thssforum.models.User
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ProfileEditMyProfile : ActivityProfileItem() {
    private lateinit var binding: ProfileEditMyProfileBinding
    private lateinit var api: APIWrapper
    private var newImageLocalPath: String? = null

    private val choosePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) return@registerForActivityResult
            val image = BGAPhotoPickerActivity.getSelectedPhotos(it.data).firstOrNull()
                ?: return@registerForActivityResult
            newImageLocalPath = image
            binding.myImage.setImageBitmap(BitmapFactory.decodeFile(image))
        }

    private val requestPhotoPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.contains(false)) return@registerForActivityResult
            val takePhotoDir =
                File(Environment.getExternalStorageDirectory(), "THSSForum")

            val photoPickerIntent = BGAPhotoPickerActivity.IntentBuilder(this)
                .cameraFileDir(takePhotoDir)
                .maxChooseCount(1)
                .selectedPhotos(null) // 当前已选中的图片路径集合
                .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
                .build()
            choosePhoto.launch(photoPickerIntent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("profile_edit_my_profile")

        binding = ProfileEditMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 载入顶部：我的头像、昵称和简介
        api = APIWrapper(this)
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo()
            withContext(Dispatchers.Main) {
                binding.myImage.setOnClickListener {
                    requestPhotoPermissions.launch(
                        listOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ).toTypedArray()
                    )
                }
            }
        }
        binding.saveButton.setOnClickListener {
            // 修改个人信息
            Log.d("test", "Clicked")
            MainScope().launch(Dispatchers.IO) {
                try {
                    val uploadedAvatar = newImageLocalPath?.let {
                        suspendCoroutine<Unit> { continuation ->
                            binding.saveButton.post {
                                binding.saveButton.startAnimation { continuation.resume(Unit) }
                            }
                        }
                        api.uploadFileToOSS(
                            Uri.fromFile(File(it)),
                            false
                        ) { _, currentBytes, totalBytes ->
                            binding.saveButton.post {
                                binding.saveButton.setProgress(currentBytes * 100.0f / totalBytes)
                            }
                        }
                    }
                    if (uploadedAvatar == null) {
                        suspendCoroutine<Unit> { continuation ->
                            binding.saveButton.post {
                                binding.saveButton.startAnimation { continuation.resume(Unit) }
                            }
                        }
                    }
                    val req = ModifyProfileRequest(
                        nickname = binding.myName.text.toString(),
                        intro = binding.myIntro.text.toString(),
                        avatar = uploadedAvatar?.toString()
                    )
                    api.modifyProfile(req)
                    Log.d("test", "I'm OK")
                    withContext(Dispatchers.Main) {
                        binding.saveButton.doneLoadingAnimation(
                            br.com.simplepass.loadingbutton.R.color.green,
                            BitmapFactory.decodeResource(
                                binding.root.context.resources,
                                R.drawable.ic_cloud_upload_white_24dp
                            )
                        )
                        binding.saveButton.postDelayed(
                            {
                                Toast.makeText(
                                    this@ProfileEditMyProfile,
                                    getString(R.string.save_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }, 200
                        )
                    }
                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        binding.saveButton.revertAnimation()
                    }
                    e.printStackTrace()
                }
            }
        }

    }


    private suspend fun loadUserInfo() {
        try {
            val user: User = api.getProfile()
            withContext(Dispatchers.Main) {
                binding.myName.setText(user.nickname)
                binding.myIntro.setText(user.intro)
            }

            withContext(Dispatchers.Main) {
                Glide.with(binding.root).load(user.avatar)
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(binding.myImage)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}