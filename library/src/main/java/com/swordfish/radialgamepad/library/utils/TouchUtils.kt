/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2020. This file is part of RadialGamePad.
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

package com.swordfish.radialgamepad.library.utils

import android.graphics.PointF
import android.graphics.RectF
import com.swordfish.radialgamepad.library.touch.FingerPosition

object TouchUtils {

    fun computeRelativeFingerPosition(fingers: List<FingerPosition>, rect: RectF): List<FingerPosition> {
        return fingers.map {
            FingerPosition(it.pointerId, (it.x - rect.left) / rect.width(), (it.y -rect.top) / rect.height())
        }
    }

    fun computeRelativePosition(x: Float, y: Float, rect: RectF): PointF {
        return PointF((x - rect.left) / rect.width(), (y - rect.top) / rect.height())
    }
}
