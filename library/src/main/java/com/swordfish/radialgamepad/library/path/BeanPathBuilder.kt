/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2021. This file is part of RadialGamePad.
 *
 * RadialGamePad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RadialGamePad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RadialGamePad.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.radialgamepad.library.path

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.swordfish.radialgamepad.library.math.MathUtils.lint
import com.swordfish.radialgamepad.library.math.MathUtils.toDegrees
import com.swordfish.radialgamepad.library.math.Sector
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

object BeanPathBuilder {

    fun build(drawingBox: Rect, sector: Sector, margin: Float): Path {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        val beanRadius = radius * (1.0f - 2 * margin)

        val maxRadius = lint(1.0f - margin, sector.minRadius, sector.maxRadius)
        val middleRadius = lint(0.5f, sector.minRadius, sector.maxRadius)
        val minRadius = lint(margin, sector.minRadius, sector.maxRadius)

        val spreadMargin = 2f * asin(radius * margin / middleRadius)
        val spreadAngle = 2f * asin(beanRadius / middleRadius)

        val startAngle = sector.minAngle + spreadAngle / 2 + spreadMargin
        val middleAngle = lint(0.5f, sector.minAngle, sector.maxAngle)
        val endAngle = sector.maxAngle - spreadAngle / 2 - spreadMargin

        return Path().apply {
            moveTo(
                sector.center.x + cos(startAngle) * maxRadius,
                sector.center.y - sin(startAngle) * maxRadius
            )
            arcTo(
                RectF(
                    sector.center.x + cos(startAngle) * middleRadius - beanRadius,
                    sector.center.y - sin(startAngle) * middleRadius - beanRadius,
                    sector.center.x + cos(startAngle) * middleRadius + beanRadius,
                    sector.center.y - sin(startAngle) * middleRadius + beanRadius
                ),
                -toDegrees(startAngle),
                180f
            )
            quadTo(
                sector.center.x + cos(middleAngle) * minRadius / cos((endAngle - startAngle) / 2f),
                sector.center.y - sin(middleAngle) * minRadius / cos((endAngle - startAngle) / 2f),
                sector.center.x + cos(endAngle) * minRadius,
                sector.center.y - sin(endAngle) * minRadius
            )
            arcTo(
                RectF(
                    sector.center.x + cos(endAngle) * middleRadius - beanRadius,
                    sector.center.y - sin(endAngle) * middleRadius - beanRadius,
                    sector.center.x + cos(endAngle) * middleRadius + beanRadius,
                    sector.center.y - sin(endAngle) * middleRadius + beanRadius
                ),
                -toDegrees(endAngle) + 180f,
                180f
            )
            quadTo(
                sector.center.x + cos(middleAngle) * maxRadius / cos((endAngle - startAngle) / 2f),
                sector.center.y - sin(middleAngle) * maxRadius / cos((endAngle - startAngle) / 2f),
                sector.center.x + cos(startAngle) * maxRadius,
                sector.center.y - sin(startAngle) * maxRadius
            )
            close()
        }
    }
}
