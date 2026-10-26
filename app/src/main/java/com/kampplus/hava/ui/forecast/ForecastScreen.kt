package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import com.kampplus.hava.appContainer
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.WeatherLevel
import com.kampplus.hava.model.pressureLevel
import com.kampplus.hava.model.uvLevel
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.chat.WeatherChat
import com.kampplus.hava.ui.component.CityPhoto
import com.kampplus.hava.ui.component.DarkSystemBars
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.WeatherGlyph
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val Ink = Color(0xFF0A254D)
private val Gold = Color(0xFFF6CE82)
private val ClockFormat = DateTimeFormatter.ofPattern("HH:mm")
private val DateFormat = DateTimeFormatter.ofPattern("d MMMM • EEEE", Locale.forLanguageTag("tr"))
private val DayFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("tr"))

@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = forecastViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ForecastScreen(
        viewModel.cityName,
        viewModel.subtitle,
        state,
        onBack,
        viewModel::onRetry,
        modifier,
        viewModel.latitude,
        viewModel.longitude
    )
}

@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null
) {
    DarkSystemBars()
    com.kampplus.hava.ui.theme.ClimoraTheme {
        ForecastScreenContent(cityName, subtitle, uiState, onBack, onRetry, modifier, latitude, longitude)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastScreenContent(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    latitude: Double? = null,
    longitude: Double? = null
) {
    var adviceOpen by rememberSaveable { mutableStateOf(false) }
    val forecast = (uiState as? LoadState.Success)?.data
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(cityName, fontWeight = FontWeight.SemiBold)
                        Text(
                            "CLIMORA  /  HAVA VE STİL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Şehirlere dön") } },
                actions = { IconButton(onClick = onRetry) { Icon(Icons.Default.Refresh, "Tahmini yenile") } }
            )
        },
        floatingActionButton = {
            if (forecast !=
                null
            ) {
                ExtendedFloatingActionButton(onClick = {
                    adviceOpen = true
                }, containerColor = Ink, contentColor = Color.White) {
                    Column {
                        Text("✦  Ne giysem?", fontWeight = FontWeight.SemiBold)
                        Text("Hava arkadaşın", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CityPhoto(
                cityName,
                Modifier.matchParentSize(),
                enabled = subtitle.contains("Türkiye"),
                latitude = latitude,
                longitude = longitude
            )
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color(0xA6072148), Color(0xF2071934)))))
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> ErrorView("Bu konum için tahmin bulunamadı.", onRetry = onRetry)
                is LoadState.Error -> ErrorView(uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(cityName, subtitle, uiState.data, latitude, longitude)
            }
        }
    }
    if (adviceOpen && forecast != null) {
        ModalBottomSheet(onDismissRequest = {
            adviceOpen = false
        }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
            WeatherChat(listOf(cityName, subtitle).filter { it.isNotBlank() }.joinToString(", "), forecast)
        }
    }
}

