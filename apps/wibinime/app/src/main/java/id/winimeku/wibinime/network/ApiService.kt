package id.winimeku.wibinime.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- MODEL AUTH ---
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val message: String, val token: String? = null, val error: String? = null)

// --- MODEL ANIME & EPISODE ---
data class Episode(
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val title: String?,
    val videoUrl: String
)

data class Anime(
    val id: String,
    val title: String,
    val synopsis: String?,
    val coverImage: String?,
    val status: String?,
    val releaseYear: Int?,
    val episodes: List<Episode>? = null // Tambahan untuk Detail
)

data class AnimeListResponse(
    val data: List<Anime>,
    val meta: MetaData
)
data class MetaData(
    val page: Int,
    val total: Int
)

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/v1/anime")
    suspend fun getAnimeList(): Response<AnimeListResponse>

    // Endpoint Baru: Ambil Detail Anime berdasarkan ID
    @GET("api/v1/anime/{id}")
    suspend fun getAnimeDetail(@Path("id") id: String): Response<Anime>
}

object ApiClient {
    // GANTI DENGAN IP LOKAL ANDA ATAU URL CLOUD
    private const val BASE_URL = "http://192.168.1.10:3000/" 
    
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
