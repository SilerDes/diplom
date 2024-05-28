package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.data

import android.os.Parcelable
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttachedPositions(
    val stateId: Int,
    val employee: RemoteEmployee,
    val selectedPositions: List<RemoteInventoryPosition>,
    val remainingPositions: List<RemoteInventoryPosition>
) : Parcelable
