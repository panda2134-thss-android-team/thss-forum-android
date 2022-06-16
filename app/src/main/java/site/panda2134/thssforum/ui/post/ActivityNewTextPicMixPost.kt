package site.panda2134.thssforum.ui.post

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout
import kotlinx.coroutines.*
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIWrapper
import site.panda2134.thssforum.databinding.PostTextPicMixBinding
import site.panda2134.thssforum.models.ImageTextPostContent
import site.panda2134.thssforum.models.PostContent
import java.io.File
import java.time.Instant


class ActivityNewTextPicMixPost : ActivityNewPost(), BGASortableNinePhotoLayout.Delegate {
    private lateinit var binding: PostTextPicMixBinding
    private lateinit var api: APIWrapper
    private val tag = "newTextPicMixPost"
    val progressPercentage = MutableLiveData<Int>(0)
    val uploading = MutableLiveData(false)
    private val alertDialog: AlertDialog
        get() = AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_photo))
            .setMessage(R.string.please_shot_or_choose_a_photo)
            .create()

    private val chooseMorePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) return@registerForActivityResult
        binding.photoPicker.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(it.data))
    }

    private val requestPermissionsResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.values.contains(false)) return@registerForActivityResult
        val takePhotoDir =
            File(Environment.getExternalStorageDirectory(), "THSSForum")

        val photoPickerIntent = BGAPhotoPickerActivity.IntentBuilder(this)
            .cameraFileDir(takePhotoDir)
            .maxChooseCount(binding.photoPicker.maxItemCount - binding.photoPicker.itemCount)
            .selectedPhotos(null) // 当前已选中的图片路径集合
            .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
            .build()
        chooseMorePhoto.launch(photoPickerIntent)
    }

    private val previewPhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        binding.photoPicker.data = BGAPhotoPickerActivity.getSelectedPhotos(it.data)
    }

    private fun loadDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        val draftTitle = pref.getString(getString(R.string.PREF_KEY_IMAGE_TITLE), "")
        val draftContent = pref.getString(getString(R.string.PREF_KEY_IMAGE_CONTENT), "")
        val draftImages = api.gsonFireObject.fromJson(pref.getString(getString(R.string.PREF_KEY_IMAGE_PATH_LIST), "[]"), Array<String>::class.java)
        binding.title.setText(draftTitle)
        binding.content.setText(draftContent)
        binding.photoPicker.apply {
            while (data.size > 0) {
                removeItem(0)
            }
            addMoreData(draftImages.toCollection(ArrayList()))
        }
        Log.d(tag, "imagePath = ${binding.photoPicker.data}")
    }

    private fun saveDraft() {
        val pref = getSharedPreferences(getString(R.string.GLOBAL_SHARED_PREF), MODE_PRIVATE)
        with(pref.edit()) {
            putString(getString(R.string.PREF_KEY_IMAGE_TITLE), binding.title.text.toString())
            putString(getString(R.string.PREF_KEY_IMAGE_CONTENT), binding.content.text.toString())
            putString(getString(R.string.PREF_KEY_IMAGE_PATH_LIST), api.gsonFireObject.toJson(binding.photoPicker.data.toTypedArray()))
            apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = APIWrapper(this)
        binding = PostTextPicMixBinding.inflate(layoutInflater)
        binding.p = this
        setContentView(binding.root)
        loadDraft()
        binding.photoPicker.apply {
            maxItemCount = 9
            isSortable = true
            isPlusEnable = true
            isEditable = true

            setDelegate(this@ActivityNewTextPicMixPost)
        }
    }

    override fun finish() {
        super.finish()
        saveDraft()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_menu_item -> {
                Log.d(tag, "item clicked")
                if(binding.photoPicker.data.size == 0) {
                    alertDialog.show()
                }
                else {
                    lifecycleScope.launch {
                        // 首先上传
                        val images = binding.photoPicker.data
                        val uploadedSizeList = MutableList(images.size) { 0L }
                        val totalSizeList = MutableList(images.size) { 0L }
                        uploading.value = true
                        val uploadedImagePath = images.mapIndexed { index, localImagePath ->
                            lifecycleScope.async(Dispatchers.IO) {
                                api.uploadFileToOSS(Uri.fromFile(File(localImagePath))) { _, uploaded, total ->
                                    uploadedSizeList[index] = uploaded
                                    totalSizeList[index] = total
                                    progressPercentage.postValue((uploadedSizeList.sum() * 100 / totalSizeList.sum()).toInt())
                                }
                            }
                        }.awaitAll()
                        uploading.value = false
                        val postContent =
                            PostContent.makeImageTextPost(
                                ImageTextPostContent(
                                    title=binding.title.text.toString(),
                                    text=binding.content.text.toString(),
                                    images=uploadedImagePath.map { it.toString() }.toCollection(ArrayList())
                                ),
                                createdAt = Instant.now()
                            )
                        withContext(Dispatchers.IO) {
                            api.newPost(postContent)
                        }
                        binding.title.text?.clear()
                        binding.content.text?.clear()
                        binding.photoPicker.data.clear()
                        Toast.makeText(this@ActivityNewTextPicMixPost, R.string.post_success, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickAddNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        models: ArrayList<String>?
    ) {
        requestPermissionsResult.launch(listOf(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray())
    }

    override fun onClickDeleteNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: ArrayList<String>?
    ) {
        binding.photoPicker.removeItem(position)
    }

    override fun onClickNinePhotoItem(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        view: View?,
        position: Int,
        model: String?,
        models: ArrayList<String>?
    ) {
        val photoPickerPreviewIntent = BGAPhotoPickerPreviewActivity.IntentBuilder(this)
            .previewPhotos(models)
            .selectedPhotos(models)
            .maxChooseCount(binding.photoPicker.maxItemCount)
            .currentPosition(position)
            .isFromTakePhoto(false)
            .build()
        previewPhotoResult.launch(photoPickerPreviewIntent)
    }

    override fun onNinePhotoItemExchanged(
        sortableNinePhotoLayout: BGASortableNinePhotoLayout?,
        fromPosition: Int,
        toPosition: Int,
        models: ArrayList<String>?
    ) {
    }
}