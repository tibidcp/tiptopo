package com.tibi.tiptopo.domain

import android.graphics.Color
import androidx.annotation.StringRes
import com.google.firebase.firestore.ServerTimestamp
import com.tibi.tiptopo.R
import java.util.Date

data class Line(
    override var id: String = "",
    var name: String = "",
    var note: String = "",
    var color: Int = Color.BLACK,
    var type: LineType = LineType.Continuous,
    var vertices: List<Vertex> = listOf(),
    @ServerTimestamp
    var date: Date? = null
) : FirestoreMembers

enum class LineType(
    val lineVectorResId: Int,
    @StringRes val lineContentDescription: Int,
    val patternVectorResId: Int = 0,
    val anchorX: Float = 0f,
    val anchorY: Float = 0f
) {
    Continuous(R.drawable.ic_continuous, R.string.continuous),
    Dashed(R.drawable.ic_dashed, R.string.dashed),
    Dotted(R.drawable.ic_dotted, R.string.dotted),
    DashDotted(R.drawable.ic_dash_dotted, R.string.dash_dotted),
    SmallMetalFence(
        R.drawable.ic_small_metal_fence,
        R.string.small_metal_fence,
        R.drawable.ic_post_sidewalk,
        0.5f,
        0.5f
    ),
    BigMetalFence(
        R.drawable.ic_big_metal_fence,
        R.string.big_metal_fence,
        R.drawable.ic_big_metal_fence_pattern,
        0.5f,
        0.56554f
    ),
    Wall(
        R.drawable.ic_wall,
        R.string.wall,
        R.drawable.ic_wall_pattern,
        0.5f,
        0.6766f
    ),
    BigStoneFence(
        R.drawable.ic_big_stone_fence,
        R.string.big_stone_fence,
        R.drawable.ic_big_stone_fence_pattern,
        0.5f,
        0.5625f
    )
}
