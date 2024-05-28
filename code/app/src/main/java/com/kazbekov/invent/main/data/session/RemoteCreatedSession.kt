package com.kazbekov.invent.main.data.session

import com.google.gson.annotations.SerializedName

data class RemoteCreatedSession(
    @SerializedName("id")
    val id: Int,
    @SerializedName("datetime_started")
    val datetimeStarted: String,
    @SerializedName("datetime_finished")
    val datetimeFinished: String? = null,
    @SerializedName("created_by")
    val createdBy: Int
)
