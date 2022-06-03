package site.panda2134.thssforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import site.panda2134.thssforum.R
import android.view.View

const val EXTRA_MESSAGE = "site.panda2134.thssforum.MESSAGE"

class AllPostsFragment: Fragment() {
    private val _menu = null
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_posts, container, false)
    }



}