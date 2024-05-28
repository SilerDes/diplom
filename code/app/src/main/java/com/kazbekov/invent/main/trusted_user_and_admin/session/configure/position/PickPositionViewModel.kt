package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.position

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

class PickPositionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryPositionRepository(application)

    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>(false)
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val fullPositionListLiveData =
        MutableLiveData<MutableList<RemoteInventoryPosition>>(mutableListOf())
    val fullPositionList: LiveData<MutableList<RemoteInventoryPosition>>
        get() = fullPositionListLiveData

    private val selectedPositionListLiveData =
        MutableLiveData<MutableList<RemoteInventoryPosition>>(mutableListOf())
    val selectedPositionList: LiveData<MutableList<RemoteInventoryPosition>>
        get() = selectedPositionListLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun setInventoryPositionList(positions: List<RemoteInventoryPosition>) {
        fullPositionListLiveData.postValue(positions.toMutableList())
    }

    fun setSelectedInventoryPositionList(positions: List<RemoteInventoryPosition>) {
        selectedPositionListLiveData.postValue(positions.toMutableList())
    }

    fun getInventoryPositions() {
        if (inProgress.value!!) return

        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = repository.getPositions(
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    fullPositionListLiveData.postValue(it.toMutableList())
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }

    fun attachPosition(position: RemoteInventoryPosition) {

        fullPositionListLiveData.value!!.remove(position).takeIf { it }?.also {
            fullPositionListLiveData.postValue(fullPositionListLiveData.value)
        }
        selectedPositionListLiveData.value!!.add(position)
        selectedPositionListLiveData.postValue(selectedPositionListLiveData.value)
    }

    fun detachPosition(position: RemoteInventoryPosition) {

        selectedPositionListLiveData.value!!.remove(position).takeIf { it }?.also {
            selectedPositionListLiveData.postValue(selectedPositionListLiveData.value)
        }
        fullPositionListLiveData.value!!.add(position)
        fullPositionListLiveData.postValue(fullPositionListLiveData.value)
    }
}