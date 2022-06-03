package site.panda2134.thssforum.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllPostsFragment()
            1 -> FollowedPostsFragment()
            else -> throw IllegalArgumentException("unknown tab! position can only be 0 or 1")
        }
    }
}