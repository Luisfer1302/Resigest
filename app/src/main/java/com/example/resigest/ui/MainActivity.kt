package com.example.resigest.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resigest.data.api.ResiGestService
import com.example.resigest.data.model.RegistroConstantesDTO
import com.example.resigest.data.model.RegistroCuidadoDTO
import com.example.resigest.data.model.ResidenteItem
import com.example.resigest.databinding.ActivityMainBinding
import com.example.resigest.ui.adapter.HistorialAdapter
import com.example.resigest.ui.adapter.ResidenteAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val api = ResiGestService.create()
    private lateinit var adapterCenso: ResidenteAdapter
    private lateinit var adapterHistorial: HistorialAdapter

    private var token: String = ""
    private var plantaActual: Int = 1
    private var residenteSeleccionado: ResidenteItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("resigest_prefs", Context.MODE_PRIVATE)
        token = prefs.getString("token", "") ?: ""
        val usuario = prefs.getString("usuario_nombre", "Personal Sanitario")
        val categoria = prefs.getString("usuario_categoria", "TCAE")
        binding.tvUsuarioSesion.text = "$usuario ($categoria)"

        setupUI()
        setupListeners()
        cargarPlanta(plantaActual)
    }

    private fun setupUI() {
        adapterCenso = ResidenteAdapter { seleccionado ->
            mostrarFichaResidente(seleccionado)
        }
        binding.rvCenso.layoutManager = LinearLayoutManager(this)
        binding.rvCenso.adapter = adapterCenso

        adapterHistorial = HistorialAdapter()
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = adapterHistorial

        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDark
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupListeners() {
        binding.tabsPlantas.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                plantaActual = (tab?.position ?: 0) + 1
                cargarPlanta(plantaActual)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.tabsSecciones.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.panelCuidados.visibility = View.VISIBLE
                        binding.panelConstantes.visibility = View.GONE
                        binding.panelHistorial.visibility = View.GONE
                    }
                    1 -> {
                        binding.panelCuidados.visibility = View.GONE
                        binding.panelConstantes.visibility = View.VISIBLE
                        binding.panelHistorial.visibility = View.GONE
                    }
                    2 -> {
                        binding.panelCuidados.visibility = View.GONE
                        binding.panelConstantes.visibility = View.GONE
                        binding.panelHistorial.visibility = View.VISIBLE
                        cargarHistorial()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Cuidados posturales
        binding.btnDecubitoSupino.setOnClickListener { registrarCuidadoDirecto(4, "Decúbito Supino") }
        binding.btnDecubitoDcho.setOnClickListener { registrarCuidadoDirecto(5, "Decúbito Lateral Dcho") }
        binding.btnDecubitoIzq.setOnClickListener { registrarCuidadoDirecto(6, "Decúbito Lateral Izq") }
        binding.btnSillon.setOnClickListener { registrarCuidadoDirecto(7, "Sedestación en Sillón") }

        // Ingestas
        binding.btnIngesta100.setOnClickListener { registrarCuidadoDirecto(9, "Comida: 100%") }
        binding.btnIngesta50.setOnClickListener { registrarCuidadoDirecto(9, "Comida: 50%") }
        binding.btnIngestaRechazo.setOnClickListener { registrarCuidadoDirecto(9, "Comida: Rechazo (<25%)") }

        // Higiene y eliminación
        binding.btnAseoCompleto.setOnClickListener { registrarCuidadoDirecto(1, "Aseo Completo") }
        binding.btnPanal.setOnClickListener { registrarCuidadoDirecto(3, "Cambio de Pañal") }
        binding.btnDeposicion.setOnClickListener { registrarCuidadoDirecto(13, "Deposición (+)") }

        binding.btnGuardarConstantes.setOnClickListener {
            guardarConstantes()
        }
    }

    private fun cargarPlanta(planta: Int) {
        lifecycleScope.launch {
            try {
                val resp = api.getResidentesPorPlanta(token, planta)
                if (resp.isSuccessful && resp.body() != null) {
                    val lista = resp.body()!!
                    binding.tvTituloPlanta.text = "Censo - Planta $planta (${lista.size} Pacientes)"
                    binding.tvContadorOcupacion.text = "${lista.size}/30 Ocupadas"
                    adapterCenso.setData(lista)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error censo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarFichaResidente(res: ResidenteItem) {
        this.residenteSeleccionado = res
        binding.tvCabeceraCama.text = "HAB: ${res.habitacion} - ${res.cama}"
        binding.tvCabeceraNombre.text = "${res.apellidos}, ${res.nombre}"
        binding.tvCabeceraNHC.text = "NHC: ${res.nhc}"

        binding.chipAlergia.text = "ALERGIAS: ${res.alergias}"
        binding.chipDieta.text = "DIETA: ${res.dieta}"
        binding.chipCaidas.text = "R. CAÍDAS: ${res.riesgo_caidas}"

        if (binding.panelHistorial.visibility == View.VISIBLE) {
            cargarHistorial()
        }
    }

    private fun registrarCuidadoDirecto(tipoId: Int, valor: String) {
        val res = residenteSeleccionado ?: return
        val obs = binding.etObservacionesCuidados.text.toString().trim()

        lifecycleScope.launch {
            try {
                val dto = RegistroCuidadoDTO(
                    residente_id = res.id,
                    tipo_cuidado_id = tipoId,
                    valor_resultado = valor,
                    observaciones = if (obs.isNotEmpty()) obs else null
                )
                val resp = api.postCuidado(token, dto)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MainActivity, "✓ $valor anotado en hoja", Toast.LENGTH_SHORT).show()
                    binding.etObservacionesCuidados.text?.clear()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarConstantes() {
        val res = residenteSeleccionado ?: return
        val sis = binding.etTASistolica.text.toString().toIntOrNull()
        val dia = binding.etTADiastolica.text.toString().toIntOrNull()
        val fc = binding.etFC.text.toString().toIntOrNull()
        val sat = binding.etSatO2.text.toString().toIntOrNull()
        val temp = binding.etTemperatura.text.toString().toDoubleOrNull()

        lifecycleScope.launch {
            try {
                val dto = RegistroConstantesDTO(
                    residente_id = res.id,
                    ta_sistolica = sis,
                    ta_diastolica = dia,
                    frecuencia_cardiaca = fc,
                    saturacion_o2 = sat,
                    temperatura = temp,
                    glucemia = null,
                    diuresis_volumen_ml = null,
                    observaciones = null
                )
                val resp = api.postConstantes(token, dto)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Constantes registradas con éxito", Toast.LENGTH_SHORT).show()
                    binding.etTASistolica.text?.clear()
                    binding.etTADiastolica.text?.clear()
                    binding.etFC.text?.clear()
                    binding.etSatO2.text?.clear()
                    binding.etTemperatura.text?.clear()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error en constantes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarHistorial() {
        val res = residenteSeleccionado ?: return
        lifecycleScope.launch {
            try {
                val resp = api.getHistorial(token, res.id)
                if (resp.isSuccessful && resp.body() != null) {
                    adapterHistorial.setData(resp.body()!!)
                }
            } catch (_: Exception) { }
        }
    }
}