@Composable
private fun ForecastContent(cityName: String, subtitle: String, forecast: Forecast, latitude: Double?, longitude: Double?) {
    var days by rememberSaveable { mutableIntStateOf(7) }
    val current = forecast.current
    val today = forecast.daily.firstOrNull()
    val upcoming = forecast.hourly.filter { !it.time.isBefore(current.observedAt.withMinute(0).withSecond(0).withNano(0)) }.take(24)
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(Brush.verticalGradient(listOf(Ink, Color(0xFF345E71))))
            ) {
                CityPhoto(
                    cityName,
                    Modifier.matchParentSize(),
                    enabled = subtitle.contains("Türkiye"),
                    latitude = latitude,
                    longitude = longitude
                )
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Ink.copy(alpha = .20f), Ink.copy(alpha = .88f)))))
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(alpha = .04f), size.width * .65f, Offset(size.width * .95f, size.height * .35f))
                    drawCircle(Color.White.copy(alpha = .04f), size.width * .45f, Offset(size.width * .95f, size.height * .35f))
                }
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(current.observedAt.format(DateFormat), color = Gold, style = MaterialTheme.typography.labelLarge)
                    Text(
                        subtitle.ifBlank {
                            "Bulunduğun konum"
                        },
                        color = Color.White.copy(alpha = .75f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            degrees(current.temperatureC),
                            fontSize = 88.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-5).sp,
                            color = Color.White
                        )
                        WeatherGlyph(current.weatherCode, Modifier.size(112.dp), current.isDay)
                    }
                    Text(
                        WeatherCondition.fromWeatherCode(current.weatherCode).label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        "Hissedilen ${degrees(current.apparentTemperatureC ?: current.temperatureC)}" +
                            (
                                today?.let {
                                    "   ·   ↑ ${degrees(it.maxTemperatureC)}  ↓ ${degrees(it.minTemperatureC)}"
                                } ?: ""
                                ),
                        color = Color.White.copy(alpha = .85f)
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = .18f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HeroMetric("Nem", current.humidityPercent?.let { "%$it" } ?: "—")
                        HeroMetric("Rüzgâr", current.windSpeedKmh?.let { "${it.roundToInt()} km/sa" } ?: "—")
                        HeroMetric("Yağış", today?.precipitationProbability?.let { "%$it" } ?: "—")
                    }
                    Text(
                        "Son veri ${current.observedAt.format(ClockFormat)} · Konumun yerel saati",
                        Modifier.padding(top = 10.dp),
                        color = Color.White.copy(alpha = .65f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        item {
            SectionTitle("Saat saat", "Önümüzdeki 24 saat")
            Spacer(Modifier.height(12.dp))
            if (upcoming.isEmpty()) Text("Saatlik tahmin şu anda bulunmuyor.")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(upcoming, key = { it.time.toString() }) { hour ->
                    val first = hour == upcoming.firstOrNull()
                    Column(
                        Modifier.width(
                            76.dp
                        ).clip(
                            RoundedCornerShape(22.dp)
                        ).background(if (first) Ink else MaterialTheme.colorScheme.surfaceContainer).padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            hour.time.format(ClockFormat),
                            color = if (first) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium
                        )
                        WeatherGlyph(hour.weatherCode, Modifier.size(38.dp), hour.isDay)
                        Text(
                            degrees(hour.temperatureC),
                            color = if (first) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        Text(
                            hour.precipitationProbability?.let {
                                "%$it"
                            } ?: "—",
                            color = if (first) Gold else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        item {
            SectionTitle("Önümüzdeki günler", "En düşük / en yüksek")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 15).forEach { count ->
                    FilterChip(selected = days == count, onClick = { days = count }, label = { Text("$count gün") })
                }
            }
            if (days ==
                15
            ) {
                Text(
                    "İleri tarihlerde tahminler değişebilir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (forecast.daily.isEmpty()) item { Text("Günlük tahmin şu anda bulunmuyor.") }
        items(forecast.daily.take(days), key = { it.date.toString() }) { day ->
            Row(
                Modifier.fillMaxWidth().clip(
                    RoundedCornerShape(18.dp)
                ).background(MaterialTheme.colorScheme.surfaceContainer).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(Modifier.weight(1.4f)) {
                    Text(
                        if (day.date ==
                            current.observedAt.toLocalDate()
                        ) {
                            "Bugün"
                        } else {
                            day.date.format(DayFormat)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        day.precipitationProbability?.let {
                            "Yağış %$it"
                        } ?: "Yağış verisi yok",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                WeatherGlyph(day.weatherCode, Modifier.size(32.dp))
                Text(degrees(day.minTemperatureC), Modifier.width(34.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Canvas(Modifier.weight(1f).height(8.dp)) {
                    val min = forecast.daily.minOf { it.minTemperatureC }
                    val range = (forecast.daily.maxOf { it.maxTemperatureC } - min).coerceAtLeast(1.0)
                    drawLine(
                        Color(0xFFE0E6E8),
                        Offset(0f, size.height / 2),
                        Offset(size.width, size.height / 2),
                        size.height,
                        StrokeCap.Round
                    )
                    drawLine(
                        Brush.horizontalGradient(listOf(Color(0xFF78B7BD), Gold)),
                        Offset(
                            ((day.minTemperatureC - min) / range * size.width).toFloat(),
                            size.height / 2
                        ),
                        Offset(((day.maxTemperatureC - min) / range * size.width).toFloat(), size.height / 2),
                        size.height,
                        StrokeCap.Round
                    )
                }
                Text(degrees(day.maxTemperatureC), Modifier.width(34.dp), fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            SectionTitle("Günün ayrıntıları", "Küçük bir bakış")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailTile("Gün doğumu", today?.sunrise?.format(ClockFormat) ?: "—", Modifier.weight(1f))
                DetailTile("Gün batımı", today?.sunset?.format(ClockFormat) ?: "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            LevelTile(
                "UV · günlük en yüksek",
                today?.uvIndex?.let { String.format(Locale.forLanguageTag("tr"), "%.1f", it) } ?: "—",
                uvLevel(today?.uvIndex),
                true
            )
            Spacer(Modifier.height(10.dp))
            LevelTile("Basınç", current.pressureHpa?.let { "${it.roundToInt()} hPa" } ?: "—", pressureLevel(current.pressureHpa), false)
            Spacer(Modifier.height(10.dp))
            DetailTile("Bulutluluk", current.cloudCover?.let { "%$it" } ?: "—", Modifier.fillMaxWidth())
        }
        item {
            com.kampplus.hava.ui.component.CityPhotoCredit(cityName, subtitle.contains("Türkiye"), latitude, longitude)
            Text(
                "Hava verileri: Open-Meteo · CC BY 4.0\nHer gün, bir adım dışarı.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable private fun HeroMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(label, color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun DetailTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
    }
}
private fun degrees(value: Double) = "${value.roundToInt()}°"

@Composable
private fun LevelTile(label: String, value: String, level: WeatherLevel?, uv: Boolean) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
        Text(level?.label ?: "Veri bulunmuyor", color = Gold, style = MaterialTheme.typography.titleMedium)
        if (level != null) {
            Canvas(Modifier.fillMaxWidth().height(16.dp)) {
                val colors = if (uv) {
                    listOf(Color(0xFF6BCB90), Color(0xFFFFDB70), Color(0xFFFFA35C), Color(0xFFEE707A), Color(0xFFBE94E8))
                } else {
                    listOf(Color(0xFF8FC5F4), Color(0xFF92D3C2), Color(0xFFC2B1E8))
                }
                drawLine(
                    Brush.horizontalGradient(colors),
                    Offset(8.dp.toPx(), size.height / 2),
                    Offset(
                        size.width - 8.dp.toPx(),
                        size.height / 2
                    ),
                    8.dp.toPx(),
                    StrokeCap.Round
                )
                val x = 8.dp.toPx() + (size.width - 16.dp.toPx()) * level.fraction
                drawCircle(Color.White, 7.dp.toPx(), Offset(x, size.height / 2))
                drawCircle(Ink, 4.dp.toPx(), Offset(x, size.height / 2))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (uv) "0 · Düşük" else "980 · Düşük", style = MaterialTheme.typography.labelSmall)
                Text(if (uv) "6 · Yüksek" else "1010 · Normal", style = MaterialTheme.typography.labelSmall)
                Text(if (uv) "11+ · Aşırı" else "1040 · Yüksek", style = MaterialTheme.typography.labelSmall)
            }
            Text(level.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun forecastViewModel(): ForecastViewModel {
    val container = androidx.compose.ui.platform.LocalContext.current.appContainer
    return viewModel(
        factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { ForecastViewModel(createSavedStateHandle(), container.weather) }
        }
    )
}
