package com.example.bm_mobile.ui.zasoby

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bm_mobile.data.api.dto.DokumentDetailDto
import com.example.bm_mobile.data.api.dto.DokumentRuchDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DokumentDetailScreen(
    dokumentId: Int,
    viewModel: ZasobyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.dokument.collectAsState()

    LaunchedEffect(dokumentId) { viewModel.loadDokument(dokumentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dokument") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadDokument(dokumentId) }
                is ZasobyUiState.Success -> DokumentContent(s.data)
            }
        }
    }
}

@Composable
private fun DokumentContent(d: DokumentDetailDto) {
    val typColor = when (d.typ) {
        "PZ", "PW" -> MaterialTheme.colorScheme.tertiary
        "WZ", "RW" -> MaterialTheme.colorScheme.error
        "MM"       -> MaterialTheme.colorScheme.secondary
        else       -> MaterialTheme.colorScheme.primary
    }
    val statusColor = when (d.status) {
        "podpisany" -> MaterialTheme.colorScheme.tertiary
        "wyslany"   -> MaterialTheme.colorScheme.primary
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Surface(tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = typColor, shape = MaterialTheme.shapes.small) {
                            Text(d.typ,
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(d.numer, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Surface(color = statusColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small) {
                            Text(d.status,
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor)
                        }
                    }
                    d.dataWystawienia?.let {
                        Text("Data: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(buildString {
                        d.magazynNazwa?.let { append("Magazyn: $it") }
                        d.magazynDocelowy?.let { if (isNotEmpty()) append(" → "); append(it) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (d.kontrahent != null || d.pracownik != null) {
                        Text(buildString {
                            d.kontrahent?.let { append(it) }
                            d.pracownik?.let { if (isNotEmpty()) append("  · "); append(it) }
                        }, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    d.opis?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                Text("Pozycje (${d.ruchy.size})",
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        items(d.ruchy, key = { it.id }) { r ->
            DokumentRuchRow(r)
            HorizontalDivider()
        }
        if (d.ruchy.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center) {
                    Text("Brak pozycji", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DokumentRuchRow(r: DokumentRuchDto) {
    val iloscStr = if (r.ilosc >= 0) "+${String.format("%.4f", r.ilosc)}"
                  else String.format("%.4f", r.ilosc)
    val iloscColor = if (r.ilosc >= 0) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error
    ListItem(
        headlineContent = {
            Text(r.produktNazwa ?: "—", fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(buildString {
                r.produktSku?.let { append("SKU: $it") }
                r.data?.let { if (isNotEmpty()) append("  · "); append(it.take(10)) }
                r.pracownik?.let { if (isNotEmpty()) append("  · "); append(it) }
                r.opisOperacji?.let { if (isNotEmpty()) append("  · "); append(it.take(40)) }
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(iloscStr, fontWeight = FontWeight.SemiBold, color = iloscColor)
                r.cenaJednostkowa?.let {
                    Text("${String.format("%.2f", it)} PLN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
