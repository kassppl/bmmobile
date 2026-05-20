package com.example.bm_mobile

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bm_mobile.data.api.ApiClient
import com.example.bm_mobile.data.api.dto.MagazynDto
import com.example.bm_mobile.data.api.dto.PojazdDto
import com.example.bm_mobile.data.repository.AuthRepository
import com.example.bm_mobile.ui.login.LoginScreen
import com.example.bm_mobile.ui.login.LoginViewModel
import com.example.bm_mobile.ui.login.WelcomeScreen
import com.example.bm_mobile.ui.magazyn.MagazynDashboardScreen
import com.example.bm_mobile.ui.magazyn.MagazynListScreen
import com.example.bm_mobile.ui.magazyn.MagazynViewModel
import com.example.bm_mobile.ui.nfc.NfcEvent
import com.example.bm_mobile.ui.nfc.NfcMode
import com.example.bm_mobile.ui.nfc.NfcTagProtection
import com.example.bm_mobile.ui.nfc.NfcViewModel
import com.example.bm_mobile.ui.pojazd.PojazdKartaScreen
import com.example.bm_mobile.ui.pojazd.PojazdListScreen
import com.example.bm_mobile.ui.pojazd.PojazdViewModel
import com.example.bm_mobile.ui.theme.BmmobileTheme
import com.example.bm_mobile.ui.zasoby.DokumentDetailScreen
import com.example.bm_mobile.ui.zasoby.HarmonogramScreen
import com.example.bm_mobile.ui.zasoby.InwentaryzacjaDetailScreen
import com.example.bm_mobile.ui.zasoby.InwentaryzacjeScreen
import com.example.bm_mobile.ui.zasoby.NaprawyListScreen
import com.example.bm_mobile.ui.zasoby.ProduktKartaScreen
import com.example.bm_mobile.ui.zasoby.SerwisDetailScreen
import com.example.bm_mobile.ui.zasoby.SerwisListScreen
import com.example.bm_mobile.ui.zasoby.WyjazdListScreen
import com.example.bm_mobile.ui.zasoby.WyjazdSzczegolyScreen
import com.example.bm_mobile.ui.zasoby.ZamowienieDetailScreen
import com.example.bm_mobile.ui.zasoby.ZamowieniaListScreen
import com.example.bm_mobile.ui.zasoby.ZasobyViewModel
import kotlinx.coroutines.launch

private enum class MainTab { MAGAZYNY, SERWIS, HARMONOGRAM, WYJAZDY, POJAZDY }

private sealed class DetailScreen {
    data class Magazyn(val dto: MagazynDto) : DetailScreen()
    data class Pojazd(val dto: PojazdDto) : DetailScreen()
    data class Dokument(val id: Int) : DetailScreen()
    data class Produkt(val id: Int) : DetailScreen()
    data class Serwis(val id: Int) : DetailScreen()
    data class Wyjazd(val id: Int) : DetailScreen()
    data class Inwentaryzacje(val magazynId: Int, val magazynNazwa: String) : DetailScreen()
    data class Inwentaryzacja(val id: Int) : DetailScreen()
    data class Zamowienie(val id: Int) : DetailScreen()
    object ZamowieniaLista : DetailScreen()
    object NaprawyLista : DetailScreen()
}

class MainActivity : ComponentActivity() {

    private val nfcVm: NfcViewModel by viewModels { NfcViewModel.factory(ApiClient.api) }
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val app = application as BmApplication
        val authRepo = AuthRepository(ApiClient.api, app.tokenStore)

