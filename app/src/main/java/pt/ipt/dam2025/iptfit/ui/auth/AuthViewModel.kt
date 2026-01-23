package pt.ipt.dam2025.iptfit.ui.auth

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import pt.ipt.dam2025.iptfit.data.local.entity.User
import pt.ipt.dam2025.iptfit.data.repository.IPTFitRepository

/**
 * ViewModel para autenticação (Login e Registo)
 */
class AuthViewModel(private val repository: IPTFitRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.loginUser(username, password)
                _loginResult.postValue(result)
            } catch (e: Exception) {
                _loginResult.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun register(user: User) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.registerUser(user)
                _registerResult.postValue(result)
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}

/**
 * Factory para criar o AuthViewModel com dependências
 */
class AuthViewModelFactory(
    private val repository: IPTFitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}