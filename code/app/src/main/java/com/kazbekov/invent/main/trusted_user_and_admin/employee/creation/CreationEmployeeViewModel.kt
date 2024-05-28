package com.kazbekov.invent.main.trusted_user_and_admin.employee.creation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class CreationEmployeeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EmployeeRepository(application)
    private var currentCall: Call<*>? = null

    private val onSuccessfulLiveData = SingleLiveEvent<Unit>()
    val onSuccessful: LiveData<Unit>
        get() = onSuccessfulLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun signUp(
        code: Int,
        password: String,
        firstName: String,
        lastName: String,
        trustedStatusId: Int,
        adminCode: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            currentCall = repository.signUp(
                code,
                password,
                firstName,
                lastName,
                trustedStatusId,
                adminCode,
                {
                    onSuccessfulLiveData.postValue(Unit)
                },
                {
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }
}