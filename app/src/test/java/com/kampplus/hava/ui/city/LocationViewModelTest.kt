package com.kampplus.hava.ui.city

import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun `konum sonucu tuketilince tekrar yonlendirme yapilmaz`() = runTest {
        val city = City(-1, "Kadıköy / İstanbul", 41.0, 29.0, "TR", "Türkiye", null)
        val vm = LocationViewModel(object : LocationRepository {
            override suspend fun locate() = city
        })
        vm.locate()
        advanceUntilIdle()
        assertEquals(city, vm.state.value.city)
        assertFalse(vm.state.value.busy)
        vm.consumeCity()
        assertNull(vm.state.value.city)
    }

    @Test fun `konum yoksa acik hata ve bos konum`() = runTest {
        val vm = LocationViewModel(object : LocationRepository {
            override suspend fun locate(): City? = null
        })
        vm.locate()
        advanceUntilIdle()
        assertNull(vm.state.value.city)
        assertNotNull(vm.state.value.message)
        assertFalse(vm.state.value.busy)
    }

    @Test fun `izin hatasi bekleme durumunu kapatir`() = runTest {
        val vm = LocationViewModel(object : LocationRepository {
            override suspend fun locate(): City? = throw SecurityException()
        })
        vm.locate()
        advanceUntilIdle()
        assertNotNull(vm.state.value.message)
        assertFalse(vm.state.value.busy)
    }
}
