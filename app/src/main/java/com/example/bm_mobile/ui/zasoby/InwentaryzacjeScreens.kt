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
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDetailDto
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwentaryzacjeScreen(
    magazynId: Int,
    magazynNazwa: String,
    viewModel: ZasobyViewModel,
    onInwClick: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.inwentaryzacje.collectAsState()

    LaunchedEffect(magazynId) { viewModel.loadInwentaryzacje(magazynId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inwentaryzacje · $magazynNazwa") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadInwentaryzacje(magazynId) }
                is ZasobyUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("Brak inwentaryzacji", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.data, key = { it.id }) { inv ->
                                InwentaryzacjaItem(inv) { onInwClick(inv.id) }
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
private fun InwentaryzacjaItem(inv: InwentaryzacjaDto, onClick: () -> Unit) {
    val statusColor = when (inv.status) {
        "otwarta"     -> MaterialTheme.colorScheme.primary
        "zatwierdzona"-> MaterialTheme.colorScheme.tertiary
        "zamknieta"   -> MaterialTheme.colorScheme.outline
        else          -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when (inv.status) {
        "otwarta"      -> "Otwarta"
        "zatwierdzona" -> "Zatwierdzona"
        "zamknieta"    -> "Zamknięta"
        else           -> inv.status
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(statusLabel,
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                }
                Text(inv.numer, fontWeight = FontWeight.SemiBold)
            }
        },
        supportingContent = {
            Text(buildString {
                append(inv.dataRozpoczecia)
                inv.dataZamkniecia?.let { append(" – $it") }
                append("  · ${inv.liczbaPozycji} poz.")
                inv.uzytkownik?.let { append("  · $it") }
                inv.opis?.let { if (isNotEmpty()) append("  · "); append(it.take(40)) }
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Text("›", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}

// ── Inwentaryzacja szczegóły ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InwentaryzacjaDetailScreen(
    inwId: Int,
    viewModel: ZasobyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.inwentaryzacjaDetail.collectAsState()

    LaunchedEffect(inwId) { viewModel.loadInwentaryzacjaDetail(inwId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arkusz inwentaryzacji") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadInwentaryzacjaDetail(inwId) }
                is ZasobyUiState.Success -> InwentaryzacjaContent(s.data)
            }
        }
    }
}

@Composable
private fun InwentaryzacjaContent(inv: InwentaryzacjaDetailDto) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Surface(tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(inv.numer, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text(buildString {
                        append(inv.dataRozpoczecia)
                        inv.dataZamkniecia?.let { append(" – $it") }
                        inv.magazynNazwa?.let { append("  · $it") }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    inv.opis?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inv.uzytkownik?.let {
                        Text("Użytkownik: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                Text("Pozycje (${inv.pozycje.size})",
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        items(inv.pozycje, key = { it.id }) { poz ->
            val roznica = poz.roznica
            val roznicaColor = when {
                roznica == null -> MaterialTheme.colorScheme.onSurfaceVariant
                roznica > 0  -> MaterialTheme.colorScheme.tertiary
                roznica < 0  -> MaterialTheme.colorScheme.error
                else         -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            ListItem(
                headlineContent = {
                    Text(poz.produktNazwa ?: "—", fontWeight = FontWeight.Medium)
                },
                supportingContent = {
                    Text(buildString {
                        append("Sys: ${String.format("%.2f", poz.stanSystemowy)}")
                        poz.stanRealny?.let { append("  Real: ${String.format("%.2f", it)}") }
                        poz.uwagi?.let { append("  · $it") }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    if (roznica != null) {
                        val roznicaStr = if (roznica > 0) "+${String.format("%.2f", roznica)}"
                                         else String.format("%.2f", roznica)
                        Text(roznicaStr, fontWeight = FontWeight.SemiBold,
                            color = roznicaColor)
                    }
                }
            )
            HorizontalDivider()
        }
    }
}
