package com.kazbekov.invent.main.data.inventory_item

import com.google.gson.annotations.SerializedName

data class RemoteInventoryItems(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("employee2items")
    val employee2items: List<RemoteInventoryItem>
)
