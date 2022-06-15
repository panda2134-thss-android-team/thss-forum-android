package site.panda2134.thssforum.ui.profile.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import site.panda2134.thssforum.api.NotificationService
import site.panda2134.thssforum.databinding.NotificationItemBinding
import site.panda2134.thssforum.utils.toTimeAgo
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

class NotificationRecyclerViewAdapter:
    RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationRecyclerViewHolder>() {

    val notificationList = arrayListOf<NotificationService.NotificationItem>()

    inner class NotificationRecyclerViewHolder(val binding: NotificationItemBinding): RecyclerView.ViewHolder(binding.root) {
        private var notificationItem: NotificationService.NotificationItem? = null

        init {
            MainScope().launch {
                while (true) {
                    delay(5 * 1000)
                    notificationItem?.run {
                        binding.notificationTime.text = receivedAt.toTimeAgo()
                    }
                }
            }
        }

        fun bindNotification(n: NotificationService.NotificationItem) {
            notificationItem = n

            MainScope().launch(Dispatchers.IO) {
                val s = NotificationService.NotificationStringBuilder(binding.root.context)
                    .build(n.content)
                withContext(Dispatchers.Main) {
                    binding.notificationTitle.text = s.title
                    binding.notificationContent.text = s.content
                    binding.notificationTime.text = n.receivedAt.toTimeAgo()
                }
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationRecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NotificationRecyclerViewHolder(NotificationItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: NotificationRecyclerViewHolder, position: Int) {
        holder.bindNotification(notificationList[position])
    }

//    override fun onViewRecycled(holder: NotificationRecyclerViewHolder) {
//        try {
////            holder.refreshScope.cancel()
//        } catch (e: Throwable) {
//            e.printStackTrace()
//        }
//    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}