package ru.netology.nmedia.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.repository.user.UserRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignInViewModel
@Inject constructor(
    private val repository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _authenticated = SingleLiveEvent<Unit>()
    val authenticated: LiveData<Unit>
        get() = _authenticated

    fun updateUser(login: String, pass: String) = viewModelScope.launch {
        try {
            if (login != "" && pass != "") {
                repository.updateUser(login, pass)
                _authenticated.value = Unit
            } else {
                Toast.makeText(getApplication(), R.string.empty_field, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(getApplication(), R.string.invalid_user, Toast.LENGTH_LONG).show()
        }
    }
}