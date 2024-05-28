package com.kazbekov.invent.main.data.inventory_item

import com.google.gson.annotations.SerializedName
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition

data class RemoteItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("position")
    val position: RemoteInventoryPosition,
    @SerializedName("count")
    var count: Int
)
