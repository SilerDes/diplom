package com.kazbekov.invent.main.trusted_user_and_admin.session.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.session.RemoteSessions
import com.kazbekov.invent.main.trusted_user_and_admin.session.data.SessionRepository
import com.kazbekov.invent.main.user.UserRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class SessionListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SessionRepository(application)
    private val userRepository = UserRepository(application)

    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onSuccessfulLiveData = MutableLiveData<RemoteSessions>()
    val onSuccessful: LiveData<RemoteSessions>
        get() = onSuccessfulLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall = null
    }

    fun getAllCreatedSessions(by: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = repository.getSessions(
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

    fun getUserSessions(by: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = userRepository.getUserSessions(
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

    fun putAwaySession(sessionId: Int) {
        onSuccessful.value ?: return
        val newList = onSuccessful.value!!.sessions.filter { it.id != sessionId }
        onSuccessfulLiveData.postValue(onSuccessful.value?.copy(sessions = newList))
    }

}