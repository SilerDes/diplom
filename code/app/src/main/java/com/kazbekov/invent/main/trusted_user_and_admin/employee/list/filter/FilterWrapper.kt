package com.kazbekov.invent.main.trusted_user_and_admin.employee.list.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterWrapper(val flags: List<String>) : Parcelable
