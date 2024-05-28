package com.kazbekov.invent.login

import android.content.Context
import android.util.Log
import com.kazbekov.invent.R
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.EmployeeRepository
import com.kazbekov.invent.network.InventResponse
import com.kazbekov.invent.network.Network
import retrofit2.Call


class LoginRepository(private val context: Context) {
    private val inventApiService = Network.apiService
    private val errors = Network.Errors

    suspend fun login(
        code: Int,
        password: String,
        onResponse: (InventResponse) -> Unit
    ): Call<*> {
        return inventApiService.auth(code, password).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = { response ->
                    response?.let { onResponse(it) }
                        ?: onResponse(
                            InventResponse.UnsuccessfulResponse(
                                Network.Common.SERVER_ANY_500_OR_INTERNET,
                                "response body is null"
                            )
                        )
                },
                onFailure = { errorCode, message ->
                    val errorMessage = when {
                        errorCode == 401 && message == errors.ERROR_401_UNAUTHORIZED -> R.string.server_error_401
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404

                        else -> {
                            log("Необработанный код ошибки: $errorCode $message")
                            R.string.server_error_any_500
                        }
                    }
                    onResponse(
                        InventResponse.UnsuccessfulResponse(
                            errorCode,
                            context.getString(errorMessage)
                        )
                    )
                }
            )
        }
    }

    companion object {
        private const val REPOSITORY_LOG_TAG = "invent.login_repository"

        private fun log(message: String) {
            Log.d(REPOSITORY_LOG_TAG, message)
        }
    }
}