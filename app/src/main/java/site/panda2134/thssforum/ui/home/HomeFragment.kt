package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.await
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.R
import site.panda2134.thssforum.api.APIService
import site.panda2134.thssforum.data.CommentItemDataSource
import site.panda2134.thssforum.databinding.FragmentHomeBinding
import site.panda2134.thssforum.models.CommentResponse
import site.panda2134.thssforum.models.User
import java.io.InputStream


class HomeFragment : Fragment() {
    private lateinit var tabAdapter: TabAdapter
    private var _binding: FragmentHomeBinding? = null

    // CommentItem的// TODO:之后删
    private val hasNext = true
    private val dataSource = CommentItemDataSource()
    private lateinit var dataset: MutableList<CommentResponse>
    // CommentItem的

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var bitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // CommentItem的// TODO:之后删
        // TODO("dynamic loading of CommentItem")
        // 建议：先做发帖的动态加载（每次加载一天动态，如果返回空，则加载一个星期，再不行就提示“只能查看近一周动态”）
        // comment的动态加载不急着做
//        dataset = dataSource.getPosts().toMutableList()
//        binding.recyclerComments.adapter = CommentItemAdapter(dataset)
//        binding.recyclerComments.addOnScrollListener(object: RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if (!recyclerView.canScrollVertically(1)) {
//                    if (dataSource.hasNextPage) {
//                        Log.i("dataset", "loaded")
//                        val oldLen = dataset.size
//                        dataset.addAll(dataSource.getPosts())
//                        recyclerView.adapter?.notifyItemInserted(oldLen)
//                    }
//                }
//            }
//        })
        //<androidx.recyclerview.widget.RecyclerView
        //                            android:id="@+id/recycler_comments"
        //                            android:layout_width="match_parent"
        //                            android:layout_height="match_parent"
        //                            app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
        //                            tools:itemCount="3"
        //                            tools:listitem="@layout/post_comment_item" />
        // CommentItem的

        // 载入顶部：我的头像、昵称和简介
        val user: User
        val apiService = APIService(requireActivity())
        MainScope().launch(Dispatchers.IO) {
            loadUserInfo(apiService)
        }

        return binding.root
    }

    private suspend fun loadUserInfo(apiService: APIService) {
        try {
            val user: User = apiService.getProfile()
            withContext(Dispatchers.Main) {
                binding.myName.text = user.nickname
                binding.myMotto.text = user.intro
            }
            // 画图

            val bmp = Fuel.get(user.avatar).await(object : ResponseDeserializable<Bitmap> {
                override fun deserialize(inputStream: InputStream): Bitmap? {
                    return BitmapFactory.decodeStream(inputStream)
                }
            })
            withContext(Dispatchers.Main) {
                binding.myImage.setImageBitmap(bmp)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabAdapter = TabAdapter(this)
        binding.pager.adapter = tabAdapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> context?.getString(R.string.all_posts)
                1 -> context?.getString(R.string.followed_posts)
                else -> throw IllegalArgumentException("unknown tab! position can only be 0 or 1")
            }
        }.attach()
    }

    // 设置要不要显示menubar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    // 在顶栏加图标（因为是fragment所以写法不同）
    // 之后写点击事件的时候，直接对应重载就可以了
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discover_searchswitch_menuicon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_search_menu_item -> {
                val intent = Intent(activity, DiscoverMenuSearch::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}