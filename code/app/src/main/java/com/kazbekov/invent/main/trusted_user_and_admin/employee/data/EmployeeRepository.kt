package com.kazbekov.invent.main.trusted_user_and_admin.employee.data

import android.content.Context
import android.util.Log
import com.kazbekov.invent.R
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.network.InventResponse
import com.kazbekov.invent.network.Network
import retrofit2.Call

class EmployeeRepository(private val context: Context) {
    private val inventApiService = Network.apiService
    private val errors = Network.Errors

    suspend fun signUp(
        code: Int,
        password: String,
        firstName: String,
        lastName: String,
        trustedStatusId: Int,
        adminCode: Int,
        onSuccessful: () -> Unit,
        onFailure: (e: InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.signup(
            employeeCode = code,
            firstName = firstName,
            lastName = lastName,
            password = password,
            trustedStatusId = trustedStatusId,
            adminCode = adminCode
        ).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404_self

                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key

                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status

                        errorCode == 409 && message == errors.ERROR_409_CONFLICT -> R.string.server_error_409

                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessageRes)
                        )
                    )
                }
            )
        }
    }

    suspend fun deleteEmployee(
        by: Int,
        d: Int,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.deleteEmployee(by, d).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode: Int, message: String ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_410_GONE -> R.string.server_error_410_employee_deletion
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        errorCode == 423 && message == errors.ERROR_423_LOCKED -> R.string.server_error_423
                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }

                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessageRes)
                        )
                    )
                }
            )
        }
    }

    fun getEmployees(
        by: Int,
        onSuccessful: (List<RemoteEmployee>) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.getEmployeeList(by).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful(it!!.employees)
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_api_key
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404_self
                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessageRes)
                        )
                    )
                }
            )
        }
    }

    suspend fun searchEmployee(
        by: Int,
        code: Int,
        onSuccessful: (RemoteEmployee) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.getEmployee(by, code).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    it!!.employees.takeIf { list -> list.isNotEmpty() }?.let { list ->
                        onSuccessful(list.first())
                    } ?: onFailure(
                        InventResponse.UnsuccessfulResponse(
                            404,
                            context.getString(R.string.server_error_404)
                        )
                    )
                },
                onFailure = { errorCode, message ->
                    val errorMessage = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessage)
                        )
                    )
                }
            )
        }
    }

    suspend fun requestPassword(
        by: Int,
        r: Int,
        onSuccessful: (pass: String) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.requestPassword(by = by, requested = r).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful(it!!.pass)
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_employee_deletion
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessageRes)
                        )
                    )
                }
            )
        }
    }

    suspend fun updateEmployee(
        by: Int,
        updatable: Int,
        firstName: String,
        secondName: String,
        trustedStatusId: Int,
        password: String? = null,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit,
        onAny: () -> Unit
    ): Call<*> {
        val call = password?.let { pass ->
            inventApiService.updateEmployee(
                by = by,
                u = updatable,
                firstName = firstName,
                secondName = secondName,
                trustedStatusId = trustedStatusId,
                password = pass
            )
        } ?: run {
            inventApiService.updateEmployee(
                by = by,
                u = updatable,
                firstName = firstName,
                secondName = secondName,
                trustedStatusId = trustedStatusId
            )
        }
        return call.apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                    onAny()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onFailure(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessageRes)
                        )
                    )
                    onAny()
                }
            )
        }
    }

    companion object {
        private const val REPOSITORY_LOG_TAG = "invent.employee_repository"

        private fun log(message: String) {
            Log.d(REPOSITORY_LOG_TAG, message)
        }
    }
}