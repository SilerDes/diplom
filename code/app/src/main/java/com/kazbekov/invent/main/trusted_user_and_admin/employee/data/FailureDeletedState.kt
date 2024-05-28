package com.kazbekov.invent.main.trusted_user_and_admin.employee.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FailureDeletedState(
    val stateId: Int,
    val message: String
) : Parcelable
