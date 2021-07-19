package com.tibi.tiptopo.domain

import android.graphics.Color
import androidx.annotation.StringRes
import com.google.firebase.firestore.ServerTimestamp
import com.tibi.tiptopo.R
import java.util.*

data class Line(
    var id: String = "",
    var name: String = "",
    var color: Int = Color.BLACK,
    var type: LineType = LineType.Continuous,
    var vertices: List<Vertex> = listOf(),
    @ServerTimestamp
    var date: Date? = null
)

enum class LineType(
    val vectorResId: Int,
    @StringRes val contentDescription: Int
) {
    Continuous(R.drawable.ic_continuous, R.string.continuous),
    Dashed(R.drawable.ic_dashed, R.string.dashed),
    Dotted(R.drawable.ic_dotted, R.string.dotted),
    DashDotted(R.drawable.ic_dash_dotted, R.string.dash_dotted),

}
