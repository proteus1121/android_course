package ua.edu.mobile.smartlife.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Розбір BLE-пакета можна перевірити без жодного Bluetooth-пристрою. */
class HeartRateParserTest {

    @Test
    fun `uint8 format - flag bit 0 is zero`() {
        val packet = byteArrayOf(0x00, 72)
        assertEquals(72, BleManager.parseHeartRate(packet))
    }

    @Test
    fun `uint16 format - flag bit 0 is one`() {
        // 300 = 0x012C -> молодший байт 0x2C, старший 0x01 (little-endian)
        val packet = byteArrayOf(0x01, 0x2C, 0x01)
        assertEquals(300, BleManager.parseHeartRate(packet))
    }

    @Test
    fun `values above 127 are read as unsigned`() {
        // Byte у Kotlin знаковий: 0xB4 = -76, але пульс має бути 180
        val packet = byteArrayOf(0x00, 0xB4.toByte())
        assertEquals(180, BleManager.parseHeartRate(packet))
    }

    @Test
    fun `too short packet returns null`() {
        assertNull(BleManager.parseHeartRate(byteArrayOf(0x00)))
        assertNull(BleManager.parseHeartRate(byteArrayOf(0x01, 0x10)))
    }
}
