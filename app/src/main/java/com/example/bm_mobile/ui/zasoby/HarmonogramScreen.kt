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
import com.example.bm_mobile.data.api.dto.HarmonogramItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarmonogramScreen(viewModel: ZasobyViewModel) {
    val state by viewModel.harmonogram.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHarmonogram() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Harmonogram przeglądów") })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadHarmonogram() }
                is ZasobyUiState.Success -> {
                    val data = s.data
                    if (data.przeterminowane.isEmpty() && data.nadchodzace.isEmpty()) {
                        Text("Brak zaległych i nadchodzących przeglądów",
                            Modifier.align(Alignment.Center).padding(24.dp))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            if (data.przeterminowane.isNotEmpty()) {
                                item {
                                    HarmonogramSekcja(
                                        "Przeterminowane (${data.przeterminowane.size})",
                                        MaterialTheme.colorScheme.error
                                    )
                                }
                                items(data.przeterminowane, key = { "przet_${it.typ}_${it.id}" }) {
                                    HarmonogramRow(it, isOverdue = true)
                                }
                            }
                            if (data.nadchodzace.isNotEmpty()) {
                                item {
                                    HarmonogramSekcja(
                                        "Nadchodzące — 30 dni (${data.nadchodzace.size})",
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                items(data.nadchodzace, key = { "nadch_${it.typ}_${it.id}" }) {
                                    HarmonogramRow(it, isOverdue = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HarmonogramSekcja(text: String, color: androidx.compose.ui.graphics.Color) {
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
private fun HarmonogramRow(item: HarmonogramItemDto, isOverdue: Boolean) {
    val accentColor = if (isOverdue) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.tertiary
    val icon = if (item.typ == "pojazd") "🚛" else "📦"
    ListItem(
        headlineContent = {
            Text("$icon ${item.nazwa}", fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(buildString {
                item.info?.let { append(it) }
                item.termin?.let {
                    if (isNotEmpty()) append("  · ")
                    append("Termin: $it")
                }
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            item.termin?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = accentColor,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    )
    HorizontalDivider()
}
