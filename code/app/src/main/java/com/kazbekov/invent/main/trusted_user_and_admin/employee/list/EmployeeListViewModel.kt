package com.kazbekov.invent.main.trusted_user_and_admin.employee.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.R
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class EmployeeListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository(application)
    private var currentCall: Call<*>? = null

    var employeeListFilter =
        arrayOf(
            application.resources.getString(R.string.trusted_status_user_remote),
            application.resources.getString(R.string.trusted_status_admin_remote),
            application.resources.getString(R.string.trusted_status_god_remote)
        )

    private val progressLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    val progress: LiveData<Boolean>
        get() = progressLiveData

    private val employeeListLiveData: MutableLiveData<List<RemoteEmployee>> =
        MutableLiveData(listOf())
    val employeeList: LiveData<List<RemoteEmployee>>
        get() = employeeListLiveData

    private val failureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val failure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = failureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun getEmployees(by: Int) {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.getEmployees(
                by,
                {
                    Logger.d("EmployeeListViewModel", it.toString())
                    progressLiveData.postValue(false)
                    employeeListLiveData.postValue(it)
                },
                {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it)
                }
            )
        }
    }
}