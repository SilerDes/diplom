package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.result

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.data.InventoryPositionRepository
import com.kazbekov.invent.main.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import java.io.ByteArrayOutputStream

class PositionPreviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryPositionRepository(application)
    var bitmap: Bitmap? = null
    private var currentCall: Call<*>? = null

    private val progressLiveData = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean>
        get() = progressLiveData

    private val successfulLiveData = SingleLiveEvent<Unit>()
    val successful: LiveData<Unit>
        get() = successfulLiveData

    private val failureLiveData = SingleLiveEvent<String>()
    val failure: LiveData<String>
        get() = failureLiveData

    override fun onCleared() {
        super.onCleared()

        currentCall?.cancel()
        currentCall = null
        bitmap = null
    }

    fun createInventoryPosition(
        by: Int,
        titleOfficial: String,
        titleNonOfficial: String
    ) {
        progressLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val bos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val byteArray = bos.toByteArray()

            val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
            currentCall = repository.uploadInventoryPosition(
                by = by,
                titleOfficial = titleOfficial,
                titleNonOfficial = titleNonOfficial,
                encodedImage = encodedImage,
                onSuccessful = {
                    progressLiveData.postValue(false)
                    successfulLiveData.postValue(Unit)
                },
                onFailure = {
                    progressLiveData.postValue(false)
                    failureLiveData.postValue(it.error)
                }
            )
        }
    }

}