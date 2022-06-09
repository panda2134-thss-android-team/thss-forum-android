package site.panda2134.thssforum.ui.post

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import site.panda2134.thssforum.MainActivity
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.*
import site.panda2134.thssforum.ui.home.DiscoverMenuSearch

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val buttonPureText = binding.pureText
        val buttonTextPicMix = binding.textPicMix
        val buttonAudio = binding.audio
        val buttonVideo = binding.video

        buttonPureText.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(activity, PostPureText::class.java)
            startActivity(intent)
        }

        buttonTextPicMix.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(activity, PostTextPicMix::class.java)
            startActivity(intent)
        }

        buttonAudio.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(activity, PostAudio::class.java)
            startActivity(intent)
        }

        buttonVideo.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(activity, PostVideo::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }





}