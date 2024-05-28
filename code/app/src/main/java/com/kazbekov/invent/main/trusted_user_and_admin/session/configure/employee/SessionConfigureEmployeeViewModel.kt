package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.employee

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class SessionConfigureEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository(application)
    private var currentCall: Call<*>? = null

    private val allEmployeesLiveData = MutableLiveData<MutableList<RemoteEmployee>>(mutableListOf())
    private val selectedEmployeesLiveData =
        MutableLiveData<MutableList<RemoteEmployee>>(mutableListOf())

    val allEmployees: LiveData<MutableList<RemoteEmployee>>
        get() = allEmployeesLiveData
    val selectedEmployees: LiveData<MutableList<RemoteEmployee>>
        get() = selectedEmployeesLiveData

    private val inProgressLiveData = MutableLiveData<Boolean>(false)
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    private fun requestEmployeeList(
        by: Int,
        onGet: (List<RemoteEmployee>) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ) {
        if (inProgressLiveData.value!!) return

        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)
            currentCall = repository.getEmployees(
                by = by,
                onSuccessful = onGet,
                onFailure = onFailure
            )
        }
    }

    fun getEmployees(by: Int) {
        requestEmployeeList(
            by = by,
            onGet = {
                inProgressLiveData.postValue(false)

                val selectedEmployeeList =
                    selectedEmployees.value!!.toSet().intersect(it.toSet())
                val allEmployees = it.toSet().minus(selectedEmployeeList)

                allEmployeesLiveData.postValue(allEmployees.toMutableList())
                selectedEmployeesLiveData.postValue(selectedEmployeeList.toMutableList())

            },
            onFailure = {
                inProgressLiveData.postValue(false)
                onFailureLiveData.postValue(it)
            }
        )

    }

    fun checkEmployeeListStatus(by: Int, onChecked: () -> Unit) {
        requestEmployeeList(
            by = by,
            onGet = {
                inProgressLiveData.postValue(false)

                val selectedEmployeeList =
                    selectedEmployees.value!!.toSet().intersect(it.toSet())
                val allEmployees = it.toSet().minus(selectedEmployeeList)

                allEmployeesLiveData.postValue(allEmployees.toMutableList())
                selectedEmployeesLiveData.postValue(selectedEmployeeList.toMutableList())

                onChecked()

            },
            onFailure = {
                inProgressLiveData.postValue(false)
                onFailureLiveData.postValue(it)
            }
        )
    }

    fun attachEmployee(employee: RemoteEmployee) {

        allEmployeesLiveData.value!!.remove(employee).takeIf { it }?.also {
            allEmployeesLiveData.postValue(allEmployeesLiveData.value)
        }
        selectedEmployeesLiveData.value!!.add(employee)
        selectedEmployeesLiveData.postValue(selectedEmployeesLiveData.value)
    }

    fun detachEmployee(employee: RemoteEmployee) {

        selectedEmployeesLiveData.value!!.remove(employee).takeIf { it }?.also {
            selectedEmployeesLiveData.postValue(selectedEmployeesLiveData.value)
        }
        allEmployeesLiveData.value!!.add(employee)
        allEmployeesLiveData.postValue(allEmployeesLiveData.value)
    }
}