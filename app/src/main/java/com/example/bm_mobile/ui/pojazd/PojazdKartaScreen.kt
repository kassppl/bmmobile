package com.example.bm_mobile.ui.pojazd

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bm_mobile.data.api.dto.PojazdDto
import com.example.bm_mobile.data.api.dto.PrzegladDto
import com.example.bm_mobile.data.api.dto.SerwisDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PojazdKartaScreen(
    pojazd: PojazdDto,
    viewModel: PojazdViewModel,
    onBack: () -> Unit
) {
    val serwisyState by viewModel.serwisy.collectAsState()
    val przegladyState by viewModel.przeglady.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(pojazd.id) {
        viewModel.loadSerwisy(pojazd.id)
        viewModel.loadPrzeglady(pojazd.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pojazd.nazwa) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ Wróć") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Dane podstawowe
            Surface(tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    pojazd.typ?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    val info = buildString {
                        pojazd.rejestracja?.let { append("Nr rej: $it") }
                        if (pojazd.kierowcaImie != null) {
                            if (isNotEmpty()) append("  ·  ")
                            append("Kierowca: ${pojazd.kierowcaImie} ${pojazd.kierowcaNazwisko}")
                        }
                        pojazd.ocDo?.let { if (isNotEmpty()) append("  ·  "); append("OC do: $it") }
                    }
                    if (info.isNotBlank()) {
                        Text(info, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Serwis") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Przeglądy") })
            }

            when (selectedTab) {
                0 -> SerwisyTab(serwisyState) { viewModel.loadSerwisy(pojazd.id) }
                1 -> PrzegladyTab(przegladyState) { viewModel.loadPrzeglady(pojazd.id) }
            }
        }
    }
}

@Composable
private fun SerwisyTab(state: PojazdUiState, onRetry: () -> Unit) {
    when (state) {
        is PojazdUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is PojazdUiState.Error -> ErrorBox(state.message, onRetry)
        is PojazdUiState.Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            val lista = state.data as List<SerwisDto>
            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak zleceń serwisowych") }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(lista, key = { it.id }) { SerwisRow(it) }
                }
            }
        }
    }
}

@Composable
private fun SerwisRow(s: SerwisDto) {
    val statusColor = when (s.status) {
        "nowe"                       -> MaterialTheme.colorScheme.primary
        "w_diagnozie"                -> MaterialTheme.colorScheme.secondary
        "oczekuje_na_zatwierdzenie"  -> MaterialTheme.colorScheme.tertiary
        "zakonczone"                 -> MaterialTheme.colorScheme.outline
        "anulowane"                  -> MaterialTheme.colorScheme.error
        else                         -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val priorytetColor = when (s.priorytet) {
        "krytyczny" -> MaterialTheme.colorScheme.error
        "pilny"     -> MaterialTheme.colorScheme.tertiary
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    ListItem(
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
                Text(s.opisUsterki?.take(60) ?: "—", fontWeight = FontWeight.Medium,
                    maxLines = 1)
            }
        },
        supportingContent = {
            Text(buildString {
                s.dataZgloszenia?.let { append(it) }
                s.terminUmowny?.let { append("  · termin: $it") }
                if (s.kosztorys > 0) append("  · ${String.format("%.0f", s.kosztorys)} PLN")
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            if (s.priorytet != "normalny") {
                Text(s.priorytet.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = priorytetColor)
            }
        }
    )
    HorizontalDivider()
}

@Composable
private fun PrzegladyTab(state: PojazdUiState, onRetry: () -> Unit) {
    when (state) {
        is PojazdUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is PojazdUiState.Error -> ErrorBox(state.message, onRetry)
        is PojazdUiState.Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            val lista = state.data as List<PrzegladDto>
            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak przeglądów") }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(lista, key = { it.id }) { PrzegladRow(it) }
                }
            }
        }
    }
}

@Composable
private fun PrzegladRow(p: PrzegladDto) {
    ListItem(
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                val waznyColor = if (p.wazny) MaterialTheme.colorScheme.tertiary
                                 else MaterialTheme.colorScheme.error
                Surface(color = waznyColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(if (p.wazny) "Ważny" else "Nieważny",
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = waznyColor)
                }
                Text(p.nazwa ?: p.typLabel, fontWeight = FontWeight.Medium)
            }
        },
        supportingContent = {
            Text(buildString {
                p.dataPrzegladu?.let { append(it) }
                p.wykonawca?.let { if (isNotEmpty()) append("  · "); append(it) }
                p.koszt?.let { if (isNotEmpty()) append("  · "); append("${String.format("%.0f", it)} PLN") }
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            p.dataNastepnegoPrzegladu?.let {
                Text("Następny:\n$it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
    HorizontalDivider()
}

@Composable
private fun ErrorBox(msg: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
    }
}
