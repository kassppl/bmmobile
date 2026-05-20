package com.example.bm_mobile.ui.magazyn

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
import com.example.bm_mobile.data.api.dto.MagazynDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazynListScreen(
    viewModel: MagazynViewModel,
    onMagazynClick: (MagazynDto) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.magazyny.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMagazyny() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Magazyny") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Wyloguj") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is MagazynUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is MagazynUiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadMagazyny() }) { Text("Spróbuj ponownie") }
                }

                is MagazynUiState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val magazyny = s.data as List<MagazynDto>
                    if (magazyny.isEmpty()) {
                        Text("Brak magazynów", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(magazyny, key = { it.id }) { mag ->
                                MagazynItem(mag, onClick = { onMagazynClick(mag) })
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
private fun MagazynItem(mag: MagazynDto, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(mag.nazwa, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            val sub = buildString {
                mag.typ?.let { append(it.uppercase()) }
                if (mag.pracownikImie != null) {
                    if (isNotEmpty()) append(" · ")
                    append("${mag.pracownikImie} ${mag.pracownikNazwisko}")
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
