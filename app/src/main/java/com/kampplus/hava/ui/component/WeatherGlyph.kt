package com.kampplus.hava.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.WeatherCondition
import kotlin.math.cos
import kotlin.math.sin

/** Küçük boyutlarda da net kalan, uygulamaya özel hava simgeleri. */
@Composable
fun WeatherGlyph(code: Int, modifier: Modifier = Modifier, isDay: Boolean = true) {
    Canvas(modifier.size(48.dp).semantics { contentDescription = WeatherCondition.fromWeatherCode(code).label }) {
        val u = size.minDimension / 100f
        val gold = Color(0xFFF6C86B)
        val cloud = Color(0xFFDCE8ED)
        if (code <= 2) {
            if (isDay) {
                for (i in 0..7) {
                    val a = i * Math.PI / 4
                    drawLine(
                        gold,
                        Offset((42 + cos(a) * 27).toFloat() * u, (40 + sin(a) * 27).toFloat() * u),
                        Offset(
                            (42 + cos(a) * 35).toFloat() * u,
                            (40 + sin(a) * 35).toFloat() * u
                        ),
                        4 * u,
                        StrokeCap.Round
                    )
                }
                drawCircle(gold, 20 * u, Offset(42 * u, 40 * u))
            } else {
                drawCircle(Color(0xFFE9E9C6), 24 * u, Offset(42 * u, 38 * u))
                drawCircle(Color(0xFF1C3549), 21 * u, Offset(53 * u, 29 * u))
            }
        }
        if (code > 0) {
            drawCircle(cloud, 18 * u, Offset(36 * u, 55 * u))
            drawCircle(cloud, 24 * u, Offset(57 * u, 46 * u))
            drawCircle(cloud, 17 * u, Offset(76 * u, 58 * u))
            drawRoundRect(cloud, Offset(22 * u, 53 * u), Size(68 * u, 23 * u), androidx.compose.ui.geometry.CornerRadius(12 * u))
        }
        if (code in 51..67 || code in 80..82 || code >= 95) {
            for (i in 0..2) {
                drawLine(
                    Color(0xFF78BDED),
                    Offset((35 + i * 19) * u, 83 * u),
                    Offset((30 + i * 19) * u, 94 * u),
                    4 * u,
                    StrokeCap.Round
                )
            }
        }
        if (code in 71..77 || code in 85..86) {
            for (i in 0..2) drawCircle(cloud, 3 * u, Offset((32 + i * 23) * u, 89 * u))
        }
        if (code == 45 || code == 48) {
            drawLine(cloud, Offset(20 * u, 85 * u), Offset(85 * u, 85 * u), 3 * u, StrokeCap.Round)
            drawLine(cloud, Offset(30 * u, 94 * u), Offset(75 * u, 94 * u), 3 * u, StrokeCap.Round)
        }
    }
}
