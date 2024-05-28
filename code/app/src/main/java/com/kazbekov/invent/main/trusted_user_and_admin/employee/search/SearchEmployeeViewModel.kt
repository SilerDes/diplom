package com.kazbekov.invent.main.trusted_user_and_admin.employee.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class SearchEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository(application)

    private val progressLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val progress: LiveData<Boolean>
        get() = progressLiveData
    private val searchResultLiveData: MutableLiveData<RemoteEmployee?> = MutableLiveData()
    val searchResult: LiveData<RemoteEmployee?>
        get() = searchResultLiveData
    private val failureLiveData = SingleLiveEvent<String>()
    val failure: LiveData<String>
        get() = failureLiveData

    private var currentCall: Call<*>? = null
    var currentEmployee: RemoteEmployee? = null
        set(value) {
            field = value
            searchResultLiveData.postValue(value)
        }

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun searchEmployee(by: Int, code: Int) {
        progressLiveData.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.searchEmployee(
                by,
                code,
                {
                    progressLiveData.postValue(false)
                    currentEmployee = it
                },
                {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it.error)
                }
            )
        }
    }
}