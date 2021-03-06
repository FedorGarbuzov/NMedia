package ru.netology.nmedia.viewModel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.post.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.work.RemovePostsWorker
import ru.netology.nmedia.work.SavePostsWorker
import ru.netology.nmedia.work.SavePostsWorker.Companion.postKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

val emptyPost = Post(
    id = 0L,
    author = "",
    authorId = 0,
    authorAvatar = "",
    published = "",
    content = "",
    share = 0,
    likes = 0,
    views = 0,
    likedByMe = false,
    uploadedToServer = false,
    attachment = null,
    read = true
)

private val noPhoto = PhotoModel()

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val workManager: WorkManager,
    auth: AppAuth,
    application: Application
) : AndroidViewModel(application) {

    private val dbPosts: LiveData<List<Post>> =
        repository.dbPosts

    private val cached = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators { before, after ->
                before ?: return@insertSeparators null
                after ?: return@insertSeparators null
                if (before.published > OffsetDateTime.now().minusHours(24).toEpochSecond().toString() &&
                    after.published < OffsetDateTime.now().minusHours(24).toEpochSecond().toString()
                ) {
                    insertDate(R.string.yesterday)
                } else
                if (before.published > OffsetDateTime.now().minusHours(48).toEpochSecond().toString() &&
                    after.published < OffsetDateTime.now().minusHours(48).toEpochSecond().toString()
                ) {
                    insertDate(R.string.long_time_ago)
                } else
                    if (before.id.rem(5) != 0L) null else
                        Ad(
                            Random.nextLong(),
                            "https://netology.ru",
                            "figma.jpg"
                        )
            }
        }
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { item ->
                    if (item !is Post) item else item.copy(ownedByMe = item.authorId == myId)
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val getNewer: LiveData<List<Post>> = dbPosts.switchMap { posts ->
        repository.getNewer(posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData()
    }

    fun loadNewer() = viewModelScope.launch {
        repository.loadNewer()
    }

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getLatest()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(errorLoading = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(
                        it,
                        if (it.attachment?.url != _photo.value?.uri.toString()) {
                            _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                        } else {
                            null
                        }
                    )
                    val data = workDataOf(postKey to id)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = OneTimeWorkRequestBuilder<SavePostsWorker>()
                        .setInputData(data)
                        .setConstraints(constraints)
                        .build()
                    workManager.enqueue(request)
                    delay(2_000)
                    loadPosts()
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(errorSaving = true)
                }
            }
            _postCreated.call()
        }
        edited.value = emptyPost
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        when {
            _photo.value?.uri == null -> {
                edited.value = edited.value?.copy(attachment = null)
            }
            edited.value?.content == text -> {
                return
            }
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                val data = workDataOf(postKey to id)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<RemovePostsWorker>()
                    .setInputData(data)
                    .setConstraints(constraints)
                    .build()
                workManager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun likedByMe(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeByMe(id)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun unlikedByMe(id: Long) {
        viewModelScope.launch {
            try {
                repository.unlikeByMe(id)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun likeById(id: Long) = viewModelScope.launch { repository.likeById(id) }
    fun shareById(id: Long) = viewModelScope.launch { repository.shareById(id) }

    private fun insertDate(date: Int): Date {
        return Date(Random.nextLong(), date)
    }
}
