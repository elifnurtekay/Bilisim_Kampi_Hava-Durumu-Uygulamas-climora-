package com.kampplus.hava.ui.city

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import com.kampplus.hava.appContainer
import com.kampplus.hava.model.City

@Composable
fun LocationButton(onCityClick: (City) -> Unit) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        FilledTonalButton(onClick = {}, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("Bulunduğum konumun havası")
        }
        return
    }
    val context = LocalContext.current
    val vm: LocationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory =
        androidx.lifecycle.viewmodel.viewModelFactory {
            initializer { LocationViewModel(context.appContainer.location) }
        }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val busy = state.busy
    val message = state.message
    val latestClick by rememberUpdatedState(onCityClick)
    androidx.compose.runtime.LaunchedEffect(state.city) {
        state.city?.let { city ->
            vm.consumeCity()
            latestClick(city)
        }
    }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) vm.locate() else vm.permissionDenied()
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        FilledTonalButton(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                vm.locate()
            } else {
                permissions.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            if (busy) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.LocationOn, null)
            }
            Spacer(Modifier.width(8.dp))
            Text(if (busy) "Konum bulunuyor…" else "Bulunduğum konumun havası")
        }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp)) }
    }
}
