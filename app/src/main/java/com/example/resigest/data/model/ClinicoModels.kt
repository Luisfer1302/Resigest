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

data class MedicacionItem(
    val id: Int,
    val residente_id: Int,
    val farmaco: String,
    val dosis: String,
    val via: String,
    val observaciones: String?,
    // Tomas pautadas (true = pautada en ese horario)
    val pauta_desayuno: Boolean = true,
    val pauta_comida: Boolean = false,
    val pauta_merienda: Boolean = false,
    val pauta_cena: Boolean = false,
    val pauta_noche: Boolean = false,
    // Estado de firma en el turno
    var firmado_desayuno: Boolean = false,
    var firmado_comida: Boolean = false,
    var firmado_merienda: Boolean = false,
    var firmado_cena: Boolean = false,
    var firmado_noche: Boolean = false
)

data class FirmaTomaDTO(
    val pauta_id: Int,
    val residente_id: Int,
    val toma: String // "DES", "COM", "MER", "CEN", "NOC"
)

data class RegistroMedicacionDTO(
    val pauta_id: Int,
    val residente_id: Int,
    val estado: String,
    val motivo_rechazo: String? = null
)

data class RegistroEliminacionDTO(
    val residente_id: Int,
    val tipo: String,             // "Solo Diuresis", "Solo Deposición", "Mixto"
    val volumen_cantidad: String, // "Escasa", "Moderada", "Abundante"
    val caracteristicas: String,  // Bristol / Color
    val observaciones: String?
)

data class RegistroIngestaDTO(
    val residente_id: Int,
    val comida: String,           // "Desayuno", "Comida", "Merienda", "Cena"
    val valoracion: String,       // "Buena (100%)", "Regular (50%)", "Escasa (<25%)", "Rechazo Total"
    val liquidos_ml: Int?,
    val observaciones: String?
)

data class RegistroCuidadoDTO(
    val residente_id: Int,
    val tipo_cuidado_id: Int,
    val valor_resultado: String,
    val observaciones: String?
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

data class HistorialItem(
    val id: Int,
    val fecha_hora: String,
    val tipo_registro: String,
    val titulo: String,
    val resultado: String?,
    val observaciones: String?,
    val profesional: String?
)

data class SimpleResponse(
    val mensaje: String? = null,
    val error: String? = null
)