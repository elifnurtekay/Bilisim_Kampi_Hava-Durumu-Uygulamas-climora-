package com.kampplus.hava

import android.app.Application
import android.content.Context
import com.kampplus.hava.data.AndroidLocationRepository
import com.kampplus.hava.data.ChatRepository
import com.kampplus.hava.data.CityPhotoSource
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.GeminiChatRepository
import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.data.OpenMeteoCityWeatherTool
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.data.WikimediaCityPhotoRepository

class ClimoraApplication : Application() {
    val container by lazy { AppContainer(this) }
}

/** Uygulama ömürlü bağımlılıklar tek yerde kurulur; ViewModel'ler arayüzleri alır. */
class AppContainer(context: Context) {
    val cities: CityRepository by lazy { OpenMeteoCityRepository() }
    val weather: WeatherRepository by lazy { OpenMeteoWeatherRepository() }
    val chat: ChatRepository by lazy { GeminiChatRepository(cityWeather = OpenMeteoCityWeatherTool(cities, weather)) }
    val photos: CityPhotoSource by lazy { WikimediaCityPhotoRepository(context.applicationContext) }
    val location: LocationRepository by lazy { AndroidLocationRepository(context.applicationContext) }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as ClimoraApplication).container
