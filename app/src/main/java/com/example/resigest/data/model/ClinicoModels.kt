package com.example.resigest.data.model

data class LoginRequest(val username: String, val password: String)
data class UsuarioSesion(val id: Int, val nombreCompleto: String, val categoria: String, val rol: String)
data class LoginResponse(val token: String, val usuario: UsuarioSesion)
data class ResidenteItem(
    val id: Int,
    val planta_id: Int,
    val habitacion: String,
    val cama: String,
    val nombre: String,
    val apellidos: String,
    val nhc: String,
    val dieta: String,
    val alergias: String,
    val aislamiento: String,
    val riesgo_caidas: String,
    val estado_cama: String
)
data class RegistroConstantesDTO(
    val residente_id: Int,
    val ta_sistolica: Int?,
    val ta_diastolica: Int?,
    val frecuencia_cardiaca: Int?,
    val saturacion_o2: Int?,
    val temperatura: Double?,
    val glucemia: Int?,
    val diuresis_volumen_ml: Int?,
    val observaciones: String?
)
data class RegistroCuidadoDTO(
    val residente_id: Int,
    val tipo_cuidado_id: Int,
    val valor_resultado: String,
    val observaciones: String?
)
data class HistorialItem(
    val id: Int,
    val fecha_hora: String,
    val tipo_registro: String,
    val titulo: String,
    val resultado: String,
    val observaciones: String?,
    val profesional: String
)