package com.kampplus.hava.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.google.gson.JsonParser

@Composable
fun PhotoCreditsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uri = LocalUriHandler.current
    val credits = remember {
        context.assets.open("city-photo-credits.json").bufferedReader().use { JsonParser.parseReader(it).asJsonArray.toList() }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Climora hakkında") }, text = {
        LazyColumn(Modifier.heightIn(max = 440.dp)) {
            item {
                Text("Hava ve stil, aynı yerde.\n\nFotoğraf lisansları · Wikimedia Commons\nGörseller ekrana sığacak şekilde kırpılır.")
            }
            items(credits.size) { index ->
                val entry = credits[index].asJsonObject
                Column {
                    TextButton(onClick = { uri.openUri(entry["source"].asString) }) {
                        Text(entry["city"].asString + " · " + entry["author"].asString)
                    }
                    TextButton(onClick = { uri.openUri(entry["licenseUrl"].asString.ifBlank { entry["source"].asString }) }) {
                        Text(entry["license"].asString, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                TextButton(onClick = { uri.openUri("https://commons.wikimedia.org/wiki/File:Cumhuriyet_Square.jpg") }) {
                    Text("İzmir · Michael ksk · CC BY-SA 3.0")
                }
                TextButton(onClick = { uri.openUri("https://commons.wikimedia.org/wiki/File:Ankara_from_bus_station.jpg") }) {
                    Text("Ankara · Boubacar Amadou Cisse · CC BY-SA 4.0")
                }
                TextButton(onClick = {
                    uri.openUri("https://commons.wikimedia.org/wiki/File:Historical_peninsula_and_modern_skyline_of_Istanbul.jpg")
                }) {
                    Text("İstanbul · Hunanuk · CC0")
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Tamam") } })
}
