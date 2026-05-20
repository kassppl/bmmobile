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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bm_mobile.data.api.dto.ProduktDetailDto
import com.example.bm_mobile.ui.nfc.NfcEvent
import com.example.bm_mobile.ui.nfc.NfcTagProtection
import com.example.bm_mobile.ui.nfc.NfcViewModel
import java.security.MessageDigest

private const val REMOVE_TAG_HASH = "cda3bc635c368c37c5d8bd3952e2ef889af1bf5bea791544d44e875e654bed63"

private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduktKartaScreen(
    produktId: Int,
    viewModel: ZasobyViewModel,
    nfcViewModel: NfcViewModel,
    onSerwisClick: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.produkt.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(produktId) { viewModel.loadProdukt(produktId) }

    // Po przypisaniu / usunięciu tagu — przeładuj dane produktu
    LaunchedEffect(Unit) {
        nfcViewModel.events.collect { event ->
            when (event) {
                is NfcEvent.TagAssigned -> if (event.produktId == produktId) viewModel.loadProdukt(produktId)
                is NfcEvent.TagRemoved  -> if (event.produktId == produktId) viewModel.loadProdukt(produktId)

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? ZasobyUiState.Success)?.data?.nazwa ?: "Produkt"
                    Text(title)
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Wróć") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is ZasobyUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ZasobyUiState.Error -> ZasobyErrorBox(s.message) { viewModel.loadProdukt(produktId) }
                is ZasobyUiState.Success -> {
                    val p = s.data
                    Column(Modifier.fillMaxSize()) {

                        ProduktNaglowek(p)
                        NfcSekcja(p, nfcViewModel)

                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                                text = { Text("Ruchy") })
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                                text = { Text("Serwis (${p.serwisy.size})") })
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                                text = { Text("Przeglądy") })
                            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 },
                                text = { Text("Cert. (${p.certyfikaty.size})") })
                        }

                        when (selectedTab) {
                            0 -> RuchyTab(p)
                            1 -> SerwisTab(p, onSerwisClick)
                            2 -> PrzegladyTab(p)
                            3 -> CertyfikatyTab(p)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProduktNaglowek(p: ProduktDetailDto) {
    Surface(tonalElevation = 1.dp) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusColor = when (p.statusSprzetu) {
                    "sprawny"      -> MaterialTheme.colorScheme.tertiary
                    "w_serwisie"   -> MaterialTheme.colorScheme.secondary
                    "do_przegladu" -> MaterialTheme.colorScheme.error
                    else           -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(color = statusColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        p.statusSprzetu ?: "—",
                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                Text(
                    "Stan: ${String.format("%.2f", p.stan)} ${p.jednostkaMiary ?: "szt"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                buildString {
                    p.producent?.let { append(it) }
                    p.model?.let { append(" $it") }
                    p.sku?.let { if (isNotEmpty()) append("  · "); append("SKU: $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                buildString {
                    p.numerSeryjnyWewn?.let { append("S/N: $it") }
                    p.lokalizacja?.let { if (isNotEmpty()) append("  · "); append(it) }
                    p.magazynNazwa?.let { if (isNotEmpty()) append("  · "); append(it) }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (p.wartoscZakupu != null || p.dataZakupu != null) {
                Text(
                    buildString {
                        p.wartoscZakupu?.let { append("Wartość: ${String.format("%.2f", it)} PLN") }
                        p.dataZakupu?.let { if (isNotEmpty()) append("  · "); append("Zakup: $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            p.terminNastepnegoPrzegl?.let {
                Text(
                    "Następny przegląd: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NfcSekcja(p: ProduktDetailDto, nfcVm: NfcViewModel) {
    var showRemoveDialog  by remember { mutableStateOf(false) }
    var showProtectDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        UsunTagDialog(
            onConfirm = { nfcVm.removeTag(p.id); showRemoveDialog = false },
            onDismiss = { showRemoveDialog = false }
        )
    }

    if (showProtectDialog) {
        WybierzOchroneDialog(
            onConfirm = { protection ->
                nfcVm.startAssignMode(
                    produktId    = p.id,
                    produktNazwa = p.nazwa,
                    firmaNazwa   = p.firmaNazwa ?: "BM",
                    protection   = protection,
                )
                showProtectDialog = false
            },
            onDismiss = { showProtectDialog = false }
        )
    }

    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (p.tagNfcId != null) {
                Column {
                    Text(
                        "📡 NFC: ${p.tagNfcId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    p.tagNfcType?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                TextButton(
                    onClick = { showRemoveDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń tag") }
            } else {
                Text(
                    "Brak tagu NFC",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showProtectDialog = true }) { Text("Przypisz tag") }
            }
        }
    }
}

@Composable
private fun WybierzOchroneDialog(
    onConfirm: (NfcTagProtection) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(NfcTagProtection.NONE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zabezpieczenie tagu NFC") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Na tagu zostaną zapisane: nazwa firmy, narzędzia i data przypisania.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OchronaOption(
                    label       = "Bez zabezpieczenia",
                    description = "Tag można ponownie zapisać dowolną aplikacją",
                    selected    = selected == NfcTagProtection.NONE,
                    onClick     = { selected = NfcTagProtection.NONE }
                )
                OchronaOption(
                    label       = "Hasłem (chroniony zapis)",
                    description = "Odczyt swobodny, nadpisanie wymaga hasła aplikacji",
                    selected    = selected == NfcTagProtection.PASSWORD,
                    onClick     = { selected = NfcTagProtection.PASSWORD }
                )
                OchronaOption(
                    label       = "Trwała blokada (tylko odczyt)",
                    description = "Tag staje się niemodyfikowalny na zawsze — nieodwracalne!",
                    selected    = selected == NfcTagProtection.READONLY,
                    onClick     = { selected = NfcTagProtection.READONLY }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) { Text("Przypisz i skanuj") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
private fun OchronaOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (selected) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UsunTagDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var haslo by remember { mutableStateOf("") }
    var blad by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potwierdzenie usunięcia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Podaj hasło administratora, aby odpiąć tag NFC.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = haslo,
                    onValueChange = { haslo = it; blad = false },
                    label = { Text("Hasło") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = blad,
                    supportingText = if (blad) {{ Text("Nieprawidłowe hasło") }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sha256(haslo) == REMOVE_TAG_HASH) {
                        onConfirm()
                    } else {
                        blad = true
                        haslo = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Usuń tag")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
private fun RuchyTab(p: ProduktDetailDto) {
    if (p.ruchy.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak ruchów magazynowych") }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(p.ruchy, key = { it.id }) { r ->
            val iloscStr = if (r.ilosc >= 0) "+${String.format("%.2f", r.ilosc)}"
                           else String.format("%.2f", r.ilosc)
            val color = if (r.ilosc >= 0) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.error
            ListItem(
                headlineContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.dokTyp?.let { t ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small) {
                                Text(t, Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(r.dokNumer ?: "—", fontWeight = FontWeight.Medium)
                    }
                },
                supportingContent = {
                    Text(buildString {
                        r.data?.let { append(it.take(10)) }
                        r.opisOperacji?.let { if (isNotEmpty()) append("  · "); append(it.take(50)) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    Text(iloscStr, fontWeight = FontWeight.SemiBold, color = color)
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun SerwisTab(p: ProduktDetailDto, onSerwisClick: (Int) -> Unit) {
    if (p.serwisy.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak zleceń serwisowych") }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(p.serwisy, key = { it.id }) { s ->
            val statusColor = when (s.status) {
                "nowe"       -> MaterialTheme.colorScheme.primary
                "zakonczone" -> MaterialTheme.colorScheme.outline
                "anulowane"  -> MaterialTheme.colorScheme.outline
                else         -> MaterialTheme.colorScheme.secondary
            }
            ListItem(
                modifier = Modifier.clickable { onSerwisClick(s.id) },
                headlineContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = statusColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                            Text(s.statusLabel,
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = statusColor)
                        }
                        Text(s.opisUsterki?.take(50) ?: "—",
                            fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                },
                supportingContent = {
                    s.dataZgl?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun PrzegladyTab(p: ProduktDetailDto) {
    if (p.przeglady.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak przeglądów") }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(p.przeglady, key = { it.id }) { prz ->
            val waznyColor = if (prz.wazny) MaterialTheme.colorScheme.tertiary
                             else MaterialTheme.colorScheme.error
            ListItem(
                headlineContent = { Text(prz.typLabel, fontWeight = FontWeight.Medium) },
                supportingContent = {
                    Text(buildString {
                        prz.dataPrzegl?.let { append(it) }
                        prz.wykonawca?.let { if (isNotEmpty()) append("  · "); append(it) }
                        prz.nastepny?.let { if (isNotEmpty()) append("  · następny: "); append(it) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingContent = {
                    Text(if (prz.wazny) "Ważny" else "Nieważny",
                        style = MaterialTheme.typography.labelSmall, color = waznyColor)
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun CertyfikatyTab(p: ProduktDetailDto) {
    if (p.certyfikaty.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Brak certyfikatów") }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(p.certyfikaty, key = { it.id }) { c ->
            val waznyColor = if (c.wazny) MaterialTheme.colorScheme.tertiary
                             else MaterialTheme.colorScheme.error
            ListItem(
                headlineContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = waznyColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                            Text(if (c.wazny) "Ważny" else "Nieważny",
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = waznyColor)
                        }
                        Text(c.typ ?: "—", fontWeight = FontWeight.Medium)
                    }
                },
                supportingContent = {
                    Text(buildString {
                        c.numer?.let { append("Nr: $it") }
                        c.wydawca?.let { if (isNotEmpty()) append("  · "); append(it) }
                        c.dataWydania?.let { if (isNotEmpty()) append("  · "); append(it) }
                        c.dataWaznosci?.let { if (isNotEmpty()) append(" do "); append(it) }
                    }, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )
            HorizontalDivider()
        }
    }
}

private fun Modifier.clickableIf(block: () -> Unit): Modifier = this.clickable(onClick = block)