        enableEdgeToEdge()
        setContent {
            BmmobileTheme {
                var isLoggedIn by remember { mutableStateOf(authRepo.isLoggedIn()) }
                var showLogin by remember { mutableStateOf(false) }

                when {
                    isLoggedIn -> MainScaffold(onLogout = {
                        authRepo.logout()
                        isLoggedIn = false
                        showLogin = false
                    })
                    showLogin -> {
                        val loginVm: LoginViewModel = viewModel(
                            factory = LoginViewModel.factory(authRepo)
                        )
                        LoginScreen(loginVm) { isLoggedIn = true }
                    }
                    else -> WelcomeScreen(onLoginClick = { showLogin = true })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(this, pi, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            val tagId   = tag?.id?.joinToString("") { "%02X".format(it) }
            val tagType = tag?.techList?.firstOrNull()?.substringAfterLast('.')
            if (tag != null && tagId != null) nfcVm.handleTag(tag, tagId, tagType)
        }
    }
}

@Composable
private fun MainScaffold(onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(MainTab.MAGAZYNY) }
    val detailStack = remember { mutableStateListOf<DetailScreen>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentDetail = detailStack.lastOrNull()
    val inDetail = currentDetail != null

    fun push(screen: DetailScreen) = detailStack.add(screen)
    fun pop() { if (detailStack.isNotEmpty()) detailStack.removeLast() }

    BackHandler(enabled = inDetail) { pop() }

    val nfcVm: NfcViewModel = viewModel(factory = NfcViewModel.factory(ApiClient.api))
    val nfcMode by nfcVm.mode.collectAsState()

    // Obsługa zdarzeń NFC — nawigacja i komunikaty
    LaunchedEffect(Unit) {
        nfcVm.events.collect { event ->
            when (event) {
                is NfcEvent.NavigateToProduct -> push(DetailScreen.Produkt(event.produktId))
                is NfcEvent.TagNotFound -> scope.launch {
                    snackbarHostState.showSnackbar("Tag NFC nie jest przypisany do żadnego narzędzia")
                }
                is NfcEvent.Error -> scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
                is NfcEvent.TagAssigned -> scope.launch {
                    val ndefInfo = if (event.ndefOk) "" else " (nie zapisano danych na tagu)"
                    val protInfo = when (event.protectionResult) {
                        true  -> " · tag zabezpieczony"
                        false -> " · UWAGA: zabezpieczenie nie powiodło się"
                        null  -> ""
                    }
                    snackbarHostState.showSnackbar("Tag przypisany$ndefInfo$protInfo")
                }
                else -> { /* TagRemoved obsługiwany w ProduktKartaScreen */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!inDetail) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == MainTab.MAGAZYNY,
                        onClick = { currentTab = MainTab.MAGAZYNY; detailStack.clear() },
                        icon = { Text("📦") }, label = { Text("Magazyny") }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.SERWIS,
                        onClick = { currentTab = MainTab.SERWIS; detailStack.clear() },
                        icon = { Text("🔧") }, label = { Text("Serwis") }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.HARMONOGRAM,
                        onClick = { currentTab = MainTab.HARMONOGRAM; detailStack.clear() },
                        icon = { Text("📅") }, label = { Text("Przeglądy") }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.WYJAZDY,
                        onClick = { currentTab = MainTab.WYJAZDY; detailStack.clear() },
                        icon = { Text("🚌") }, label = { Text("Wyjazdy") }
                    )
                    NavigationBarItem(
                        selected = currentTab == MainTab.POJAZDY,
                        onClick = { currentTab = MainTab.POJAZDY; detailStack.clear() },
                        icon = { Text("🚛") }, label = { Text("Pojazdy") }
                    )
                }
            }
        }
    ) { _ ->

        // Baner trybu przypisywania NFC
        if (nfcMode is NfcMode.WaitingForAssign) {
            NfcAssignBanner(
                produktNazwa = (nfcMode as NfcMode.WaitingForAssign).produktNazwa,
                protection   = (nfcMode as NfcMode.WaitingForAssign).protection,
                onCancel     = { nfcVm.cancelAssignMode() },
            )
            return@Scaffold
        }

        when (val detail = currentDetail) {

            is DetailScreen.Magazyn -> {
                val magVm: MagazynViewModel = viewModel(factory = MagazynViewModel.factory(ApiClient.api))
                MagazynDashboardScreen(
                    magazyn = detail.dto,
                    viewModel = magVm,
                    onProduktClick = { push(DetailScreen.Produkt(it)) },
                    onDokumentClick = { push(DetailScreen.Dokument(it)) },
                    onInwentaryzacjeClick = {
                        push(DetailScreen.Inwentaryzacje(detail.dto.id, detail.dto.nazwa))
                    },
                    onBack = ::pop
                )
            }

            is DetailScreen.Pojazd -> {
                val pojVm: PojazdViewModel = viewModel(factory = PojazdViewModel.factory(ApiClient.api))
                PojazdKartaScreen(pojazd = detail.dto, viewModel = pojVm, onBack = ::pop)
            }

            is DetailScreen.Dokument -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                DokumentDetailScreen(dokumentId = detail.id, viewModel = zasobyVm, onBack = ::pop)
            }

            is DetailScreen.Produkt -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                ProduktKartaScreen(
                    produktId = detail.id,
                    viewModel = zasobyVm,
                    nfcViewModel = nfcVm,
                    onSerwisClick = { push(DetailScreen.Serwis(it)) },
                    onBack = ::pop
                )
            }

