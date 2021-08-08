package com.tibi.tiptopo.domain

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Station(
    override var id: String = "",
    var name: String = "",
    var backsightId: String = "",
    var backsightVA: Double = 0.0,
    var backsightHA: Double = 0.0,
    var backsightSD: Double = 0.0,
    var backsightDA: Double = 0.0,
    var hi: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    @ServerTimestamp
    var date: Date? = null
) : FirestoreMembers
