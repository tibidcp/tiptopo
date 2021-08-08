package com.tibi.tiptopo.domain

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Project(
    override var id: String = "",
    var name: String = "",
    @ServerTimestamp
    var date: Date? = null,
    var totalStation: TotalStation = TotalStation.NikonNPL352
) : FirestoreMembers

enum class TotalStation {
    NikonNPL352,
    TrimbleM3
}
