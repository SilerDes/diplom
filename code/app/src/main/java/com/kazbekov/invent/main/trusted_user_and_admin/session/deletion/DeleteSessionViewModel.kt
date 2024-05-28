package com.kazbekov.invent.main.trusted_user_and_admin.session.deletion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.data.InventoryItemRepository
import com.kazbekov.invent.main.trusted_user_and_admin.session.data.SessionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class DeleteSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SessionRepository(application)
    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>()
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onSuccessfulDeletionLiveData = MutableLiveData<Unit>()
    val onSuccessfulDeletion: LiveData<Unit>
        get() = onSuccessfulDeletionLiveData

    private val onFailureDeletionLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailureDeletion: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureDeletionLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun deleteSession(by: Int, sessionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = repository.deleteSession(
                by = by,
                sessionId = sessionId,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onSuccessfulDeletionLiveData.postValue(Unit)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureDeletionLiveData.postValue(it)
                }
            )
        }
    }
}