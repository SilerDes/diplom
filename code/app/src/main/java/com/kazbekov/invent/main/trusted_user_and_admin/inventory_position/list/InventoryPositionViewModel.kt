package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data.InventoryPositionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class InventoryPositionViewModel(application: Application) : AndroidViewModel(application) {

    private var currentCall: Call<*>? = null
    private val repository = InventoryPositionRepository(application)

    private val progressLiveData = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean>
        get() = progressLiveData

    private val sourceSetLiveData = MutableLiveData<List<RemoteInventoryPosition>>()
    val sourceSet: LiveData<List<RemoteInventoryPosition>>
        get() = sourceSetLiveData

    private val failureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val failure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = failureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun getPositions() {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.getPositions(
                onSuccessful = {
                    progressLiveData.postValue(false)
                    sourceSetLiveData.postValue(it)
                },
                onFailure = {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it)
                }
            )
        }
    }
}