package com.swordfish.radialgamepad.library.math

import android.graphics.PointF

data class Sector(
    val center: PointF,
    val minRadius: Float,
    val maxRadius: Float,
    val minAngle: Float,
    val maxAngle: Float
)
