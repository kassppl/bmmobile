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
import com.example.bm_mobile.data.api.dto.NaprawyDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaprawyListScreen(viewModel: ZasobyViewModel, onBack: () -> Unit = {}) {
    val state by viewModel.naprawy.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNaprawy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Naprawy") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadNaprawy() }
                is ZasobyUiState.Success -> {
                    if (s.data.isEmpty()) {
                        Text("Brak napraw", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.data, key = { it.id }) { n ->
                                NaprawyRow(n)
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
private fun NaprawyRow(n: NaprawyDto) {
    val wykonaneColor = when (n.wykonane) {
        true  -> MaterialTheme.colorScheme.tertiary
        false -> MaterialTheme.colorScheme.error
        null  -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val wykonaneLabel = when (n.wykonane) {
        true  -> "Wykonane"
        false -> "Niewykonane"
        null  -> "—"
    }
    ListItem(
        headlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = wykonaneColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small) {
                    Text(wykonaneLabel,
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = wykonaneColor)
                }
                Text(
                    buildString {
                        n.imie?.let { append(it) }
                        n.nazwisko?.let { append(" $it") }
                        if (isEmpty()) append("—")
                    },
                    fontWeight = FontWeight.Medium, maxLines = 1
                )
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                n.opisUsterki?.let {
                    Text(it.take(80), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(buildString {
                    n.jednostka?.let { append(it) }
                    n.miejsce?.let { if (isNotEmpty()) append("  · "); append(it) }
                    n.czaszlecenia?.let { if (isNotEmpty()) append("  · "); append(it.take(10)) }
                    n.pojazdNazwa?.let { if (isNotEmpty()) append("  · "); append(it) }
                }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        trailingContent = {
            val wim = n.wykonawcaImie
            val wnaz = n.wykonawcaNazwisko
            if (wim != null) {
                Text("$wim $wnaz",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
