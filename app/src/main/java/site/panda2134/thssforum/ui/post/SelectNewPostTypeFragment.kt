package site.panda2134.thssforum.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.FragmentPostBinding

class SelectNewPostTypeFragment : Fragment() {

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
            requireView().findNavController().navigate(R.id.action_navigation_post_to_postPureText)
        }

        buttonTextPicMix.setOnClickListener {
            // Do something in response to button click
            requireView().findNavController().navigate(R.id.action_navigation_post_to_postTextPicMix)
        }

        buttonAudio.setOnClickListener {
            // Do something in response to button click
            requireView().findNavController().navigate(R.id.action_navigation_post_to_postAudio)
        }

        buttonVideo.setOnClickListener {
            // Do something in response to button click
            requireView().findNavController().navigate(R.id.action_navigation_post_to_postVideo)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }


}