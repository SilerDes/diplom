package com.kazbekov.invent.main.trusted_user_and_admin.session.data

import android.content.Context
import com.kazbekov.invent.R
import com.kazbekov.invent.main.data.session.RemoteCreatedSession
import com.kazbekov.invent.main.data.session.RemoteSessions
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.network.InventResponse
import com.kazbekov.invent.network.Network
import retrofit2.Call

class SessionRepository(private val context: Context) {
    private val inventApiService = Network.apiService
    private val errors = Network.Errors

    suspend fun createSession(
        by: Int,
        onSuccessful: (RemoteCreatedSession) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.createSession(by).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = { remoteSession ->
                    remoteSession?.let {
                        onSuccessful(it)
                    } ?: onFailure(
                        InventResponse.UnsuccessfulResponse(
                            500,
                            context.getString(R.string.server_bad_response)
                        )
                    )
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self

                        else -> {
                            Logger.e(
                                REPOSITORY_LOG_TAG,
                                "Необработанный код ошибки: $errorCode $message"
                            )
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

    suspend fun stopSession(
        by: Int,
        sessionId: Int,
        onSuccessful: (Long) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.stopSession(by, sessionId).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = { remoteTimestamp ->
                    remoteTimestamp?.let {
                        onSuccessful(it.timestamp)
                    } ?: onFailure(
                        InventResponse.UnsuccessfulResponse(
                            500,
                            context.getString(R.string.server_bad_response)
                        )
                    )
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_session_deletion

                        else -> {
                            Logger.e(
                                REPOSITORY_LOG_TAG,
                                "Необработанный код ошибки: $errorCode $message"
                            )
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

    suspend fun deleteSession(
        by: Int,
        sessionId: Int,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.deleteSession(by, sessionId).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_session_deletion

                        else -> {
                            Logger.e(
                                REPOSITORY_LOG_TAG,
                                "Необработанный код ошибки: $errorCode $message"
                            )
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

    suspend fun getSessions(
        by: Int,
        onSuccessful: (RemoteSessions) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.getSessions(by).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = { remoteSessions ->
                    remoteSessions?.let {
                        onSuccessful(it)
                    } ?: onFailure(
                        InventResponse.UnsuccessfulResponse(
                            500,
                            context.getString(R.string.server_bad_response)
                        )
                    )
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self

                        else -> {
                            Logger.e(
                                REPOSITORY_LOG_TAG,
                                "Необработанный код ошибки: $errorCode $message"
                            )
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

    companion object {
        private const val REPOSITORY_LOG_TAG = "invent.session_repository"
    }
}