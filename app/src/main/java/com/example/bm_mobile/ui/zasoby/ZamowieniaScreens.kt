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
import com.example.bm_mobile.data.api.dto.ZamowienieDetailDto
import com.example.bm_mobile.data.api.dto.ZamowienieDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZamowieniaListScreen(
    viewModel: ZasobyViewModel,
    onZamowienieClick: (Int) -> Unit,
    onBack: () -> Unit = {},
) {
    val state by viewModel.zamowienia.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadZamowienia() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zamówienia") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadZamowienia() }
                is ZasobyUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("Brak zamówień", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.data, key = { it.id }) { z ->
                                ZamowienieItem(z) { onZamowienieClick(z.id) }
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
private fun ZamowienieItem(z: ZamowienieDto, onClick: () -> Unit) {
    val statusColor = when (z.status) {
        "robocze"     -> MaterialTheme.colorScheme.onSurfaceVariant
        "zatwierdzone"-> MaterialTheme.colorScheme.secondary
        "wyslane"     -> MaterialTheme.colorScheme.primary
        "zrealizowane"-> MaterialTheme.colorScheme.tertiary
        "anulowane"   -> MaterialTheme.colorScheme.error
        else          -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(z.statusLabel,
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                }
                Text(z.numer ?: "—", fontWeight = FontWeight.SemiBold)
            }
        },
        supportingContent = {
            Text(buildString {
                z.dostawca?.let { append(it) }
                z.dataUtworzenia?.let { if (isNotEmpty()) append("  · "); append(it) }
                append("  · ${z.liczbaElementow} poz.")
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            if (z.sumaWartosc > 0) {
                Text("${String.format("%.0f", z.sumaWartosc)} PLN",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold)
            } else {
                Text("›", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ── Zamówienie szczegóły ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZamowienieDetailScreen(
    zamowienieId: Int,
    viewModel: ZasobyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.zamowienieDetail.collectAsState()

    LaunchedEffect(zamowienieId) { viewModel.loadZamowienieDetail(zamowienieId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zamówienie") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadZamowienieDetail(zamowienieId) }
                is ZasobyUiState.Success -> ZamowienieContent(s.data)
            }
        }
    }
}

@Composable
private fun ZamowienieContent(z: ZamowienieDetailDto) {
    val statusColor = when (z.status) {
        "robocze"     -> MaterialTheme.colorScheme.onSurfaceVariant
        "zatwierdzone"-> MaterialTheme.colorScheme.secondary
        "wyslane"     -> MaterialTheme.colorScheme.primary
        "zrealizowane"-> MaterialTheme.colorScheme.tertiary
        "anulowane"   -> MaterialTheme.colorScheme.error
        else          -> MaterialTheme.colorScheme.onSurfaceVariant
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
                            Text(z.statusLabel,
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor)
                        }
                        Text(z.numer ?: "—", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                    z.dostawca?.let {
                        Text("Dostawca: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(buildString {
                        z.dataUtworzenia?.let { append("Utworzono: $it") }
                        z.dataZatwierdzenia?.let { if (isNotEmpty()) append("  · Zatw.: "); append(it) }
                        z.dataWyslania?.let { if (isNotEmpty()) append("  · Wysłano: "); append(it) }
                        z.dataRealizacji?.let { if (isNotEmpty()) append("  · Zrealiz.: "); append(it) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    z.tworzacy?.let {
                        Text("Tworzący: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    z.zatwierdzajacy?.let {
                        Text("Zatwierdzający: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    z.uwagi?.let {
                        Text("Uwagi: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (z.sumaWartosc > 0) {
                        Text("Łącznie: ${String.format("%.2f", z.sumaWartosc)} PLN",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                Text("Elementy (${z.elementy.size})",
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        items(z.elementy, key = { it.id }) { el ->
            ListItem(
                headlineContent = {
                    Text(el.nazwa ?: "—", fontWeight = FontWeight.Medium)
                },
                supportingContent = {
                    Text(buildString {
                        append("${String.format("%.2f", el.ilosc)} ${el.jednostka ?: "szt"}")
                        el.cenaSzacunk?.let { append("  · ${String.format("%.2f", it)} PLN/szt") }
                        el.uwagi?.let { if (isNotEmpty()) append("  · "); append(it.take(40)) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    el.wartosc?.let {
                        Text("${String.format("%.2f", it)} PLN",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            HorizontalDivider()
        }
    }
}
