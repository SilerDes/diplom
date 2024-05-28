package com.kazbekov.invent.main.data.inventory_position

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RemoteInventoryPosition(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title_official")
    val titleOfficial: String,
    @SerializedName("title_user")
    val titleNonOfficial: String,
    @SerializedName("image_link")
    val imageLink: String
) : Parcelable
