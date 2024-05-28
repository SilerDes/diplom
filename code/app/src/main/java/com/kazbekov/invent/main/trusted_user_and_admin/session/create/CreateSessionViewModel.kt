package com.kazbekov.invent.main.trusted_user_and_admin.session.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.session.RemoteCreatedSession
import com.kazbekov.invent.main.trusted_user_and_admin.session.data.SessionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class CreateSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepository(application)
    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onSuccessfulLiveData = MutableLiveData<RemoteCreatedSession>()
    val onSuccessful: LiveData<RemoteCreatedSession>
        get() = onSuccessfulLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun createSession(by: Int) {
        inProgressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.createSession(
                by = by,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onSuccessfulLiveData.postValue(it)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }
}