package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.AppActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImp
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            when (message.data[action]?.let { Action.valueOf(it) }) {
                Action.LIKE -> handleLike(gson.fromJson(message.data[content], RemoteClass::class.java))
                Action.SHARE -> handleShare(gson.fromJson(message.data[content], RemoteClass::class.java))
                Action.POST -> handlePost(gson.fromJson(message.data[content], Post::class.java))
                else -> handleMessage(message)
            }
        } catch (e: IllegalArgumentException) {}
    }

    override fun onNewToken(token: String) {
        println(token)
    }

    private fun handleLike(content: RemoteClass) {
        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_like)
                .setContentTitle(
                        getString(
                                R.string.notification_user_liked,
                                content.userName,
                                content.postAuthor
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        getRepository().likeById(content.postId)
    }

    private fun handleShare(content: RemoteClass) {
        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_share)
                .setContentTitle(
                        getString(
                                R.string.notification_user_share,
                                content.userName,
                                content.postAuthor
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        getRepository().shareById(content.postId)
    }

    private fun handlePost(content: Post) {
        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_post)
                .setContentTitle(
                        getString(
                                R.string.notification_user_post_title,
                                content.author
                        )
                )
                .setContentText(content.content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.content))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        getRepository().saveAsync(content, object : PostRepository.Callback<Post>{})
    }

    private fun handleMessage(message: RemoteMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["content"])
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["content"]))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(applicationContext)
                .notify(Random.nextInt(100_000), notification)
    }

    private fun getRepository(): PostRepository {
        return PostRepositoryImp()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, AppActivity::class.java)
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(this, 0, intent, 0)
    }
}

enum class Action {
    LIKE, SHARE, POST
}

data class RemoteClass(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postAuthor: String,
        val content: String,
        val published: String,
)