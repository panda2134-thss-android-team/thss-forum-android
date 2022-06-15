package site.panda2134.thssforum.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    private val allPostsFragment = AllPostsFragment()
    private val followedPostsFragment = FollowedPostsFragment()

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> allPostsFragment
            1 -> followedPostsFragment
            else -> throw IllegalArgumentException("unknown tab! position can only be 0 or 1")
        }
    }
}