package com.example.bm_mobile.ui.zasoby

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bm_mobile.data.api.dto.WyjazdDto
import com.example.bm_mobile.data.api.dto.WyjazdSzczegolyDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WyjazdListScreen(
    viewModel: ZasobyViewModel,
    onWyjazdClick: (Int) -> Unit,
) {
    val state by viewModel.wyjazdy.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadWyjazdy() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Listy wyjazdowe") }) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadWyjazdy() }
                is ZasobyUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("Brak list wyjazdowych", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.data, key = { it.id }) { w ->
                                WyjazdItem(w) { onWyjazdClick(w.id) }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WyjazdItem(w: WyjazdDto, onClick: () -> Unit) {
    val statusColor = when (w.status) {
        "planowany"  -> MaterialTheme.colorScheme.primary
        "w_terenie"  -> MaterialTheme.colorScheme.tertiary
        "zakonczony" -> MaterialTheme.colorScheme.outline
        else         -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(w.statusLabel,
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                }
                Text(w.numer, fontWeight = FontWeight.SemiBold)
            }
        },
        supportingContent = {
            Text(buildString {
                append(w.projektNazwa)
                w.lokalizacja?.let { append("  · $it") }
                w.dataWyjazdu?.let { append("  · $it") }
                append("  · ${w.liczbaPozycji} poz.")
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2)
        },
        trailingContent = {
            val km = w.kierownikImie
            val kn = w.kierownikNazwisko
            if (km != null) {
                Text("$km $kn", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("›", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ── Szczegóły wyjazdu ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WyjazdSzczegolyScreen(
    wyjazdId: Int,
    viewModel: ZasobyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.wyjazdSzczegoly.collectAsState()

    LaunchedEffect(wyjazdId) { viewModel.loadWyjazdSzczegoly(wyjazdId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista wyjazdowa") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ Wróć") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadWyjazdSzczegoly(wyjazdId) }
                is ZasobyUiState.Success -> WyjazdSzczegolyContent(s.data)
            }
        }
    }
}

@Composable
private fun WyjazdSzczegolyContent(w: WyjazdSzczegolyDto) {
    val statusColor = when (w.status) {
        "planowany"  -> MaterialTheme.colorScheme.primary
        "w_terenie"  -> MaterialTheme.colorScheme.tertiary
        "zakonczony" -> MaterialTheme.colorScheme.outline
        else         -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Surface(tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = statusColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small) {
                            Text(w.statusLabel,
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor)
                        }
                        Text(w.numer, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                    Text(w.projektNazwa, style = MaterialTheme.typography.bodyMedium)
                    w.lokalizacja?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(buildString {
                        w.magazynNazwa?.let { append("Magazyn: $it") }
                        w.dataWyjazdu?.let { if (isNotEmpty()) append("  · "); append("Wyjazd: $it") }
                        w.dataPowrotu?.let { if (isNotEmpty()) append("  · "); append("Powrót: $it") }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val km = w.kierownikImie
                    val kn = w.kierownikNazwisko
                    if (km != null) {
                        Text("Kierownik: $km $kn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    w.uwagi?.let {
                        Text("Uwagi: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                Text("Pozycje (${w.pozycje.size})",
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        items(w.pozycje, key = { it.id }) { poz ->
            val wrocoColor = if (poz.wrocilo) MaterialTheme.colorScheme.tertiary
                             else MaterialTheme.colorScheme.onSurfaceVariant
            ListItem(
                headlineContent = {
                    Text(poz.produktNazwa ?: "—", fontWeight = FontWeight.Medium)
                },
                supportingContent = {
                    Text(buildString {
                        append("Ilość: ${String.format("%.2f", poz.ilosc)}")
                        poz.uwagi?.let { append("  · $it") }
                        poz.dataPowrotu?.let { append("  · Powrót: $it") }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    Text(if (poz.wrocilo) "Wróciło" else "W terenie",
                        style = MaterialTheme.typography.labelSmall,
                        color = wrocoColor)
                }
            )
            HorizontalDivider()
        }
    }
}
