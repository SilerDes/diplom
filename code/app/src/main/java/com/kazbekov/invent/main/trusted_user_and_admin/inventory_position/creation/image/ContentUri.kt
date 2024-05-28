package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.image

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentUri(
    val uri: Uri
) : Parcelable