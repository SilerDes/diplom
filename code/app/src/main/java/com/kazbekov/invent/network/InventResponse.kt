package com.kazbekov.invent.network

import com.google.gson.annotations.SerializedName

sealed class InventResponse {

    data class SuccessfulLoginResponse(
        @SerializedName("employee_status")
        val employeeStatus: Int,
        @SerializedName("first_name")
        val firstName: String,
        @SerializedName("second_name")
        val secondName: String
    ) : InventResponse()

    data class UnsuccessfulResponse(
        val code: Int,
        val error: String
    ) : InventResponse()
}

