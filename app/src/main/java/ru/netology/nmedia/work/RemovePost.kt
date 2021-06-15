package ru.netology.nmedia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.netology.nmedia.repository.post.PostRepository
import ru.netology.nmedia.work.SavePostsWorker.Companion.postKey
import javax.inject.Inject
import javax.inject.Singleton

class RemovePostsWorker(
        applicationContext: Context,
        params: WorkerParameters,
        private val repository: PostRepository
) : CoroutineWorker(applicationContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(postKey, 0L)
        if (id == 0L) {
            Result.failure()
        }

        return try {
            repository.removeByIdWork(id)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@Singleton
class RemovePostsWorkerFactory @Inject constructor(
    private val repository: PostRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        RemovePostsWorker::class.java.name ->
            RemovePostsWorker(appContext, workerParameters, repository)
        else -> null
    }
}