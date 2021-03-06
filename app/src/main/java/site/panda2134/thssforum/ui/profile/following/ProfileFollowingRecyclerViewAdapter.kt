package site.panda2134.thssforum.ui.profile.following

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.ProfileFollowingListItemBinding
import site.panda2134.thssforum.models.User
import site.panda2134.thssforum.ui.profile.ProfileUserHomepage

class ProfileFollowingRecyclerViewAdapter(private val dataset: ArrayList<User>):
    RecyclerView.Adapter<ProfileFollowingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileFollowingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ProfileFollowingListItemBinding.inflate(layoutInflater)
        return ProfileFollowingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileFollowingViewHolder, position: Int) {
        with (holder.binding) {
            nickname.text = dataset[position].nickname
            Glide.with(root).load(dataset[position].avatar)
                .placeholder(R.drawable.ic_baseline_account_circle_24).into(followingAvatar)

            // 点击item跳转至对应的主页
            followingItem.setOnClickListener {
                val intent = Intent(holder.binding.root.context, ProfileUserHomepage::class.java)
                    .putExtra("author", dataset[position].uid)
                holder.binding.root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}