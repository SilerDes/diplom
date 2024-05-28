package com.kazbekov.invent.main.data.employee

import com.google.gson.annotations.SerializedName

data class RemotePassword(
    @SerializedName("pass")
    val pass: String
)
