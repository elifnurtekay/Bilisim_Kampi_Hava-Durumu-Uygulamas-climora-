package com.kampplus.hava.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import com.kampplus.hava.R
import com.kampplus.hava.appContainer
import java.util.Locale

fun cityPhotoResource(name: String): Int? =
    cityPhotos[name.lowercase(Locale.forLanguageTag("tr"))] ?: when (name.lowercase(Locale.forLanguageTag("tr"))) {
        "istanbul", "ıstanbul", "İstanbul" -> R.drawable.istanbul
        "ankara" -> R.drawable.ankara
        "izmir", "ızmir" -> R.drawable.izmir
        else -> null
    }

@Composable
fun CityPhoto(name: String, modifier: Modifier = Modifier, enabled: Boolean = true, latitude: Double? = null, longitude: Double? = null) {
    val local = if (enabled) cityPhotoResource(name) else null
    if (local != null) {
        Image(painterResource(local), contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
        return
    }
    val photo = cityPhotoState(name, latitude, longitude)
    photo?.let { image ->
        Image(
            image.bitmap.asImageBitmap(),
            contentDescription = "Konum çevresinden: ${image.place}",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CityPhotoCredit(name: String, localAllowed: Boolean, latitude: Double?, longitude: Double?) {
    if (localAllowed && cityPhotoResource(name) != null) return
    val uri = androidx.compose.ui.platform.LocalUriHandler.current
    val photo = cityPhotoState(name, latitude, longitude)
    photo?.let {
        androidx.compose.material3.TextButton(onClick = { uri.openUri(it.source) }) {
            androidx.compose.material3.Text(
                "Konum çevresinden: ${it.place}\n${it.author} · ${it.license} · Wikimedia · Kırpılmış görsel",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun cityPhotoState(name: String, latitude: Double?, longitude: Double?): com.kampplus.hava.data.RemoteCityPhoto? {
    val container = androidx.compose.ui.platform.LocalContext.current.appContainer
    val vm: CityPhotoViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = "photo:$name:$latitude:$longitude",
        factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { CityPhotoViewModel(container.photos, name, latitude, longitude) }
        }
    )
    val photo by vm.photo.collectAsStateWithLifecycle()
    return photo
}
