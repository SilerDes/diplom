package com.kazbekov.invent.main.trusted_user_and_admin.employee.deletion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class DeleteEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private var currentCall: Call<*>? = null

    private val repository = EmployeeRepository(application)

    private val successfulLiveData: MutableLiveData<Unit> = MutableLiveData()
    val successful: LiveData<Unit>
        get() = successfulLiveData
    private val failureLiveData: MutableLiveData<InventResponse.UnsuccessfulResponse> =
        SingleLiveEvent()
    val failure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = failureLiveData
    private val progressLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val progress: LiveData<Boolean>
        get() = progressLiveData

    fun deleteEmployee(by: Int, d: Int) {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.deleteEmployee(
                by = by,
                d = d,
                onSuccessful = {
                    progressLiveData.postValue(false)
                    successfulLiveData.postValue(Unit)
                },
                onFailure = {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it)
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }
}