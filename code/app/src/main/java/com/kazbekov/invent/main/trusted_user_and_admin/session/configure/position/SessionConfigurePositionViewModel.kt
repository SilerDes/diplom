package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.position

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.data.antoher.Employee2PositionsWrapper
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import com.kazbekov.invent.main.data.post.Employee2ItemsPost
import com.kazbekov.invent.main.data.post.InventoryItemsPost
import com.kazbekov.invent.main.data.post.Item2Post
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.data.InventoryItemRepository
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data.InventoryPositionRepository
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

class SessionConfigurePositionViewModel(application: Application) : AndroidViewModel(application) {

    private val employeeRepository = EmployeeRepository(application)
    private val inventoryPositionRepository = InventoryPositionRepository(application)
    private val inventoryItemRepository = InventoryItemRepository(application)

    private val employee2Positions =
        mutableMapOf<RemoteEmployee, MutableList<RemoteInventoryPosition>>()
    private var currentFullPositionList = mutableListOf<RemoteInventoryPosition>()

    private var currentCall: Call<*>? = null

    private val inProgressLiveData = MutableLiveData<Boolean>(false)
    val inProgress: LiveData<Boolean>
        get() = inProgressLiveData

    private val onCheckSuccessLiveData = SingleLiveEvent<Int>()
    val onCheckSuccess: LiveData<Int>
        get() = onCheckSuccessLiveData

    private val onInventoryItemsCreatedLiveData = SingleLiveEvent<Unit>()
    val onInventoryItemsCreated: LiveData<Unit>
        get() = onInventoryItemsCreatedLiveData

    private val onFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onFailureLiveData

    var needRequestPositions = true

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun setDefaultState(employees: List<RemoteEmployee>) {
        for (employee in employees) {
            employee2Positions[employee] = mutableListOf()
        }

    }

    fun updateEmployeeCase(
        employee: RemoteEmployee,
        case: List<RemoteInventoryPosition>,
        remainingList: List<RemoteInventoryPosition>
    ) {
        employee2Positions[employee] = case.toMutableList()
        currentFullPositionList = remainingList.toMutableList()

        needRequestPositions = false
    }

    fun getCase(employee: RemoteEmployee): Employee2PositionsWrapper {
        return Employee2PositionsWrapper(
            employee,
            employee2Positions[employee] ?: emptyList(),
            currentFullPositionList
        )

    }

    fun getEmployees(): List<RemoteEmployee> = employee2Positions.toList().map { it.first }

    fun syncEmployeesAndPositions(by: Int, requestCode: Int) {
        if (inProgress.value!!) return

        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)

            currentCall = employeeRepository.getEmployees(
                by = by,
                onSuccessful = { employees ->
                    val employeesToRemove = mutableListOf<RemoteEmployee>()
                    employee2Positions.forEach { (employee, _) ->
                        if (!employees.contains(employee)) {
                            employeesToRemove += employee
                        }
                    }
                    for (employee in employeesToRemove) {
                        employee2Positions.remove(employee)
                    }

                    currentCall = inventoryPositionRepository.getPositions(
                        onSuccessful = { allPositions ->
                            val allSelectedList = mutableListOf<RemoteInventoryPosition>()
                            val positionsToRemove = mutableListOf<RemoteInventoryPosition>()
                            employee2Positions.forEach { (_, positions) ->
                                for (position in positions) {
                                    if (!allPositions.contains(position)) {
                                        //positions.remove(position)
                                        positionsToRemove += position
                                    } else {
                                        allSelectedList += position
                                    }
                                }

                                employee2Positions.forEach { (_, positions) ->
                                    positions.removeAll(positionsToRemove)
                                }
                            }
                            currentFullPositionList.removeAll(allSelectedList)

                            inProgressLiveData.postValue(false)
                            onCheckSuccessLiveData.postValue(requestCode)
                        },
                        onFailure = {
                            inProgressLiveData.postValue(false)
                            onFailureLiveData.postValue(it)
                        }
                    )
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }

    fun postData(by: Int, sessionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            inProgressLiveData.postValue(true)

            val employee2ItemPost = employee2Positions.toList()
                .map {
                    Pair(it.first.code, it.second.map { position -> Item2Post(position.id) })
                }
                .map {
                    Employee2ItemsPost(it.first, it.second)
                }
            val data = InventoryItemsPost(sessionId, employee2ItemPost)

            currentCall = inventoryItemRepository.createInventoryItems(
                by = by,
                inventoryItems = data,
                onSuccessful = {
                    inProgressLiveData.postValue(false)
                    onInventoryItemsCreatedLiveData.postValue(Unit)
                },
                onFailure = {
                    inProgressLiveData.postValue(false)
                    onFailureLiveData.postValue(it)
                }
            )
        }
    }
}