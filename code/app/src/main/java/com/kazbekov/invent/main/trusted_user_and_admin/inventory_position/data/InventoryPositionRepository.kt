package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data

import android.content.Context
import com.kazbekov.invent.R
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPositions
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.network.InventResponse
import com.kazbekov.invent.network.Network
import retrofit2.Call

class InventoryPositionRepository(private val context: Context) {
    private val inventApiService = Network.apiService
    private val errors = Network.Errors

    fun getPositions(
        onSuccessful: (List<RemoteInventoryPosition>) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<RemoteInventoryPositions> {
        return inventApiService.getInventoryPositions().apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful(it!!.positions)
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key

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

    fun checkTitleAvailability(
        by: Int,
        titleOfficial: String,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.checkAvailabilityTitle(by, titleOfficial).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key

                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status

                        errorCode == 423 && message == errors.ERROR_423_LOCKED -> R.string.server_error_403_availability_title

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

    suspend fun uploadInventoryPosition(
        by: Int,
        titleOfficial: String,
        titleNonOfficial: String,
        encodedImage: String,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.uploadInventoryPosition(
            by,
            titleOfficial,
            titleNonOfficial,
            encodedImage
        ).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key

                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status

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

    suspend fun searchPositions(
        s: String,
        onSuccessful: (List<RemoteInventoryPosition>) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.searchInventoryPositions(s).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful(it?.positions ?: emptyList())
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key

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

    suspend fun deleteInventoryPosition(
        by: Int,
        toDelete: Int,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.deleteInventoryPosition(by, toDelete).apply {
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
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_position_deletion

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

    suspend fun updateInventoryPosition(
        by: Int,
        updatable: Int,
        titleOfficial: String,
        titleUser: String,
        encodedImage: String? = null,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        val call = encodedImage?.let {
            inventApiService.updateInventoryPosition(by, updatable, titleOfficial, titleUser, it)
        } ?: run {
            inventApiService.updateInventoryPosition(by, updatable, titleOfficial, titleUser)
        }

        return call.apply {
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
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND -> R.string.server_error_404_position

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
        private const val REPOSITORY_LOG_TAG = "invent.inventory_position_repository"
    }
}