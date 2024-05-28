package com.kazbekov.invent.main.data.antoher

import com.google.gson.annotations.SerializedName

data class ChangeVersion(
    @SerializedName("last_version")
    val lastChangeVersion: Int
)
