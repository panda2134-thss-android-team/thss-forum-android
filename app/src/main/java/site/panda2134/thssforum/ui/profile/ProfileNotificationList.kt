package site.panda2134.thssforum.ui.profile

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import site.panda2134.thssforum.api.NotificationService
import site.panda2134.thssforum.databinding.ProfileNotificationListBinding
import site.panda2134.thssforum.ui.profile.notification.NotificationRecyclerViewAdapter

class ProfileNotificationList : ActivityProfileItem() {
    private lateinit var binding: ProfileNotificationListBinding
    private lateinit var adapter: NotificationRecyclerViewAdapter
    private var binder: NotificationService.LocalBinder? = null

    fun refreshList() {
        val oldNotification = adapter.notificationList.toHashSet()
        val newNotification = binder.run {
            if (this == null) return@refreshList
            getNotifications().sortedBy { it.receivedAt }.reversed().filter { it !in oldNotification }
        }
        adapter.notificationList.addAll(0, newNotification)
        adapter.notifyItemRangeInserted(0, newNotification.size)
        if (adapter.notificationList.size == 0) {
            binding.noContent.visibility = View.VISIBLE
        } else {
            binding.noContent.visibility = View.GONE
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            binder = service as NotificationService.LocalBinder
            adapter.notificationList.clear()
            refreshList()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ProfileNotificationListBinding.inflate(layoutInflater)
        adapter = NotificationRecyclerViewAdapter()
        binding.notificationList.adapter = adapter
        setContentView(binding.root)
        bindService(Intent(this, NotificationService::class.java), connection, 0)
        binding.swiperRefresh.setOnRefreshListener {
            refreshList()
            binding.swiperRefresh.isRefreshing = false
        }
    }


}