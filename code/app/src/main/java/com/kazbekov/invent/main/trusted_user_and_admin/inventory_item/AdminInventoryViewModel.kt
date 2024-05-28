package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItems
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.data.InventoryItemRepository
import com.kazbekov.invent.main.trusted_user_and_admin.session.data.SessionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class AdminInventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryItemRepository = InventoryItemRepository(application)
    private val sessionRepository = SessionRepository(application)
    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onSuccessfulLiveData = MutableLiveData<RemoteInventoryItems>()
    val onSuccessful: LiveData<RemoteInventoryItems>
        get() = onSuccessfulLiveData

    private val onSessionStoppedLiveData = SingleLiveEvent<Unit>()
    val onSessionStopped: LiveData<Unit>
        get() = onSessionStoppedLiveData

    private val onInventoryItemDeletedLiveData = SingleLiveEvent<Int>()
    val onInventoryItemDeleted: LiveData<Int>
        get() = onInventoryItemDeletedLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        currentCall?.cancel()
        super.onCleared()
    }

    fun getInventoryItems(by: Int, sessionId: Int) {
        if (inProgressLiveData.value == true) return

        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = inventoryItemRepository.getInventoryItems(
                by = by,
                sessionId = sessionId,
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

    fun stopSession(by: Int, sessionId: Int) {
        if (inProgressLiveData.value == true) return

        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = sessionRepository.stopSession(
                by = by,
                sessionId = sessionId,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onSessionStoppedLiveData.postValue(Unit)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }

    fun deleteSession(by: Int, sessionId: Int) {
        if (inProgressLiveData.value == true) return
    }

}