package com.example.resigest.data.api

import com.example.resigest.data.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ResiGestService {
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/residentes/planta/{plantaId}")
    suspend fun getResidentesPorPlanta(
        @Header("Authorization") token: String,
        @Path("plantaId") plantaId: Int
    ): Response<List<ResidenteItem>>

    @POST("api/registros/cuidados")
    suspend fun postCuidado(
        @Header("Authorization") token: String,
        @Body body: RegistroCuidadoDTO
    ): Response<Map<String, Any>>

    @POST("api/registros/constantes")
    suspend fun postConstantes(
        @Header("Authorization") token: String,
        @Body body: RegistroConstantesDTO
    ): Response<Map<String, Any>>

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