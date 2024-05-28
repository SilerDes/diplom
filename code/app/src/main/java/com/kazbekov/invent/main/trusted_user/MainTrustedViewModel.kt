package com.kazbekov.invent.main.trusted_user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call

class MainTrustedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TrustedUserRepository()
    private var currentCall: Call<*>? = null

    private val isRefreshingLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> get() = isRefreshingLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

}