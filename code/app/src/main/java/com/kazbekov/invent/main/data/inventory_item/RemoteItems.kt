package com.kazbekov.invent.main.data.inventory_item

import com.google.gson.annotations.SerializedName

data class RemoteItems(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("items")
    val items: List<RemoteItem>
)