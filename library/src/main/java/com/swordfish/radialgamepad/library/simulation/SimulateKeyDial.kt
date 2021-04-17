package com.swordfish.radialgamepad.library.simulation

import com.swordfish.radialgamepad.library.dials.Dial

interface SimulateKeyDial : Dial {

    fun simulateKeyPress(id: Int, simulatePress: Boolean): Boolean

    fun clearSimulateKeyPress(id: Int): Boolean
}
