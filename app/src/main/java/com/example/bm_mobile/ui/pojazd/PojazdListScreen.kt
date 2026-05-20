package com.example.bm_mobile.ui.pojazd

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
import com.example.bm_mobile.data.api.dto.PojazdDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PojazdListScreen(
    viewModel: PojazdViewModel,
    onPojazdClick: (PojazdDto) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.pojazdy.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPojazdy() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pojazdy") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ Wróć") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is PojazdUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is PojazdUiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadPojazdy() }) { Text("Spróbuj ponownie") }
                }

                is PojazdUiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val pojazdy = s.data as List<PojazdDto>
                    if (pojazdy.isEmpty()) {
                        Text("Brak pojazdów", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(pojazdy, key = { it.id }) { p ->
                                PojazdItem(p, onClick = { onPojazdClick(p) })
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
private fun PojazdItem(p: PojazdDto, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(p.nazwa, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            val sub = buildString {
                p.typ?.let { append(it) }
                p.rejestracja?.let { if (isNotEmpty()) append(" · "); append(it) }
                if (p.kierowcaImie != null) {
                    if (isNotEmpty()) append(" · ")
                    append("${p.kierowcaImie} ${p.kierowcaNazwisko}")
                }
            }
            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Text("›", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
}
