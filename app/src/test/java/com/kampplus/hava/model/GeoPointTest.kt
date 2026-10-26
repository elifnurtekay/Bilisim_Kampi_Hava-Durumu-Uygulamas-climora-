package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoPointTest {
    @Test fun `ayni noktada uzaklik sifir`() {
        val point = GeoPoint(41.0, 29.0)
        assertEquals(0.0, point.distanceKmTo(point), 0.001)
    }

    @Test fun `ayni isimli uzak sehirlerin fotografi eslesmez`() {
        assertTrue(GeoPoint(48.85, 2.35).distanceKmTo(GeoPoint(33.66, -95.55)) > 7000)
        assertTrue(GeoPoint(41.0, 29.0).distanceKmTo(GeoPoint(41.01, 29.02)) < 5)
    }
}
