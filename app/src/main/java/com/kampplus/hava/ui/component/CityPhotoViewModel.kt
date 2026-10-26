package com.kampplus.hava.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.CityPhotoSource
import com.kampplus.hava.data.RemoteCityPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CityPhotoViewModel(repository: CityPhotoSource, name: String, latitude: Double?, longitude: Double?) : ViewModel() {
    private val mutablePhoto = MutableStateFlow<RemoteCityPhoto?>(null)
    val photo = mutablePhoto.asStateFlow()
    init {
        if (latitude != null && longitude != null) {
            viewModelScope.launch {
                mutablePhoto.value = repository.load(name, latitude, longitude)
            }
        }
    }
}
