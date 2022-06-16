package site.panda2134.thssforum.ui.profile.following

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.panda2134.thssforum.api.downloadImage
import site.panda2134.thssforum.databinding.ProfileFollowingListItemBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.profile.ProfileUserHomepage

// 对于关注者列表不需要实现懒加载
// 主页的RecyclerView需要实现，因此还要考虑监听滚动事件
class ProfileFollowingRecyclerViewAdapter(private val dataset: ArrayList<User>):
    RecyclerView.Adapter<ProfileFollowingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileFollowingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ProfileFollowingListItemBinding.inflate(layoutInflater)
        return ProfileFollowingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileFollowingViewHolder, position: Int) {
        holder.binding.nickname.text = dataset[position].nickname
        MainScope().launch(Dispatchers.IO) {
            val bmp = downloadImage(dataset[position].avatar)
            withContext(Dispatchers.Main) {
                holder.binding.followingAvatar.setImageBitmap(bmp)
            }
        }

        // 点击item跳转至对应的主页
        holder.binding.followingItem.setOnClickListener {
            println("在点击了！这里是：" + dataset[position].nickname)
            val intent = Intent(holder.binding.root.context, ProfileUserHomepage::class.java)
                            .putExtra("author", dataset[position].uid)
            holder.binding.root.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}