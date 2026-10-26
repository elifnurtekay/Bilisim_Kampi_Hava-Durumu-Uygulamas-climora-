package com.kampplus.hava.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.component.CityPhoto

@Composable
fun CityCard(city: City, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hasPhoto = city.countryCode == "TR" && com.kampplus.hava.ui.component.cityPhotoResource(city.name) != null
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (hasPhoto) {
            Box(Modifier.fillMaxWidth().height(170.dp).background(Color(0xFF173B58))) {
                CityPhoto(
                    city.name,
                    Modifier.matchParentSize(),
                    enabled = city.countryCode == "TR",
                    latitude = city.latitude,
                    longitude = city.longitude
                )
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6102734)))))
                Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            city.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(city.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f))
                    }
                    Text("Keşfet  ↗", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
            com.kampplus.hava.ui.component.CityPhotoCredit(city.name, city.countryCode == "TR", city.latitude, city.longitude)
        } else {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(city.countryCode ?: "•", Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge)
                }
                Column(Modifier.weight(1f)) {
                    Text(city.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(city.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("↗", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
