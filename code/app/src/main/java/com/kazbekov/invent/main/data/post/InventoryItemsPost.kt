package com.kazbekov.invent.main.data.post

import com.google.gson.annotations.SerializedName

data class InventoryItemsPost(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("employee2items")
    val employee2items: List<Employee2ItemsPost>
)
