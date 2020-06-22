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

package com.swordfish.radialgamepad.library

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.dials.*
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.EventsSource
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.touchbound.CircleTouchBound
import com.swordfish.radialgamepad.library.touchbound.SectorTouchBound
import com.swordfish.radialgamepad.library.touchbound.TouchBound
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.MathUtils.toRadians
import com.swordfish.radialgamepad.library.utils.MultiTapDetector
import com.swordfish.radialgamepad.library.utils.PaintUtils
import com.swordfish.radialgamepad.library.utils.PaintUtils.scale
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

class RadialGamePad @JvmOverloads constructor(
    private val gamePadConfig: RadialGamePadConfig,
    marginsInDp: Float = 16f,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), EventsSource {

    private val marginsInPixel: Int = PaintUtils.convertDpToPixel(marginsInDp, context).roundToInt()

    private var dials: Int = gamePadConfig.sockets
    private var size: Float = 0f
    private var center = PointF(0f, 0f)

    // It's better to set padding inside in other to catch touch events happening there.
    var offsetX: Float = 0.0f
        set(value) {
            field = value
            requestLayoutAndInvalidate()
        }

    var offsetY: Float = 0.0f
        set(value) {
            field = value
            requestLayoutAndInvalidate()
        }

    var secondaryDialRotation: Float = 0f
        set(value) {
            field = toRadians(value)
            requestLayoutAndInvalidate()
        }

    private lateinit var primaryInteractor: DialInteractor
    private lateinit var secondaryInteractors: Map<Int, DialInteractor>

    private val gestureDetector: MultiTapDetector = MultiTapDetector(context) { x, y, taps, isConfirmed ->
        if (!isConfirmed) return@MultiTapDetector

        val gestureType = when (taps) {
            1 -> GestureType.SINGLE_TAP
            2 -> GestureType.DOUBLE_TAP
            3 -> GestureType.TRIPLE_TAP
            else -> null
        } ?: return@MultiTapDetector

        val updated = allInteractors().map {
            it.gesture(x, y, gestureType)
        }

        if (updated.any { it }) {
            postInvalidate()
        }
    }

    init {
        initializePrimaryInteractor(gamePadConfig.primaryDial)
        initializeSecondaryInteractors(gamePadConfig.secondaryDials)
    }

    /** Simulate a motion event. It's used in Lemuroid to map events from sensors. */
    fun simulateMotionEvent(id: Int, relativeX: Float, relativeY: Float) {
        val updated = allDials()
            .filterIsInstance<MotionDial>()
            .map { it.simulateMotion(id, relativeX, relativeY) }
            .any { it }

        if (updated) {
            postInvalidate()
        }
    }

    /** Programmatically clear motion events associated with the id. */
    fun simulateClearMotionEvent(id: Int) {
        val updated = allDials()
            .filterIsInstance<MotionDial>()
            .map { it.simulateClearMotion(id) }
            .any { it }

        if (updated) {
            postInvalidate()
        }
    }

    private fun initializePrimaryInteractor(configuration: PrimaryDialConfig) {
        val primaryDial = when (configuration) {
            is PrimaryDialConfig.Cross -> CrossDial(
                context,
                configuration.id,
                configuration.rightDrawableId ?: R.drawable.direction_right_normal,
                configuration.rightDrawableId ?: R.drawable.direction_right_pressed,
                configuration.rightDrawableForegroundId,
                configuration.theme ?: gamePadConfig.theme
            )
            is PrimaryDialConfig.Stick -> StickDial(
                configuration.id,
                configuration.buttonPressId,
                configuration.theme ?: gamePadConfig.theme
            )
            is PrimaryDialConfig.PrimaryButtons -> PrimaryButtonsDial(
                context,
                configuration.dials,
                configuration.center,
                toRadians(configuration.rotationInDegrees),
                configuration.theme ?: gamePadConfig.theme
            )
        }
        primaryInteractor = DialInteractor(primaryDial)
    }

    private fun initializeSecondaryInteractors(secondaryDials: List<SecondaryDialConfig>) {
        secondaryInteractors = secondaryDials.map { config ->
            val secondaryDial = when (config) {
                is SecondaryDialConfig.Stick -> StickDial(
                    config.id,
                    config.buttonPressId,
                    config.theme ?: gamePadConfig.theme
                )
                is SecondaryDialConfig.SingleButton -> ButtonDial(
                    context,
                    config.buttonConfig,
                    config.theme ?: gamePadConfig.theme
                )
                is SecondaryDialConfig.Empty -> EmptyDial()
                is SecondaryDialConfig.Cross -> CrossDial(
                    context,
                    config.id,
                    config.rightDrawableId ?: R.drawable.direction_right_normal,
                    config.rightDrawableId ?: R.drawable.direction_right_pressed,
                    config.rightDrawableForegroundId,
                    config.theme ?: gamePadConfig.theme
                )
            }
            config.index to DialInteractor(secondaryDial)
        }.toMap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val extendedSize = computeTotalSizeAsSizeMultipliers()

        applyMeasuredDimensions(widthMeasureSpec, heightMeasureSpec, extendedSize)

        size = minOf(
            (measuredWidth - marginsInPixel * 2) / extendedSize.width(),
            (measuredHeight - marginsInPixel * 2) / extendedSize.height()
        )

        val maxDisplacementX = (measuredWidth - marginsInPixel * 2 - size * extendedSize.width()) / 2f
        val maxDisplacementY = (measuredHeight - marginsInPixel * 2 - size * extendedSize.height()) / 2f

        center.x = offsetX * maxDisplacementX + measuredWidth / 2f - (extendedSize.left + extendedSize.right) * size * 0.5f
        center.y = offsetY * maxDisplacementY + measuredHeight / 2f - (extendedSize.top + extendedSize.bottom) * size * 0.5f

        measurePrimaryDial()
        measureSecondaryDials()
    }

    private fun applyMeasuredDimensions(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        extendedSize: RectF
    ) {
        val (widthMode, width) = extractModeAndDimension(widthMeasureSpec)
        val (heightMode, height) = extractModeAndDimension(heightMeasureSpec)

        val usableWidth = width - marginsInPixel * 2
        val usableHeight = height - marginsInPixel * 2

        when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    width,
                    minOf(
                        usableHeight,
                        (usableWidth * extendedSize.height() / extendedSize.width()).roundToInt()
                    ) + marginsInPixel * 2
                )
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    minOf(
                        usableWidth,
                        (usableHeight * extendedSize.width() / extendedSize.height()).roundToInt()
                    ) + marginsInPixel * 2, height
                )
            }
            else -> setMeasuredDimension(width, height)
        }
    }

    private fun extractModeAndDimension(widthMeasureSpec: Int): Pair<Int, Int> {
        return MeasureSpec.getMode(widthMeasureSpec) to MeasureSpec.getSize(widthMeasureSpec)
    }

    /** Different dial configurations cause the view to grow in different directions. This functions
     *  returns a bounding box as multipliers of 'size' that contains the whole view. They are later
     *  used to compute the actual size. */
    private fun computeTotalSizeAsSizeMultipliers(): RectF {
        val allSockets = gamePadConfig.secondaryDials

        val sizes = allSockets.map { measureSecondaryDialAsSizeMultiplier(it) }

        val minX = minOf(sizes.map { it.left }.min() ?: -1f, -1f)
        val minY = minOf(sizes.map { it.top }.min() ?: -1f, -1f)

        val maxX = maxOf(sizes.map { it.right }.max() ?: 1f, 1f)
        val maxY = maxOf(sizes.map { it.bottom }.max() ?: 1f, 1f)

        return RectF(minX, minY, maxX, maxY)
    }

    private fun measureSecondaryDials() {
        gamePadConfig.secondaryDials.forEach { config ->
            val (rect, bounds) = measureSecondaryDial(config)
            secondaryInteractors[config.index]?.touchBound = bounds
            secondaryInteractors[config.index]?.measure(rect)
        }
    }

    private fun measurePrimaryDial() {
        primaryInteractor.measure(RectF(center.x - size, center.y - size, center.x + size, center.y + size))
        primaryInteractor.touchBound = CircleTouchBound(center, size)
    }

    private fun measureSecondaryDial(config: SecondaryDialConfig): Pair<RectF, TouchBound> {
        val rect = measureSecondaryDialAsSizeMultiplier(config).scale(size)
        rect.offset(center.x, center.y)

        val dialAngle = Constants.PI2 / dials
        val dialSize = DEFAULT_SECONDARY_DIAL_SCALE * size * config.scale

        val touchBound = SectorTouchBound(
            PointF(center.x, center.y),
            size,
            size + dialSize * config.scale,
            secondaryDialRotation + config.index * dialAngle - dialAngle / 2,
            secondaryDialRotation + (config.index + config.spread - 1) * dialAngle + dialAngle / 2
        )

        return rect to touchBound
    }

    private fun measureSecondaryDialAsSizeMultiplier(config: SecondaryDialConfig): RectF {
        val dialAngle = Constants.PI2 / dials
        val dialSize = DEFAULT_SECONDARY_DIAL_SCALE * config.scale
        val distanceToCenter = maxOf(0.5f * dialSize / tan(dialAngle * config.spread / 2f), 1.0f + dialSize / 2f)

        val index = config.index + (config.spread - 1) * 0.5f

        val finalAngle = index * dialAngle + secondaryDialRotation

        return RectF(
            (cos(finalAngle) * distanceToCenter - dialSize / 2f),
            (-sin(finalAngle) * distanceToCenter - dialSize / 2f),
            (cos(finalAngle) * distanceToCenter + dialSize / 2f),
            (-sin(finalAngle) * distanceToCenter + dialSize / 2f)
        )
    }

    private fun requestLayoutAndInvalidate() {
        requestLayout()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        primaryInteractor.draw(canvas)

        secondaryInteractors.values.forEach {
            it.draw(canvas)
        }
    }

    override fun events(): Observable<Event> {
        val allEvents = allDials().map { it.events() }
        return Observable.merge(allEvents)
            .doOnNext {
                if (gamePadConfig.haptic && it.haptic) performHapticFeedback()
            }
    }

    private fun performHapticFeedback() {
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.handleEvent(event)

        val fingers = TouchUtils.extractFingerPositions(event)

        val trackedFingers = allDials().mapNotNull { it.trackedPointerId() }

        val updated = allInteractors().map { dial ->
            forwardTouchToDial(dial, fingers, trackedFingers)
        }

        if (updated.any { it }) {
            postInvalidate()
        }

        return true
    }

    private fun forwardTouchToDial(
        dial: DialInteractor,
        fingers: List<TouchUtils.FingerPosition>,
        trackedFingers: List<Int>
    ): Boolean {
        return if (dial.trackedPointerId() != null) {
            dial.touch(fingers.filter { it.pointerId == dial.dial.trackedPointerId() })
        } else {
            dial.touch(fingers.filter { it.pointerId !in trackedFingers })
        }
    }

    private fun allDials(): List<Dial> = allInteractors().map { it.dial }

    private fun allInteractors(): List<DialInteractor> =
        listOf(primaryInteractor) + secondaryInteractors.values

    companion object {
        const val DEFAULT_SECONDARY_DIAL_SCALE = 0.75f
    }
}