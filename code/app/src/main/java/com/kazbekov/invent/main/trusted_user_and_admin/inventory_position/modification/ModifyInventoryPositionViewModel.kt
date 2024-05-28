package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.modification

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data.InventoryPositionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import com.kazbekov.invent.network.InventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import java.io.ByteArrayOutputStream

class ModifyInventoryPositionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryPositionRepository(application)
    private var currentCall: Call<*>? = null

    var bitmap: Bitmap? = null
        private set
    private val onBitmapAttachedLiveData = MutableLiveData<Boolean>()
    val onBitmapAttached: LiveData<Boolean>
        get() = onBitmapAttachedLiveData

    private val progressLiveData = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean>
        get() = progressLiveData

    private val onUpdateSuccessfulLiveData = SingleLiveEvent<Unit>()
    val onUpdateSuccessful: LiveData<Unit>
        get() = onUpdateSuccessfulLiveData

    private val onUpdateFailureLiveData = SingleLiveEvent<InventResponse.UnsuccessfulResponse>()
    val onUpdateFailure: LiveData<InventResponse.UnsuccessfulResponse>
        get() = onUpdateFailureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
    }

    fun setSelectedBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap
        onBitmapAttachedLiveData.postValue(true)
    }

    fun removeSelectedBitmap() {
        bitmap = null
        onBitmapAttachedLiveData.postValue(false)
    }

    fun updatePosition(
        by: Int,
        positionId: Int,
        titleOfficial: String,
        titleUser: String
    ) {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val encodedImage = if (bitmap != null) {
                val bos = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                val byteArray = bos.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            } else null

            currentCall = repository.updateInventoryPosition(
                by = by,
                updatable = positionId,
                titleOfficial = titleOfficial,
                titleUser = titleUser,
                encodedImage = encodedImage,
                onSuccessful = {
                    progressLiveData.postValue(false)
                    onUpdateSuccessfulLiveData.postValue(Unit)
                },
                onFailure = {
                    progressLiveData.postValue(false)
                    onUpdateFailureLiveData.postValue(it)
                }
            )
        }
    }
}