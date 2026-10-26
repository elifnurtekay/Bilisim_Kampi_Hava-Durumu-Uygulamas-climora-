package com.kampplus.hava.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.model.City
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationState(val busy: Boolean = false, val city: City? = null, val message: String? = null)

class LocationViewModel(private val repository: LocationRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LocationState())
    val state = mutableState.asStateFlow()
    fun permissionDenied() {
        mutableState.value = LocationState(message = "Konum izni verilmedi. Şehir arayarak devam edebilirsin.")
    }
    fun consumeCity() {
        mutableState.value = mutableState.value.copy(city = null)
    }
    fun locate() {
        if (state.value.busy) return
        mutableState.value = LocationState(busy = true)
        viewModelScope.launch {
            try {
                val city = repository.locate()
                mutableState.value =
                    LocationState(
                        city = city,
                        message = if (city ==
                            null
                        ) {
                            "Konum alınamadı. Konum hizmetlerini açıp yeniden dene."
                        } else {
                            null
                        }
                    )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: SecurityException) {
                permissionDenied()
            } catch (_: Exception) {
                mutableState.value =
                    LocationState(message = "Konum hizmetine ulaşılamadı. Şehir arayarak devam edebilirsin.")
            }
        }
    }
}
