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
import com.example.bm_mobile.data.api.dto.SerwisDetailDto
import com.example.bm_mobile.data.api.dto.SerwisHistoriaDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerwisDetailScreen(
    serwisId: Int,
    viewModel: ZasobyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.serwisDetail.collectAsState()

    LaunchedEffect(serwisId) { viewModel.loadSerwisDetail(serwisId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zlecenie serwisowe") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadSerwisDetail(serwisId) }
                is ZasobyUiState.Success -> SerwisDetailContent(s.data)
            }
        }
    }
}

@Composable
private fun SerwisDetailContent(s: SerwisDetailDto) {
    val statusColor = when (s.status) {
        "nowe"                 -> MaterialTheme.colorScheme.primary
        "w_diagnozie"          -> MaterialTheme.colorScheme.secondary
        "oczekuje_zatwierdzenie",
        "oczekuje_wlasciciel"  -> MaterialTheme.colorScheme.error
        "zakonczone"           -> MaterialTheme.colorScheme.outline
        "anulowane"            -> MaterialTheme.colorScheme.outline
        else                   -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    LazyColumn(Modifier.fillMaxSize()) {
        // Nagłówek
        item {
            Surface(tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = statusColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small) {
                            Text(s.statusLabel,
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor)
                        }
                        val priorytetColor = when (s.priorytet) {
                            "krytyczny" -> MaterialTheme.colorScheme.error
                            "pilny"     -> MaterialTheme.colorScheme.tertiary
                            else        -> null
                        }
                        priorytetColor?.let {
                            Text(s.priorytet.replaceFirstChar { c -> c.uppercase() },
                                style = MaterialTheme.typography.labelSmall, color = it)
                        }
                    }
                    // Zasób (produkt lub pojazd)
                    val zasob = s.produktNazwa ?: s.pojazdNazwa
                    if (zasob != null) {
                        Text(zasob, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold)
                        s.magazynNazwa?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // Typ
                    s.typLabel?.let {
                        Text("Typ: $it", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        // Opis usterki
        item {
            InfoSekcja("Opis usterki") {
                Text(s.opisUsterki ?: "—", style = MaterialTheme.typography.bodyMedium)
            }
        }
        // Diagnoza
        if (!s.diagnoza.isNullOrBlank()) {
            item {
                InfoSekcja("Diagnoza") {
                    Text(s.diagnoza, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        // Prace wykonane
        if (!s.opisPracWykonanych.isNullOrBlank()) {
            item {
                InfoSekcja("Prace wykonane") {
                    Text(s.opisPracWykonanych, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        // Daty i kosztorys
        item {
            InfoSekcja("Szczegóły") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("Zgłoszono", s.dataZgloszenia)
                    InfoRow("Termin umowny", s.terminUmowny)
                    InfoRow("Zakończono", s.dataZakonczenia)
                    val kosztTotal = s.kosztorysLacznie
                    if (kosztTotal > 0) {
                        InfoRow("Kosztorys łącznie", "${String.format("%.2f", kosztTotal)} PLN")
                        s.kosztorysRobocizna?.let { InfoRow("  Robocizna", "${String.format("%.2f", it)} PLN") }
                        s.kosztorysCzesci?.let { InfoRow("  Części", "${String.format("%.2f", it)} PLN") }
                        s.roboczogodziny?.let { InfoRow("  Roboczogodziny", "$it h") }
                    }
                    InfoRow("Zglaszający", s.zglaszajacy)
                    InfoRow("Serwisant", s.serwisant)
                }
            }
        }
        // Serwis zewnętrzny
        if (!s.firmaSerwisowa.isNullOrBlank()) {
            item {
                InfoSekcja("Serwis zewnętrzny") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InfoRow("Firma", s.firmaSerwisowa)
                        InfoRow("Nr listu", s.nrListuPrzewozowego)
                        InfoRow("Wysłano", s.dataWyslania)
                        InfoRow("Odebrano", s.dataOdbioru)
                        s.kosztFaktury?.let { InfoRow("Koszt faktury", "${String.format("%.2f", it)} PLN") }
                        InfoRow("Gwarancja do", s.gwarancjaSerwisowaDo)
                    }
                }
            }
        }
        // Historia
        if (s.historia.isNotEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                    Text("Historia (${s.historia.size})",
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            items(s.historia, key = { it.id }) { h ->
                HistoriaRow(h)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun InfoSekcja(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        content()
    }
    HorizontalDivider()
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HistoriaRow(h: SerwisHistoriaDto) {
    ListItem(
        headlineContent = {
            Text(h.akcjaLabel, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                h.tresc?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text(buildString {
                    append(h.data)
                    h.uzytkownik?.let { append("  · $it") }
                    if (h.statusPrzed != null && h.statusPo != null) {
                        append("  · ${h.statusPrzed} → ${h.statusPo}")
                    }
                }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
