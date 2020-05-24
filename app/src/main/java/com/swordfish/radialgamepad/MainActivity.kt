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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import com.swordfish.radialgamepad.library.RadialGamePad
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val leftPad = RadialGamePad(SamplePadConfigs.GBA_LEFT, this)
        val rightPad = RadialGamePad(SamplePadConfigs.GBA_RIGHT, this)

        findViewById<FrameLayout>(R.id.left_container).addView(leftPad)
        findViewById<FrameLayout>(R.id.right_container).addView(rightPad)

        compositeDisposable.add(
            Observable.merge(leftPad.events(), rightPad.events())
                .subscribe { Log.e("EVENT", it.toString()) }
        )
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }
}
