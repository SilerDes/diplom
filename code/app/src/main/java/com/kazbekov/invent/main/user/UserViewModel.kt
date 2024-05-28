package com.kazbekov.invent.main.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItem
import com.kazbekov.invent.main.data.inventory_item.RemoteItem
import com.kazbekov.invent.main.data.inventory_item.RemoteItems
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.data.InventoryItemRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryItemRepository = InventoryItemRepository(application)
    private val userRepository = UserRepository(application)
    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>(false)
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onSuccessfulUpdateLiveData = SingleLiveEvent<Unit>()
    val onSuccessfulUpdate: LiveData<Unit>
        get() = onSuccessfulUpdateLiveData

    private val onFailureUpdateLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailureUpdate: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureUpdateLiveData

    private val onInventoryItemsFetchedLiveData = MutableLiveData<List<RemoteItem>>()
    val onInventoryItemFetched: LiveData<List<RemoteItem>>
        get() = onInventoryItemsFetchedLiveData

    override fun onCleared() {
        currentCall?.cancel()
        super.onCleared()
    }

    fun updateInventoryItem(id: Int, count: Int) {
        if (count < 0) {
            onFailureUpdateLiveData.postValue(
                InventResponse.UnsuccessfulResponse(
                    UserInventoryFragment.CODE_NEGATIVE_COUNT, ""
                )
            )
            return
        }
        if (inProgressLiveData.value == true) return
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)

            currentCall = inventoryItemRepository.updateInventoryItemState(
                id = id,
                newState = count,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onSuccessfulUpdateLiveData.postValue(Unit)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureUpdateLiveData.postValue(it)
                }
            )
        }
    }

    fun getInventoryItems(by: Int, sessionId: Int) {
        if (inProgressLiveData.value == true) return
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)

            currentCall = userRepository.getUserInventoryItems(
                by = by,
                sessionId = sessionId,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onInventoryItemsFetchedLiveData.postValue(it.items)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                }
            )
        }
    }
}