package site.panda2134.thssforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.FragmentAllPostsBinding
import site.panda2134.thssforum.models.User

const val EXTRA_MESSAGE = "site.panda2134.thssforum.MESSAGE"

class AllPostsFragment: Fragment() {
    private val _menu = null
    private var _binding: FragmentAllPostsBinding? = null
    private val binding get() = _binding!!
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            _binding = FragmentAllPostsBinding.inflate(inflater, container, false)


            // TODO：这里最后应该是item中的内容
            // 把动态item中的每一项调用api填写
            val user: User
            val apiService = APIService(requireActivity())
            MainScope().launch(Dispatchers.IO) {
                loadPostItem(apiService)
            }


            return binding.root
    }

    private suspend fun loadPostItem(apiService: APIService) {// TODO:这里写到一半
        try {
            val user: User = apiService.getProfile()
            withContext(Dispatchers.Main) {
                binding.postUser.text = user.nickname
            }
            // 画图
            val bmp = downloadImage(user.avatar)
            withContext(Dispatchers.Main) {
                binding.postUserImage.setImageBitmap(bmp)// 画图
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }



}