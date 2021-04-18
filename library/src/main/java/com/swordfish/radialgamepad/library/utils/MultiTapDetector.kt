package com.swordfish.radialgamepad.library.utils

import android.content.Context
import android.os.Handler
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class MultiTapDetector(context: Context, private val callback: (Float, Float, Int, Boolean) -> Unit) {
    private var numberOfTaps = 0
    private val handler = Handler()

    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
    private val tapTimeout = ViewConfiguration.getTapTimeout().toLong()
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    private val viewConfig = ViewConfiguration.get(context)

    private var downEvent = Event()
    private var lastTapUpEvent = Event()

    data class Event(var time: Long = 0, var x: Float = 0f, var y: Float = 0f) {
        fun copyFrom(motionEvent: MotionEvent) {
            time = motionEvent.eventTime
            x = motionEvent.x
            y = motionEvent.y
        }

        fun clear() {
            time = 0
        }
    }

    fun handleEvent(event: MotionEvent) {
        when(event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                downEvent.copyFrom(event)

                if (numberOfTaps == 0) {
                    callback(downEvent.x, downEvent.y, 0, true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // If a move greater than the allowed slop happens before timeout, then this is a scroll and not a tap
                if(event.eventTime - event.downTime < tapTimeout
                    && abs(event.x - downEvent.x) > viewConfig.scaledTouchSlop
                    && abs(event.y - downEvent.y) > viewConfig.scaledTouchSlop) {
                    downEvent.clear()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val downEvent = this.downEvent
                val lastTapUpEvent = this.lastTapUpEvent

                if(downEvent.time > 0 && event.eventTime - downEvent.time < longPressTimeout) {
                    // We have a tap
                    if(lastTapUpEvent.time > 0
                        && event.eventTime - lastTapUpEvent.time < doubleTapTimeout
                        && abs(event.x - lastTapUpEvent.x) < viewConfig.scaledDoubleTapSlop
                        && abs(event.y - lastTapUpEvent.y) < viewConfig.scaledDoubleTapSlop) {
                        // Double tap
                        numberOfTaps++
                    } else {
                        numberOfTaps = 1
                    }
                    this.lastTapUpEvent.copyFrom(event)

                    // Send event
                    val taps = numberOfTaps
                    handler.postDelayed({
                        // When this callback runs, we know if it is the final tap of a sequence
                        // if the number of taps has not changed
                        callback(downEvent.x, downEvent.y, taps, taps == numberOfTaps)
                    }, doubleTapTimeout)
                }
            }
        }
    }
}