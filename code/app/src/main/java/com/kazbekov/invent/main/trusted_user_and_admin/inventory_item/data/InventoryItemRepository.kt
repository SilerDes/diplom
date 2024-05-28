package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.data

import android.content.Context
import com.google.gson.Gson
import com.kazbekov.invent.R
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItems
import com.kazbekov.invent.main.data.post.InventoryItemsPost
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.network.InventResponse
import com.kazbekov.invent.network.Network
import retrofit2.Call

class InventoryItemRepository(private val context: Context) {
    private val inventApiService = Network.apiService
    private val errors = Network.Errors

    suspend fun getInventoryItems(
        by: Int,
        sessionId: Int,
        onSuccessful: (RemoteInventoryItems) -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.getInventoryItems(by, sessionId).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = { inventoryItems ->
                    inventoryItems?.let {
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

    suspend fun updateInventoryItemState(
        id: Int,
        newState: Int,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.updateInventoryItemState(id, newState).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_inventory_item_deletion

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

    suspend fun deleteInventoryItem(
        by: Int,
        id: Int,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        return inventApiService.deleteInventoryItem(by, id).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 403 && message == errors.ERROR_403_STATUS -> R.string.server_error_403_status
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_inventory_item_deletion
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

    suspend fun createInventoryItems(
        by: Int,
        inventoryItems: InventoryItemsPost,
        onSuccessful: () -> Unit,
        onFailure: (InventResponse.UnsuccessfulResponse) -> Unit
    ): Call<*> {
        //val jsonBody = Gson().toJson(inventoryItems)
        return inventApiService.createInventoryItems(by, inventoryItems).apply {
            Network.Common.execute(
                call = this,
                onSuccessful = {
                    onSuccessful()
                },
                onFailure = { errorCode, message ->
                    val errorMessageRes = when {
                        errorCode == 400 && message == errors.ERROR_400_BAD_REQUEST -> R.string.server_error_400_bad_request
                        errorCode == 403 && message == errors.ERROR_403_API_KEY -> R.string.server_error_403_api_key
                        errorCode == 404 && message == errors.ERROR_404_NOT_FOUND_SELF -> R.string.server_error_404_self
                        errorCode == 410 && message == errors.ERROR_410_GONE -> R.string.server_error_410_session_deletion
                        errorCode == 409 && message == errors.ERROR_409_CONFLICT -> R.string.server_error_409_session_finished
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

    companion object {
        private const val REPOSITORY_LOG_TAG = "invent.inventory_item_repository"
    }
}