package site.panda2134.thssforum.ui.profile.draft

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.zerobranch.layout.SwipeLayout
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import site.panda2134.thssforum.R
import site.panda2134.thssforum.databinding.DraftItemBinding
import site.panda2134.thssforum.ui.post.*
import site.panda2134.thssforum.utils.toTimeAgo
import java.time.Instant

class DraftListRecyclerViewAdapter(val dataset: MutableList<Pair<String, ActivityNewPostWithDraft.Companion.DeserializedDraft>>,
                                   private val showOnEmptyView: View? = null):
    RecyclerView.Adapter<DraftListRecyclerViewAdapter.DraftListRecyclerViewHolder>() {

    class DraftListRecyclerViewHolder(val binding: DraftItemBinding): RecyclerView.ViewHolder(binding.root) {
        private var draftId: String? = null
        private var draftCreatedAt: Instant? = null

        init {
            MainScope().launch {
                while (true) {
                    delay(5)
                    binding.draftTime.text = draftCreatedAt?.toTimeAgo() ?: continue
                }
            }
        }

        fun bindDraft(
            draftId: String,
            draft: ActivityNewPostWithDraft.Companion.DeserializedDraft
        ) {
            this.draftId = draftId
            this.draftCreatedAt = draft.createdAt

            binding.draftTitle.text = draft.data.get("title").asString
                .ifBlank { binding.root.context.getString(R.string.empty_title) }

            binding.draftTime.text = draft.createdAt.toTimeAgo()
            with (binding.root.context) {
                binding.draftType.text = when (draft.activityClassName) {
                    ActivityNewPureTextPost::class.java.name -> getString(R.string.pure_text)
                    ActivityNewTextPicMixPost::class.java.name -> getString(R.string.text_pic_mix)
                    ActivityNewAudioPost::class.java.name -> getString(R.string.audio)
                    ActivityNewVideoPost::class.java.name -> getString(R.string.video)
                    else -> getString(R.string.unknown)
                }
            }
            binding.swipeLayout.setOnActionsListener(object: SwipeLayout.SwipeActionsListener {
                override fun onOpen(direction: Int, isContinuous: Boolean) {
                    if (direction == SwipeLayout.LEFT) {
                        AlertDialog.Builder(binding.root.context)
                            .setTitle(R.string.are_you_sure_to_delete)
                            .setMessage(R.string.delete_lose_forever)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                with (binding.root.context) {
                                    getSharedPreferences(getString(R.string.DRAFT_SHARED_PREF), Context.MODE_PRIVATE).edit().run {
                                        remove(draftId)
                                        apply()
                                    }
                                    Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show()
                                    val adapter = bindingAdapter as? DraftListRecyclerViewAdapter
                                    adapter?.dataset?.removeAt(bindingAdapterPosition)
                                    adapter?.notifyItemRemoved(bindingAdapterPosition)
                                    adapter?.showOnEmptyView?.isVisible = adapter?.itemCount == 0
                                }
                            }
                            .show()
                    }
                }

                override fun onClose() {
                }
            })
            binding.dragItem.setOnClickListener {
                ActivityNewPostWithDraft.startNewPostActivityForDraftId(binding.root.context, draftId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftListRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DraftItemBinding.inflate(layoutInflater, parent, false)
        return DraftListRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DraftListRecyclerViewHolder, position: Int) {
        val (draftId, deserializedDraft) = dataset[position]
        holder.bindDraft(draftId, deserializedDraft)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}