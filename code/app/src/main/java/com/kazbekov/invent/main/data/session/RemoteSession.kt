package com.kazbekov.invent.main.data.session

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteSession(
    @SerializedName("id")
    val id: Int,
    @SerializedName("datetime_started")
    val startedAt: String,
    @SerializedName("datetime_finished")
    val finishedAt: String? = null,
    @SerializedName("created_by")
    val createdBy: RemoteEmployee
) : Parcelable
