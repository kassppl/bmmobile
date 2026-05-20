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
import com.example.bm_mobile.data.api.dto.SerwisGlobalDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerwisListScreen(
    viewModel: ZasobyViewModel,
    onSerwisClick: (Int) -> Unit,
    onZamowieniaClick: () -> Unit = {},
    onNaprawyClick: () -> Unit = {},
) {
    val state by viewModel.serwisy.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSerwisy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serwis") },
                actions = {
                    TextButton(onClick = onNaprawyClick) { Text("Naprawy") }
                    TextButton(onClick = onZamowieniaClick) { Text("Zamówienia") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadSerwisy() }
                is ZasobyUiState.Success -> {
                    val data = s.data
                    val wszystkie = data.oczekujace + data.otwarte
                    if (wszystkie.isEmpty()) {
                        Text("Brak aktywnych zleceń serwisowych", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            if (data.oczekujace.isNotEmpty()) {
                                item {
                                    SerwisSekcjaHeader("Oczekują na zatwierdzenie (${data.oczekujace.size})",
                                        MaterialTheme.colorScheme.error)
                                }
                                items(data.oczekujace, key = { it.id }) { SerwisRow(it, onSerwisClick) }
                            }
                            if (data.otwarte.isNotEmpty()) {
                                item {
                                    SerwisSekcjaHeader("Aktywne (${data.otwarte.size})",
                                        MaterialTheme.colorScheme.primary)
                                }
                                items(data.otwarte, key = { "o_${it.id}" }) { SerwisRow(it, onSerwisClick) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SerwisSekcjaHeader(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.08f)) {
        Text(
            text,
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SerwisRow(s: SerwisGlobalDto, onSerwisClick: (Int) -> Unit = {}) {
    val statusColor = when (s.status) {
        "nowe"                      -> MaterialTheme.colorScheme.primary
        "w_diagnozie"               -> MaterialTheme.colorScheme.secondary
        "oczekuje_zatwierdzenie",
        "oczekuje_wlasciciel"       -> MaterialTheme.colorScheme.error
        "zakonczone"                -> MaterialTheme.colorScheme.outline
        "anulowane"                 -> MaterialTheme.colorScheme.outline
        else                        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val priorytetColor = when (s.priorytet) {
        "krytyczny" -> MaterialTheme.colorScheme.error
        "pilny"     -> MaterialTheme.colorScheme.tertiary
        else        -> null
    }
    ListItem(
        modifier = Modifier.clickable { onSerwisClick(s.id) },
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(s.statusLabel,
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor)
                }
                Text(s.opisUsterki?.take(55) ?: "—",
                    fontWeight = FontWeight.Medium, maxLines = 1)
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val zasob = s.produktNazwa ?: s.pojazdNazwa
                if (zasob != null) {
                    Text(buildString {
                        append(zasob)
                        s.magazynNazwa?.let { append("  · $it") }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(buildString {
                    s.dataZgloszenia?.let { append(it) }
                    s.terminUmowny?.let { append("  · termin: $it") }
                    if (s.kosztorys > 0) append("  · ${String.format("%.0f", s.kosztorys)} PLN")
                }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        trailingContent = {
            priorytetColor?.let {
                Text(s.priorytet.replaceFirstChar { c -> c.uppercase() },
                    style = MaterialTheme.typography.labelSmall, color = it)
            }
        }
    )
    HorizontalDivider()
}

@Composable
fun ZasobyErrorBox(msg: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(msg, color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
    }
}
