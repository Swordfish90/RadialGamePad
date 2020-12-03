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
import android.os.Bundle
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.view.*
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.dials.*
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.EventsSource
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.math.MathUtils.clamp
import com.swordfish.radialgamepad.library.touchbound.CircleTouchBound
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.math.MathUtils.toRadians
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.touchbound.SectorTouchBound
import com.swordfish.radialgamepad.library.utils.MultiTapDetector
import com.swordfish.radialgamepad.library.utils.PaintUtils
import com.swordfish.radialgamepad.library.utils.PaintUtils.scale
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan
import kotlin.properties.Delegates

class RadialGamePad @JvmOverloads constructor(
    private val gamePadConfig: RadialGamePadConfig,
    defaultMarginsInDp: Float = 16f,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), EventsSource {

    private val exploreByTouchHelper = object : ExploreByTouchHelper(this) {

        private fun computeVirtualViews(): Map<Int, AccessibilityBox> {
            return allInteractors()
                .flatMap { it.dial.accessibilityBoxes() }
                .sortedBy { it.rect.top }
                .mapIndexed { index, accessibilityBox -> index to accessibilityBox }
                .toMap()
        }

        override fun getVirtualViewAt(x: Float, y: Float): Int {
            return computeVirtualViews().entries
                .filter { (_, accessibilityBox) -> accessibilityBox.rect.contains(x.roundToInt(), y.roundToInt()) }
                .map { (id, _) -> id }
                .firstOrNull() ?: INVALID_ID
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
            computeVirtualViews().forEach { (id, _) ->  virtualViewIds.add(id) }
        }

        override fun onPerformActionForVirtualView(
            virtualViewId: Int,
            action: Int,
            arguments: Bundle?
        ): Boolean {
            return false
        }

        override fun onPopulateNodeForVirtualView(
            virtualViewId: Int,
            node: AccessibilityNodeInfoCompat
        ) {
            val virtualView = computeVirtualViews()[virtualViewId]
            node.setBoundsInParent(virtualView!!.rect)
            node.contentDescription = virtualView.text
        }
    }

    private val marginsInPixel: Int = PaintUtils.convertDpToPixel(defaultMarginsInDp, context).roundToInt()

    private var dials: Int = gamePadConfig.sockets
    private var size: Float = 0f
    private var center = PointF(0f, 0f)

    // It's better to set padding inside in other to catch touch events happening there.
    var gravityX: Float by Delegates.observable(0f) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var gravityY: Float by Delegates.observable(0f) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var offsetX: Float by Delegates.observable(0f) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var offsetY: Float by Delegates.observable(0f) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var primaryDialMaxSizeDp: Float = Float.MAX_VALUE
        set(value) {
            field = value
            requestLayoutAndInvalidate()
        }

    var secondaryDialRotation: Float = 0f
        set(value) {
            field = toRadians(value)
            requestLayoutAndInvalidate()
        }

    var spacingTop: Int by Delegates.observable(0) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var spacingBottom: Int by Delegates.observable(0) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var spacingLeft: Int by Delegates.observable(0) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    var spacingRight: Int by Delegates.observable(0) { _, _, _ ->
        requestLayoutAndInvalidate()
    }

    private lateinit var primaryInteractor: DialInteractor
    private lateinit var secondaryInteractors: List<DialInteractor>

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
        ViewCompat.setAccessibilityDelegate(this, exploreByTouchHelper)
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
                configuration.contentDescription,
                configuration.theme ?: gamePadConfig.theme
            )
            is PrimaryDialConfig.Stick -> StickDial(
                configuration.id,
                configuration.buttonPressId,
                configuration.contentDescription,
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
                    config.contentDescription,
                    config.theme ?: gamePadConfig.theme
                )
                is SecondaryDialConfig.SingleButton -> ButtonDial(
                    context,
                    config.spread,
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
                    config.contentDescription,
                    config.theme ?: gamePadConfig.theme
                )
            }
            DialInteractor(secondaryDial)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val extendedSize = computeTotalSizeAsSizeMultipliers()

        applyMeasuredDimensions(widthMeasureSpec, heightMeasureSpec, extendedSize)

        val usableWidth = measuredWidth - spacingLeft - spacingRight - 2 * marginsInPixel
        val usableHeight = measuredHeight - spacingTop - spacingBottom - 2 * marginsInPixel

        size = minOf(
            usableWidth / extendedSize.width(),
            usableHeight / extendedSize.height(),
            PaintUtils.convertDpToPixel(primaryDialMaxSizeDp, context) / 2f
        )

        val maxDisplacementX = (usableWidth - size * extendedSize.width()) / 2f
        val maxDisplacementY = (usableHeight - size * extendedSize.height()) / 2f

        val totalDisplacementX = gravityX * maxDisplacementX + offsetX
        val finalOffsetX = clamp(totalDisplacementX, -maxDisplacementX, maxDisplacementX)

        val totalDisplacementY = gravityY * maxDisplacementY + offsetY
        val finalOffsetY = clamp(totalDisplacementY, -maxDisplacementY, maxDisplacementY)

        val baseCenterX = spacingLeft + (measuredWidth - spacingLeft - spacingRight) / 2f
        val baseCenterY = spacingTop + (measuredHeight - spacingTop - spacingBottom) / 2f

        center.x = finalOffsetX + baseCenterX - (extendedSize.left + extendedSize.right) * size * 0.5f
        center.y = finalOffsetY + baseCenterY - (extendedSize.top + extendedSize.bottom) * size * 0.5f

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

        val usableWidth = width - spacingLeft - spacingRight - 2 * marginsInPixel
        val usableHeight = height - spacingBottom - spacingTop - 2 * marginsInPixel

        val enforcedMaxSize = PaintUtils.convertDpToPixel(primaryDialMaxSizeDp, context) / 2

        when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    width,
                    minOf(
                        usableHeight,
                        (usableWidth * extendedSize.height() / extendedSize.width()).roundToInt(),
                        (enforcedMaxSize * extendedSize.height()).roundToInt()
                    ) + spacingBottom + spacingTop + 2 * marginsInPixel
                )
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    minOf(
                        usableWidth,
                        (usableHeight * extendedSize.width() / extendedSize.height()).roundToInt(),
                        (enforcedMaxSize * extendedSize.width()).roundToInt()
                    ) + spacingLeft + spacingRight + 2 * marginsInPixel,
                    height
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

        val sizes = allSockets.map { config ->
            if (config.avoidClipping) {
                measureSecondaryDialDrawingBoxNoClipping(config)
            } else {
                measureSecondaryDialDrawingBox(config)
            }
        }

        return PaintUtils.mergeRectangles(listOf(RectF(-1f, -1f, 1f, 1f)) + sizes)
    }

    private fun measureSecondaryDials() {
        gamePadConfig.secondaryDials.forEachIndexed { index, config ->
            val (rect, sector) = measureSecondaryDial(config)
            secondaryInteractors[index].touchBound = SectorTouchBound(sector)
            secondaryInteractors[index].measure(rect, sector)
        }
    }

    private fun measurePrimaryDial() {
        primaryInteractor.measure(RectF(center.x - size, center.y - size, center.x + size, center.y + size))
        primaryInteractor.touchBound = CircleTouchBound(center, size)
    }

    private fun measureSecondaryDial(config: SecondaryDialConfig): Pair<RectF, Sector> {
        val rect = measureSecondaryDialDrawingBox(config).scale(size)
        rect.offset(center.x, center.y)

        val dialAngle = Constants.PI2 / dials
        val dialSize = DEFAULT_SECONDARY_DIAL_SCALE * size * config.scale

        val sector = Sector(
            PointF(center.x, center.y),
            size,
            size + dialSize * config.scale,
            secondaryDialRotation + config.index * dialAngle - dialAngle / 2,
            secondaryDialRotation + (config.index + config.spread - 1) * dialAngle + dialAngle / 2
        )

        return rect to sector
    }

    private fun measureSecondaryDialDrawingBoxNoClipping(config: SecondaryDialConfig): RectF {
        val drawingBoxes = (config.index until (config.index + config.spread))
            .map { measureSecondaryDialDrawingBox(config.scale, it, 1) }

        return PaintUtils.mergeRectangles(drawingBoxes)
    }

    private fun measureSecondaryDialDrawingBox(config: SecondaryDialConfig): RectF {
        return measureSecondaryDialDrawingBox(config.scale, config.index, config.spread)
    }

    private fun measureSecondaryDialDrawingBox(scale: Float, index: Int, spread: Int): RectF {
        val dialAngle = Constants.PI2 / dials
        val dialSize = DEFAULT_SECONDARY_DIAL_SCALE * scale
        val distanceToCenter = maxOf(0.5f * dialSize / tan(dialAngle * spread / 2f), 1.0f + dialSize / 2f)

        val index = index + (spread - 1) * 0.5f

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

        secondaryInteractors.forEach {
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
        listOf(primaryInteractor) + secondaryInteractors

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (exploreByTouchHelper.dispatchHoverEvent(event)) {
            return true
        }
        return super.dispatchHoverEvent(event)
    }

    companion object {
        const val DEFAULT_SECONDARY_DIAL_SCALE = 0.75f
    }
}