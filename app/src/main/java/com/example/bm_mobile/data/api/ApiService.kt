package com.example.bm_mobile.data.api

import com.example.bm_mobile.data.api.dto.DokumentDetailDto
import com.example.bm_mobile.data.api.dto.NfcAssignRequest
import com.example.bm_mobile.data.api.dto.NfcLookupDto
import com.example.bm_mobile.data.api.dto.DokumentDto
import com.example.bm_mobile.data.api.dto.HarmonogramResponse
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDetailDto
import com.example.bm_mobile.data.api.dto.InwentaryzacjaDto
import com.example.bm_mobile.data.api.dto.LoginRequest
import com.example.bm_mobile.data.api.dto.LoginResponse
import com.example.bm_mobile.data.api.dto.MagazynDto
import com.example.bm_mobile.data.api.dto.NaprawyDto
import com.example.bm_mobile.data.api.dto.PojazdDto
import com.example.bm_mobile.data.api.dto.PrzegladDto
import com.example.bm_mobile.data.api.dto.ProduktDetailDto
import com.example.bm_mobile.data.api.dto.ProduktDto
import com.example.bm_mobile.data.api.dto.SerwisDetailDto
import com.example.bm_mobile.data.api.dto.SerwisDto
import com.example.bm_mobile.data.api.dto.SerwisyResponse
import com.example.bm_mobile.data.api.dto.WyjazdDto
import com.example.bm_mobile.data.api.dto.WyjazdSzczegolyDto
import com.example.bm_mobile.data.api.dto.ZamowienieDetailDto
import com.example.bm_mobile.data.api.dto.ZamowienieDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/jsonlogin")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ── Magazyny ──────────────────────────────────────────────────────────────

    @GET("api/magazyny")
    suspend fun getMagazyny(): List<MagazynDto>

    @GET("api/magazyn/{id}/produkty")
    suspend fun getProdukty(@Path("id") magazynId: Int): List<ProduktDto>

    @GET("api/magazyn/{id}/dokumenty")
    suspend fun getDokumenty(
        @Path("id") magazynId: Int,
        @Query("limit") limit: Int = 50
    ): List<DokumentDto>

    @GET("api/magazyn/{id}/inwentaryzacje")
    suspend fun getInwentaryzacje(@Path("id") magazynId: Int): List<InwentaryzacjaDto>

    // ── Detale ────────────────────────────────────────────────────────────────

    @GET("api/dokument/{id}")
    suspend fun getDokumentDetail(@Path("id") id: Int): DokumentDetailDto

    @GET("api/produkt/{id}")
    suspend fun getProduktDetail(@Path("id") id: Int): ProduktDetailDto

    @GET("api/serwis/{id}")
    suspend fun getSerwisDetail(@Path("id") id: Int): SerwisDetailDto

    @GET("api/inwentaryzacja/{id}")
    suspend fun getInwentaryzacjaDetail(@Path("id") id: Int): InwentaryzacjaDetailDto

    @GET("api/zamowienie/{id}")
    suspend fun getZamowienieDetail(@Path("id") id: Int): ZamowienieDetailDto

    // ── Pojazdy ───────────────────────────────────────────────────────────────

    @GET("api/pojazdy")
    suspend fun getPojazdy(): List<PojazdDto>

    @GET("api/pojazd/{id}/serwisy")
    suspend fun getPojazdSerwisy(@Path("id") pojazdId: Int): List<SerwisDto>

    @GET("api/pojazd/{id}/przeglady")
    suspend fun getPrzeglady(@Path("id") pojazdId: Int): List<PrzegladDto>

    // ── Zasoby globalne ───────────────────────────────────────────────────────

    @GET("api/serwisy")
    suspend fun getSerwisyGlobalne(): SerwisyResponse

    @GET("api/harmonogram")
    suspend fun getHarmonogram(): HarmonogramResponse

    @GET("api/wyjazdy")
    suspend fun getWyjazdy(): List<WyjazdDto>

    @GET("api/wyjazd/{id}")
    suspend fun getWyjazdSzczegoly(@Path("id") id: Int): WyjazdSzczegolyDto

    @GET("api/naprawy")
    suspend fun getNaprawy(): List<NaprawyDto>

    @GET("api/zamowienia")
    suspend fun getZamowienia(): List<ZamowienieDto>

    // ── NFC ───────────────────────────────────────────────────────────────────

    @GET("api/nfc/{tagId}")
    suspend fun nfcLookup(@Path("tagId") tagId: String): NfcLookupDto

    @PUT("api/produkt/{id}/nfc")
    suspend fun nfcAssign(@Path("id") id: Int, @Body request: NfcAssignRequest)

    @DELETE("api/produkt/{id}/nfc")
    suspend fun nfcRemove(@Path("id") id: Int)
}
