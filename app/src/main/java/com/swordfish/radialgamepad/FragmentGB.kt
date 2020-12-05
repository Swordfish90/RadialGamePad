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

package com.swordfish.radialgamepad

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.event.Event
import io.reactivex.disposables.CompositeDisposable

class FragmentGB : Fragment() {

    private lateinit var leftPad: RadialGamePad
    private lateinit var rightPad: RadialGamePad

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_double_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        leftPad = RadialGamePad(SamplePadConfigs.GB_LEFT, 8f, requireContext())
        rightPad = RadialGamePad(SamplePadConfigs.GB_RIGHT, 8f,requireContext())

        // We want the pad anchored to the bottom of the screen
        leftPad.gravityX = -1f
        leftPad.gravityY = 1f

        rightPad.gravityX = 1f
        rightPad.gravityY = 1f

        view.findViewById<FrameLayout>(R.id.leftcontainer).addView(leftPad)
        view.findViewById<FrameLayout>(R.id.rightcontainer).addView(rightPad)
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(leftPad.events().subscribe { handleEvent(it) })
        compositeDisposable.add(rightPad.events().subscribe { handleEvent(it) })
    }

    private fun handleEvent(event: Event) {
        Log.d("Event", "Event received $event")
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }
}