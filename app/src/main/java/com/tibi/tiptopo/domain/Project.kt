package com.tibi.tiptopo.domain

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Project(
    override var id: String = "",
    var name: String = "",
    @ServerTimestamp
    var date: Date? = null,
    var totalStation: TotalStation = TotalStation.Nikon
) : FirestoreMembers

enum class TotalStation {
    Nikon,
    Trimble,
    Leica,
    Sokkia
}
