package com.kazbekov.invent.main.data.antoher

import android.os.Parcelable
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import kotlinx.parcelize.Parcelize

@Parcelize
data class Employee2PositionsWrapper(
    val employee: RemoteEmployee,
    val selectedPositions: List<RemoteInventoryPosition>,
    val allPositions: List<RemoteInventoryPosition>
) : Parcelable