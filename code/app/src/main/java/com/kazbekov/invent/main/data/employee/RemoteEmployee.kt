package com.kazbekov.invent.main.data.employee

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteEmployee(
    @SerializedName("code")
    val code: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val secondName: String,
    @SerializedName("trusted_status_id")
    val trustedStatusId: Int,
    @SerializedName("trusted_status_title")
    val trustedStatus: String
) : Parcelable
