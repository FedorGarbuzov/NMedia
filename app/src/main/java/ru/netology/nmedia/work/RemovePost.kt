package ru.netology.nmedia.work

import android.content.Context
import android.content.res.Resources
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.post.PostRepositoryImp
import ru.netology.nmedia.work.SavePostWorker.Companion.postKey
import java.lang.NullPointerException

class RemovePostWorker(
        applicationContext: Context,
        params: WorkerParameters
) : CoroutineWorker(applicationContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(postKey, 0L)
        if (id == 0L) {
            Result.failure()
        }

        val repository = PostRepositoryImp(
                AppDb.getInstance(applicationContext).postDao(),
                AppDb.getInstance(applicationContext).postWorkDao()
        )

        return try {
            repository.removeByIdWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}