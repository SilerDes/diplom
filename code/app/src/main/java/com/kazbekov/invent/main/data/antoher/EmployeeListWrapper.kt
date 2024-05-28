package com.kazbekov.invent.main.data.antoher

import android.os.Parcelable
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmployeeListWrapper(
    val employees: List<RemoteEmployee>
) : Parcelable