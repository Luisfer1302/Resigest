package com.example.resigest.data.api

import com.example.resigest.data.model.HistorialItem
import com.example.resigest.data.model.LoginRequest
import com.example.resigest.data.model.LoginResponse
import com.example.resigest.data.model.MedicacionItem
import com.example.resigest.data.model.RegistroConstantesDTO
import com.example.resigest.data.model.RegistroCuidadoDTO
import com.example.resigest.data.model.RegistroEliminacionDTO
import com.example.resigest.data.model.RegistroIngestaDTO
import com.example.resigest.data.model.RegistroMedicacionDTO
import com.example.resigest.data.model.ResidenteItem
import com.example.resigest.data.model.SimpleResponse
import com.example.resigest.data.model.FirmaTomaDTO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ResiGestService {

    @POST("api/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @GET("api/residentes/planta/{plantaId}")
    suspend fun getResidentesPorPlanta(
        @Header("Authorization") token: String,
        @Path("plantaId") plantaId: Int
    ): Response<List<ResidenteItem>>

    @GET("api/residentes/{id}/medicacion")
    suspend fun getMedicacion(
        @Header("Authorization") token: String,
        @Path("id") residenteId: Int,
        @Query("fecha") fecha: String? = null
    ): Response<List<MedicacionItem>>

    @POST("api/registros/medicacion")
    suspend fun postAdministrarMedicacion(
        @Header("Authorization") token: String,
        @Body body: RegistroMedicacionDTO
    ): Response<SimpleResponse>

    @POST("api/registros/medicacion/firmar")
    suspend fun postFirmarToma(
        @Header("Authorization") token: String,
        @Body body: FirmaTomaDTO
    ): Response<SimpleResponse>

    @POST("api/registros/eliminacion")
    suspend fun postEliminacion(
        @Header("Authorization") token: String,
        @Body body: RegistroEliminacionDTO
    ): Response<SimpleResponse>

    @POST("api/registros/ingestas")
    suspend fun postIngesta(
        @Header("Authorization") token: String,
        @Body body: RegistroIngestaDTO
    ): Response<SimpleResponse>

    @POST("api/registros/cuidados")
    suspend fun postCuidado(
        @Header("Authorization") token: String,
        @Body body: RegistroCuidadoDTO
    ): Response<SimpleResponse>

    @POST("api/registros/constantes")
    suspend fun postConstantes(
        @Header("Authorization") token: String,
        @Body body: RegistroConstantesDTO
    ): Response<SimpleResponse>

    @GET("api/residentes/{id}/historial")
    suspend fun getHistorial(
        @Header("Authorization") token: String,
        @Path("id") residenteId: Int
    ): Response<List<HistorialItem>>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:3000/"

        fun create(): ResiGestService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ResiGestService::class.java)
        }
    }
}