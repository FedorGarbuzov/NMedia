package ru.netology.nmedia.viewModel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.R
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.repository.user.UserRepository
import ru.netology.nmedia.repository.user.UserRepositoryImp
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.lang.Exception

private val noPhoto = PhotoModel()

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepositoryImp()

    private val _registered = SingleLiveEvent<Unit>()
    val registered: LiveData<Unit>
        get() = _registered

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun createUser(login: String, pass: String, name: String) = viewModelScope.launch {
        try {
            when (_photo.value) {
                noPhoto ->
                    if (login != "" && pass != "" && name != "") {
                        repository.createUser(login, pass, name)
                        _registered.value = Unit
                    } else {
                        Toast.makeText(getApplication(), R.string.empty_field, Toast.LENGTH_LONG).show()
                    }
                else ->
                    photo.value?.uri?.let {
                        repository.createWithPhoto(
                                login.toRequestBody("text/plain".toMediaType()),
                                pass.toRequestBody("text/plain".toMediaType()),
                                name.toRequestBody("text/plain".toMediaType()),
                                MediaUpload(it.toFile())
                        )
                        _registered.value = Unit
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
        }
    }
}