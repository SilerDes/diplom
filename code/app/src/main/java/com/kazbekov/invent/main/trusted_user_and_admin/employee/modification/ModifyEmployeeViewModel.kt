package com.kazbekov.invent.main.trusted_user_and_admin.employee.modification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.trusted_user_and_admin.employee.modification.common.ProgressType
import com.kazbekov.invent.main.trusted_user_and_admin.employee.modification.common.UpdatedEmployee
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class ModifyEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository(application)

    private var currentCall: Call<*>? = null

    private val progressLiveData = MutableLiveData<ProgressType>(ProgressType.NO_PROGRESS)
    val progress: LiveData<ProgressType>
        get() = progressLiveData

    private val passwordSuccessfulLiveData = MutableLiveData<String>()
    val passwordSuccessful: LiveData<String>
        get() = passwordSuccessfulLiveData
    private val passwordFailureLiveData = MutableLiveData<InventResponse.UnsuccessfulResponse>()
    val passwordFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = passwordFailureLiveData

    private val employeeUpdateSuccessfulLiveData = MutableLiveData<UpdatedEmployee>()
    val employeeUpdateSuccessful: LiveData<UpdatedEmployee>
        get() = employeeUpdateSuccessfulLiveData
    private val employeeUpdateFailureLiveData =
        MutableLiveData<InventResponse.UnsuccessfulResponse>()
    val employeeUpdateFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = employeeUpdateFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun requestPassword(
        by: Int,
        requested: Int
    ) {
        progressLiveData.postValue(ProgressType.PASSWORD_REQUEST_PROGRESS)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.requestPassword(
                by = by,
                r = requested,
                onSuccessful = {
                    passwordSuccessfulLiveData.postValue(it)
                    progressLiveData.postValue(ProgressType.NO_PROGRESS)
                },
                onFailure = {
                    passwordFailureLiveData.postValue(it)
                    progressLiveData.postValue(ProgressType.NO_PROGRESS)
                }
            )
        }
    }

    fun updateEmployee(
        by: Int,
        updatable: Int,
        firstName: String,
        secondName: String,
        status: Int,
        password: String? = null
    ) {
        progressLiveData.postValue(ProgressType.SAVE_CHANGES_PROGRESS)
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.updateEmployee(
                by = by,
                updatable = updatable,
                firstName = firstName,
                secondName = secondName,
                trustedStatusId = status,
                password = password,
                onSuccessful = {
                    employeeUpdateSuccessfulLiveData.postValue(
                        UpdatedEmployee(
                            updatable,
                            firstName,
                            secondName,
                            status
                        )
                    )
                },
                onFailure = {
                    employeeUpdateFailureLiveData.postValue(it)
                },
                onAny = {
                    progressLiveData.postValue(ProgressType.NO_PROGRESS)
                }
            )
        }
    }
}