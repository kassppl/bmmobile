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
import com.example.bm_mobile.data.api.dto.DokumentDto
import com.example.bm_mobile.data.api.dto.MagazynDto
import com.example.bm_mobile.data.api.dto.ProduktDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazynDashboardScreen(
    magazyn: MagazynDto,
    viewModel: MagazynViewModel,
    onProduktClick: (Int) -> Unit,
    onDokumentClick: (Int) -> Unit,
    onInwentaryzacjeClick: () -> Unit,
    onBack: () -> Unit,
) {
    val produktyState by viewModel.produkty.collectAsState()
    val dokumentyState by viewModel.dokumenty.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(magazyn.id) {
        viewModel.loadProdukty(magazyn.id)
        viewModel.loadDokumenty(magazyn.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(magazyn.nazwa) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("‹ Wróć") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text("Zasoby") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text("Dokumenty") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text("Inwentaryzacje") })
            }

            when (selectedTab) {
                0 -> ProduktyTab(produktyState, onProduktClick) { viewModel.loadProdukty(magazyn.id) }
                1 -> DokumentyTab(dokumentyState, onDokumentClick) { viewModel.loadDokumenty(magazyn.id) }
                2 -> InwentaryzacjeTab(onInwentaryzacjeClick)
            }
        }
    }
}

@Composable
private fun ProduktyTab(state: MagazynUiState, onProduktClick: (Int) -> Unit, onRetry: () -> Unit) {
    when (state) {
        is MagazynUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is MagazynUiState.Error -> ErrorBox(state.message, onRetry)
        is MagazynUiState.Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            val lista = state.data as List<ProduktDto>
            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak produktów") }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(lista, key = { it.id }) { ProduktRow(it, onProduktClick) }
                }
            }
        }
    }
}

@Composable
private fun ProduktRow(p: ProduktDto, onClick: (Int) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick(p.id) },
        headlineContent = { Text(p.nazwa, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(buildString {
                p.sku?.let { append("SKU: $it") }
                p.numerSeryjnyWewn?.let { if (isNotEmpty()) append("  ·  "); append("S/N: $it") }
                p.producent?.let { if (isNotEmpty()) append("  ·  "); append(it) }
                p.model?.let { append(" $it") }
            }, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                val stanTxt = if (p.stan % 1.0 == 0.0) p.stan.toInt().toString()
                              else "%.2f".format(p.stan)
                Text("$stanTxt ${p.jednostkaMiary ?: "szt"}", fontWeight = FontWeight.SemiBold)
                p.statusSprzetu?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = statusColor(it))
                }
            }
        }
    )
    HorizontalDivider()
}

@Composable
private fun DokumentyTab(state: MagazynUiState, onDokumentClick: (Int) -> Unit, onRetry: () -> Unit) {
    when (state) {
        is MagazynUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is MagazynUiState.Error -> ErrorBox(state.message, onRetry)
        is MagazynUiState.Success<*> -> {
            @Suppress("UNCHECKED_CAST")
            val lista = state.data as List<DokumentDto>
            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak dokumentów") }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(lista, key = { it.id }) { DokumentRow(it, onDokumentClick) }
                }
            }
        }
    }
}

@Composable
private fun DokumentRow(d: DokumentDto, onClick: (Int) -> Unit) {
    val typColor = when (d.typ) {
        "PZ", "PW" -> MaterialTheme.colorScheme.tertiary
        "WZ", "RW" -> MaterialTheme.colorScheme.error
        "MM"       -> MaterialTheme.colorScheme.secondary
        else       -> MaterialTheme.colorScheme.primary
    }
    ListItem(
        modifier = Modifier.clickable { onClick(d.id) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = typColor, shape = MaterialTheme.shapes.small) {
                    Text(d.typ, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary)
                }
                Text(d.numer, fontWeight = FontWeight.Medium)
            }
        },
        supportingContent = {
            Text(buildString {
                d.kontrahent?.let { append(it) }
                d.pracownik?.let { if (isNotEmpty()) append("  ·  "); append(it) }
            }, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Text(d.dataWystawienia ?: "", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
    HorizontalDivider()
}

@Composable
private fun InwentaryzacjeTab(onInwentaryzacjeClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onInwentaryzacjeClick) {
            Text("Przeglądaj inwentaryzacje")
        }
    }
}

@Composable
private fun ErrorBox(msg: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
    }
}

@Composable
private fun statusColor(status: String) = when (status) {
    "sprawny"     -> MaterialTheme.colorScheme.tertiary
    "w_serwisie"  -> MaterialTheme.colorScheme.secondary
    "do_przegladu"-> MaterialTheme.colorScheme.error
    else          -> MaterialTheme.colorScheme.onSurfaceVariant
}
