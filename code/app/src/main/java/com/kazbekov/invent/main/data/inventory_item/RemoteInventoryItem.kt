package com.kazbekov.invent.main.data.inventory_item

import com.google.gson.annotations.SerializedName
import com.kazbekov.invent.main.data.employee.RemoteEmployee

data class RemoteInventoryItem(
    @SerializedName("employee")
    val employee: RemoteEmployee,
    @SerializedName("items")
    val items: List<RemoteItem>
)