            is DetailScreen.Serwis -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                SerwisDetailScreen(serwisId = detail.id, viewModel = zasobyVm, onBack = ::pop)
            }

            is DetailScreen.Wyjazd -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                WyjazdSzczegolyScreen(wyjazdId = detail.id, viewModel = zasobyVm, onBack = ::pop)
            }

            is DetailScreen.Inwentaryzacje -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                InwentaryzacjeScreen(
                    magazynId = detail.magazynId,
                    magazynNazwa = detail.magazynNazwa,
                    viewModel = zasobyVm,
                    onInwClick = { push(DetailScreen.Inwentaryzacja(it)) },
                    onBack = ::pop
                )
            }

            is DetailScreen.Inwentaryzacja -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                InwentaryzacjaDetailScreen(inwId = detail.id, viewModel = zasobyVm, onBack = ::pop)
            }

            is DetailScreen.ZamowieniaLista -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                ZamowieniaListScreen(
                    viewModel = zasobyVm,
                    onZamowienieClick = { push(DetailScreen.Zamowienie(it)) },
                    onBack = ::pop
                )
            }

            is DetailScreen.NaprawyLista -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                NaprawyListScreen(viewModel = zasobyVm, onBack = ::pop)
            }

            is DetailScreen.Zamowienie -> {
                val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                ZamowienieDetailScreen(zamowienieId = detail.id, viewModel = zasobyVm, onBack = ::pop)
            }

            null -> when (currentTab) {
                MainTab.MAGAZYNY -> {
                    val magVm: MagazynViewModel = viewModel(factory = MagazynViewModel.factory(ApiClient.api))
                    MagazynListScreen(
                        viewModel = magVm,
                        onMagazynClick = { push(DetailScreen.Magazyn(it)) },
                        onLogout = onLogout
                    )
                }
                MainTab.SERWIS -> {
                    val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                    SerwisListScreen(
                        viewModel = zasobyVm,
                        onSerwisClick = { push(DetailScreen.Serwis(it)) },
                        onZamowieniaClick = { push(DetailScreen.ZamowieniaLista) },
                        onNaprawyClick = { push(DetailScreen.NaprawyLista) }
                    )
                }
                MainTab.HARMONOGRAM -> {
                    val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                    HarmonogramScreen(zasobyVm)
                }
                MainTab.WYJAZDY -> {
                    val zasobyVm: ZasobyViewModel = viewModel(factory = ZasobyViewModel.factory(ApiClient.api))
                    WyjazdListScreen(
                        viewModel = zasobyVm,
                        onWyjazdClick = { push(DetailScreen.Wyjazd(it)) }
                    )
                }
                MainTab.POJAZDY -> {
                    val pojVm: PojazdViewModel = viewModel(factory = PojazdViewModel.factory(ApiClient.api))
                    PojazdListScreen(
                        viewModel = pojVm,
                        onPojazdClick = { push(DetailScreen.Pojazd(it)) },
                        onBack = { currentTab = MainTab.MAGAZYNY }
                    )
                }
            }
        }
    }
}

@Composable
private fun NfcAssignBanner(
    produktNazwa: String,
    protection: NfcTagProtection,
    onCancel: () -> Unit,
) {
    val protectionLabel = when (protection) {
        NfcTagProtection.NONE     -> "Dane zostaną zapisane na tagu"
        NfcTagProtection.PASSWORD -> "Tag zostanie zabezpieczony hasłem"
        NfcTagProtection.READONLY -> "Tag zostanie trwale zablokowany (tylko odczyt)"
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📡", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text("Przyłóż telefon do tagu NFC", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                produktNazwa,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                protectionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onCancel) { Text("Anuluj") }
        }
    }
}
