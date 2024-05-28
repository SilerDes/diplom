package com.kazbekov.invent.login

import android.app.Application
import androidx.lifecycle.*
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val authLiveData: MutableLiveData<InventResponse> = SingleLiveEvent()
    val auth: LiveData<InventResponse>
        get() = authLiveData
    private val repository = LoginRepository(application.applicationContext)

    private var currentCall: Call<*>? = null

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun login(code: Int, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.login(code, password) {
                authLiveData.postValue(it)
                currentCall = null
            }
        }
    }
}