package ua.edu.mobile.smartlife.ui.sensors

import org.junit.Assert.assertEquals
import org.junit.Test

class MotionTest {

    @Test
    fun `phone lying flat - magnitude equals gravity, no tilt`() {
        val state = SensorsViewModel.computeMotion(MotionState(), 0f, 0f, 9.81f)
        assertEquals(9.81f, state.magnitude, 0.01f)
        assertEquals(0f, state.pitch, 0.5f)
        assertEquals(0f, state.roll, 0.5f)
    }

    @Test
    fun `sharp spike counts as one shake`() {
        var state = SensorsViewModel.computeMotion(MotionState(), 0f, 0f, 9.81f)
        state = SensorsViewModel.computeMotion(state, 12f, 15f, 9.8f)  // різкий ривок
        state = SensorsViewModel.computeMotion(state, 13f, 14f, 9.8f)  // ривок триває — не рахуємо двічі
        state = SensorsViewModel.computeMotion(state, 0f, 0.3f, 9.8f)  // заспокоїлися

        assertEquals(1, state.shakes)
    }

    @Test
    fun `three separate spikes give three shakes`() {
        var state = MotionState()
        repeat(3) {
            state = SensorsViewModel.computeMotion(state, 0f, 0f, 9.81f)
            state = SensorsViewModel.computeMotion(state, 12f, 15f, 9.8f)
        }
        assertEquals(3, state.shakes)
    }
}
