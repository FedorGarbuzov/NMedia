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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.post.PostRepository
import ru.netology.nmedia.ui.AppActivity
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService @Inject constructor(
    private val repository: PostRepository
) : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    @Inject
    lateinit var auth: AppAuth

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
        CoroutineScope(Dispatchers.Default).launch {
            val userId = auth.authStateFlow.value.id
            val remoteId = gson.fromJson(
                    message.data[content],
                    Message::class.java
            ).recipientId
            println(remoteId)
            if (remoteId == userId || remoteId == null) {
                try {
                    when (message.data[action]?.let { Action.valueOf(it) }) {
                        Action.LIKE ->
                            handleLike(
                                    gson.fromJson(
                                            message.data[content],
                                            RemoteClass::class.java
                                    )
                            )

                        Action.SHARE ->
                            handleShare(
                                    gson.fromJson(
                                            message.data[content],
                                            RemoteClass::class.java
                                    )
                            )

                        Action.POST -> handlePost(
                                gson.fromJson(
                                        message.data[content],
                                        Post::class.java
                                )
                        )
                        else -> handleMessage(message)
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            } else {
                auth.sendPushToken()
            }
        }
    }


    override fun onNewToken(token: String) {
        println(token)
        auth.sendPushToken(token)
    }

    private suspend fun handleLike(content: RemoteClass) {
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
        repository.likeById(content.postId)
    }

    private suspend fun handleShare(content: RemoteClass) {
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
        repository.shareById(content.postId)
    }

    private suspend fun handlePost(content: Post) {
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
        repository.processWork(content.id)

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

data class Message(
        val recipientId: Long?,
        val content: String,
)