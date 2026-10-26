package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherLevelsTest {
    @Test fun `uv sinirlari ve eksik veri`() {
        assertEquals(listOf("Düşük", "Orta", "Yüksek", "Çok yüksek", "Aşırı"), listOf(0.0, 3.0, 6.0, 8.0, 11.0).map { uvLevel(it)?.label })
        assertNull(uvLevel(null))
        assertNull(uvLevel(-1.0))
        assertNull(uvLevel(Double.NaN))
        assertEquals(1f, uvLevel(20.0)!!.fraction)
    }

    @Test fun `basinc sinirlari ve eksik veri`() {
        assertEquals("Düşük", pressureLevel(999.0)?.label)
        assertEquals("Normal aralık", pressureLevel(1000.0)?.label)
        assertEquals("Normal aralık", pressureLevel(1020.0)?.label)
        assertEquals("Yüksek", pressureLevel(1021.0)?.label)
        assertEquals(0f, pressureLevel(970.0)!!.fraction)
        assertNull(pressureLevel(null))
        assertNull(pressureLevel(Double.NaN))
    }
}
