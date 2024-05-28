package com.kazbekov.invent.main.data.session

import com.google.gson.annotations.SerializedName
import com.kazbekov.invent.main.data.session.RemoteSession

data class RemoteSessions(
    @SerializedName("sessions")
    val sessions: List<RemoteSession>
)
