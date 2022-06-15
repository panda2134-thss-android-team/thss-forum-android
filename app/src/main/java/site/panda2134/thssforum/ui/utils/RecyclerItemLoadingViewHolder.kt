package site.panda2134.thssforum.ui.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.RecyclerItemLoadingBinding

class RecyclerItemLoadingViewHolder(val binding: RecyclerItemLoadingBinding):
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.progressBar.visibility = View.VISIBLE
        binding.noContent.visibility = View.GONE
    }

    fun setLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.noContent.visibility = View.GONE
    }

    fun setNoContent(additionalText: String? = null) {
        binding.progressBar.visibility = View.GONE
        binding.noContent.visibility = View.VISIBLE
        binding.noContent.text = when (additionalText) {
            null -> binding.root.context.getString(R.string.NO_MORE_CONTENT)
            else -> binding.root.context.getString(R.string.NO_MORE_CONTENT) + "($additionalText)"
        }
    }
}