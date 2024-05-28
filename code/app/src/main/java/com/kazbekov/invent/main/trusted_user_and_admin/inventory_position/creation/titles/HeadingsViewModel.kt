package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.titles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data.InventoryPositionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class HeadingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryPositionRepository(application)
    private var currentCall: Call<*>? = null

    private val progressLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val progress: LiveData<Boolean>
        get() = progressLiveData

    private val failureLiveData = SingleLiveEvent<String>()
    val failure: LiveData<String>
        get() = failureLiveData

    private val successfulLiveData = SingleLiveEvent<Pair<String, String>>()
    val successful: LiveData<Pair<String, String>>
        get() = successfulLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun checkAvailability(
        by: Int,
        title: String,
        titleNonOfficial: String
    ) {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.checkTitleAvailability(
                by,
                title,
                {
                    progressLiveData.postValue(false)
                    successfulLiveData.postValue(Pair(title, titleNonOfficial))
                },
                {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it.error)
                }
            )
        }
    }
}