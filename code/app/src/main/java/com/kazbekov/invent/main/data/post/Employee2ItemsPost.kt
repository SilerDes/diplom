package com.kazbekov.invent.main.data.post

import com.google.gson.annotations.SerializedName

data class Employee2ItemsPost(
    @SerializedName("employee")
    val employee: Int,
    @SerializedName("items")
    val items: List<Item2Post>
)