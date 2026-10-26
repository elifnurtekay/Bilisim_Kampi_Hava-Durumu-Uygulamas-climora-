package com.kampplus.hava.ui.city

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import com.kampplus.hava.R
import com.kampplus.hava.appContainer
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.DarkSystemBars
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.PhotoCreditsDialog
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.theme.ClimoraTheme
import com.kampplus.hava.ui.theme.HavaTheme
@Composable
fun CityRoute(onCityClick: (City) -> Unit, modifier: Modifier = Modifier, viewModel: CityViewModel = cityViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        modifier = modifier
    )
}

/**
 * Şehir arama ekranı: `when` ile her duruma bir görünüm.
 *
 * State tutmaz, yalnızca aldığı değerleri çizer ve olayları yukarı bildirir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<List<City>>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    var creditsOpen by remember { mutableStateOf(false) }
    DarkSystemBars()
    ClimoraTheme {
        if (creditsOpen) PhotoCreditsDialog(onDismiss = { creditsOpen = false })
        Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(innerPadding).background(
                    Brush.verticalGradient(listOf(Color(0xFF173E63), Color(0xFF071B37)))
                ),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("climora", fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                            Text(
                                "HAVA VE STİL",
                                color = Color(0xFFF6CE82),
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 3.sp
                            )
                        }
                        Image(
                            painterResource(R.drawable.climora_logo),
                            "Climora logosu",
                            Modifier.size(86.dp).clip(RoundedCornerShape(22.dp))
                        )
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            "Bugünün havası,\nsenin tarzın.",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Bir şehir seç. Havayı keşfet, gününe hazırlan.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                item { LocationButton(onCityClick) }
                item {
                    SearchBar(query, onQueryChange, onSearch, Modifier.padding(horizontal = 20.dp))
                }
                item {
                    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("İstanbul", "Ankara", "İzmir", "Paris").forEach { name ->
                            TextButton(onClick = {
                                onQueryChange(name)
                                onSearch()
                            }) { Text(name) }
                        }
                    }
                    Text(
                        "ŞEHRİNİ KEŞFET",
                        Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                when (uiState) {
                    LoadState.Loading -> item { Box(Modifier.height(300.dp)) { ShimmerList() } }
                    is LoadState.Success -> items(uiState.data, key = { it.id }) { city ->
                        CityCard(city, { onCityClick(city) }, Modifier.padding(horizontal = 20.dp))
                    }
                    LoadState.Empty -> item {
                        Box(Modifier.height(220.dp)) {
                            EmptyView(title = "Şehir bulunamadı", message = "Yazımı kontrol et ya da başka bir şehir dene.")
                        }
                    }
                    is LoadState.Error -> item { Box(Modifier.height(220.dp)) { ErrorView(uiState.message, onRetry = onSearch) } }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(onClick = {
                            creditsOpen = true
                        }) { Text("Climora hakkında", style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
}

/**
 * Arama kutusu.
 *
 * State tutmaz: değeri yukarıdan alır, her değişikliği yukarıya bildirir
 * ("değer aşağı, olay yukarı" — state hoisting).
 */
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        label = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            // Temizle düğmesi yalnızca kutu doluyken görünür.
            Row {
                IconButton(onClick = {
                    keyboardController?.hide()
                    onSearch()
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Şehir ara")
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                    }
                }
            }
        },
        singleLine = true,
        // Klavyedeki Enter tuşu "Ara" olur.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    HavaTheme {
        CityScreen(
            query = "Ankara",
            uiState = LoadState.Success(
                listOf(
                    City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
                    City(738648, "Ankara", 41.0050, 39.7269, "TR", "Türkiye", "Trabzon")
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyPreview() {
    HavaTheme {
        CityScreen(
            query = "xyz",
            uiState = LoadState.Empty,
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}

@Composable
private fun cityViewModel(): CityViewModel {
    val container = androidx.compose.ui.platform.LocalContext.current.appContainer
    return viewModel(factory = androidx.lifecycle.viewmodel.viewModelFactory { initializer { CityViewModel(container.cities) } })
}
