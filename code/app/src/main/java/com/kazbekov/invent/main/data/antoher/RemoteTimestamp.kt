package com.kazbekov.invent.main.data.antoher

import com.google.gson.annotations.SerializedName

data class RemoteTimestamp(
    @SerializedName("datetime_finished")
    val timestamp: Long
)
