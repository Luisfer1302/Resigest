package com.example.resigest.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resigest.data.api.ResiGestService
import com.example.resigest.data.model.*
import com.example.resigest.databinding.ActivityMainBinding
import com.example.resigest.ui.adapter.HistorialAdapter
import com.example.resigest.ui.adapter.MedicacionAdapter
import com.example.resigest.ui.adapter.ResidenteAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val api = ResiGestService.create()

    private lateinit var adapterCenso: ResidenteAdapter
    private lateinit var adapterHistorial: HistorialAdapter
    private lateinit var adapterMedicacion: MedicacionAdapter

    private var token: String = ""
    private var plantaActual: Int = 1
    private var residenteSeleccionado: ResidenteItem? = null

    // Estados seleccionados para Eliminación
    private var selTipoElim: String = "Solo Diuresis"
    private var selCantElim: String = "Moderada"
    private var selCaracElim: String = "Normal / Formada"

    // Estados seleccionados para Ingestas
    private var selTomaComida: String = "Comida"
    private var selAceptacionIngesta: String = "Buena (100%)"

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

        adapterMedicacion = MedicacionAdapter { med, toma ->
            firmarTomaHoraria(med, toma)
        }
        binding.rvMedicacion.layoutManager = LinearLayoutManager(this)
        binding.rvMedicacion.adapter = adapterMedicacion

        val isDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDark
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
                delegate.localNightMode = if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
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
                binding.panelMedicacion.visibility = if (tab?.position == 0) View.VISIBLE else View.GONE
                binding.panelEliminacion.visibility = if (tab?.position == 1) View.VISIBLE else View.GONE
                binding.panelIngestas.visibility = if (tab?.position == 2) View.VISIBLE else View.GONE
                binding.panelConstantes.visibility = if (tab?.position == 3) View.VISIBLE else View.GONE
                binding.panelHistorial.visibility = if (tab?.position == 4) View.VISIBLE else View.GONE

                if (tab?.position == 0) cargarMedicacion()
                if (tab?.position == 4) cargarHistorial()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Selección de botones para eliminación
        binding.btnElimDiuresis.setOnClickListener { highlightButton(binding.btnElimDiuresis, listOf(binding.btnElimDeposicion, binding.btnElimMixto)); selTipoElim = "Solo Diuresis" }
        binding.btnElimDeposicion.setOnClickListener { highlightButton(binding.btnElimDeposicion, listOf(binding.btnElimDiuresis, binding.btnElimMixto)); selTipoElim = "Solo Deposición" }
        binding.btnElimMixto.setOnClickListener { highlightButton(binding.btnElimMixto, listOf(binding.btnElimDiuresis, binding.btnElimDeposicion)); selTipoElim = "Mixto" }

        binding.btnCantEscasa.setOnClickListener { highlightButton(binding.btnCantEscasa, listOf(binding.btnCantNormal, binding.btnCantAbundante)); selCantElim = "Escasa" }
        binding.btnCantNormal.setOnClickListener { highlightButton(binding.btnCantNormal, listOf(binding.btnCantEscasa, binding.btnCantAbundante)); selCantElim = "Moderada" }
        binding.btnCantAbundante.setOnClickListener { highlightButton(binding.btnCantAbundante, listOf(binding.btnCantEscasa, binding.btnCantNormal)); selCantElim = "Abundante" }

        binding.btnCaracNormal.setOnClickListener { highlightButton(binding.btnCaracNormal, listOf(binding.btnCaracPastosa, binding.btnCaracLiquida, binding.btnCaracDura)); selCaracElim = "Normal / Formada" }
        binding.btnCaracPastosa.setOnClickListener { highlightButton(binding.btnCaracPastosa, listOf(binding.btnCaracNormal, binding.btnCaracLiquida, binding.btnCaracDura)); selCaracElim = "Blanda / Pastosa" }
        binding.btnCaracLiquida.setOnClickListener { highlightButton(binding.btnCaracLiquida, listOf(binding.btnCaracNormal, binding.btnCaracPastosa, binding.btnCaracDura)); selCaracElim = "Líquida / Diarrea" }
        binding.btnCaracDura.setOnClickListener { highlightButton(binding.btnCaracDura, listOf(binding.btnCaracNormal, binding.btnCaracPastosa, binding.btnCaracLiquida)); selCaracElim = "Dura / Caprina" }

        binding.btnGuardarEliminacion.setOnClickListener { guardarEliminacion() }

        // Selección de botones para ingestas
        binding.btnTomaDesayuno.setOnClickListener { highlightButton(binding.btnTomaDesayuno, listOf(binding.btnTomaComida, binding.btnTomaMerienda, binding.btnTomaCena)); selTomaComida = "Desayuno" }
        binding.btnTomaComida.setOnClickListener { highlightButton(binding.btnTomaComida, listOf(binding.btnTomaDesayuno, binding.btnTomaMerienda, binding.btnTomaCena)); selTomaComida = "Comida" }
        binding.btnTomaMerienda.setOnClickListener { highlightButton(binding.btnTomaMerienda, listOf(binding.btnTomaDesayuno, binding.btnTomaComida, binding.btnTomaCena)); selTomaComida = "Merienda" }
        binding.btnTomaCena.setOnClickListener { highlightButton(binding.btnTomaCena, listOf(binding.btnTomaDesayuno, binding.btnTomaComida, binding.btnTomaMerienda)); selTomaComida = "Cena" }

        binding.btnIngestaBuena.setOnClickListener { highlightButton(binding.btnIngestaBuena, listOf(binding.btnIngestaMedia, binding.btnIngestaEscasa, binding.btnIngestaRechazoTotal)); selAceptacionIngesta = "Buena (100%)" }
        binding.btnIngestaMedia.setOnClickListener { highlightButton(binding.btnIngestaMedia, listOf(binding.btnIngestaBuena, binding.btnIngestaEscasa, binding.btnIngestaRechazoTotal)); selAceptacionIngesta = "Regular (50%)" }
        binding.btnIngestaEscasa.setOnClickListener { highlightButton(binding.btnIngestaEscasa, listOf(binding.btnIngestaBuena, binding.btnIngestaMedia, binding.btnIngestaRechazoTotal)); selAceptacionIngesta = "Escasa (<25%)" }
        binding.btnIngestaRechazoTotal.setOnClickListener { highlightButton(binding.btnIngestaRechazoTotal, listOf(binding.btnIngestaBuena, binding.btnIngestaMedia, binding.btnIngestaEscasa)); selAceptacionIngesta = "Rechazo Total" }

        binding.btnGuardarIngesta.setOnClickListener { guardarIngesta() }
        binding.btnGuardarConstantes.setOnClickListener { guardarConstantes() }
    }

    private fun highlightButton(selected: Button, others: List<Button>) {
        selected.setBackgroundColor(Color.parseColor("#0B4F8A"))
        selected.setTextColor(Color.WHITE)
        others.forEach {
            it.setBackgroundColor(Color.TRANSPARENT)
            it.setTextColor(Color.parseColor("#0B4F8A"))
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

        // Alertas de alta visibilidad
        binding.chipAlergia.text = "⚠ ALERGIAS: ${res.alergias.uppercase()}"
        binding.chipDieta.text = "🥗 DIETA: ${res.dieta.uppercase()}"
        binding.chipCaidas.text = "🦽 R. CAÍDAS: ${res.riesgo_caidas.uppercase()}"

        cargarMedicacion()
        if (binding.panelHistorial.visibility == View.VISIBLE) {
            cargarHistorial()
        }
    }

    private fun cargarMedicacion() {
        val res = residenteSeleccionado ?: return
        lifecycleScope.launch {
            try {
                val resp = api.getMedicacion(token, res.id)
                if (resp.isSuccessful && resp.body() != null) {
                    adapterMedicacion.setData(resp.body()!!)
                } else {
                    Toast.makeText(this@MainActivity, "Error servidor: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Ahora si hay un fallo de lectura, la tablet te lo dirá en pantalla en vez de quedarse vacía
                Toast.makeText(this@MainActivity, "Fallo al leer pautas: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun administrarFarmaco(med: MedicacionItem) {
        val res = residenteSeleccionado ?: return
        lifecycleScope.launch {
            try {
                val dto = RegistroMedicacionDTO(
                    pauta_id = med.id,
                    residente_id = res.id,
                    estado = "ADMINISTRADO"
                )
                val resp = api.postAdministrarMedicacion(token, dto)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MainActivity, "✓ ${med.farmaco} administrado y firmado", Toast.LENGTH_SHORT).show()
                    cargarMedicacion()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEliminacion() {
        val res = residenteSeleccionado ?: return
        val obs = binding.etObsEliminacion.text.toString().trim()

        lifecycleScope.launch {
            try {
                val dto = RegistroEliminacionDTO(
                    residente_id = res.id,
                    tipo = selTipoElim,
                    volumen_cantidad = selCantElim,
                    caracteristicas = selCaracElim,
                    observaciones = if (obs.isNotEmpty()) obs else null
                )
                val resp = api.postEliminacion(token, dto)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MainActivity, "✓ Eliminación registrada en hoja clínica", Toast.LENGTH_SHORT).show()
                    binding.etObsEliminacion.text?.clear()
                    cargarHistorial()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al registrar eliminación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarIngesta() {
        val res = residenteSeleccionado ?: return
        val liquidos = binding.etLiquidosMl.text.toString().toIntOrNull()

        lifecycleScope.launch {
            try {
                val dto = RegistroIngestaDTO(
                    residente_id = res.id,
                    comida = selTomaComida,
                    valoracion = selAceptacionIngesta,
                    liquidos_ml = liquidos,
                    observaciones = null
                )
                val resp = api.postIngesta(token, dto)
                if (resp.isSuccessful) {
                    Toast.makeText(this@MainActivity, "✓ Ingesta de $selTomaComida anotada", Toast.LENGTH_SHORT).show()
                    binding.etLiquidosMl.text?.clear()
                    cargarHistorial()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error en ingesta: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@MainActivity, "Constantes guardadas correctamente", Toast.LENGTH_SHORT).show()
                    binding.etTASistolica.text?.clear()
                    binding.etTADiastolica.text?.clear()
                    binding.etFC.text?.clear()
                    binding.etSatO2.text?.clear()
                    binding.etTemperatura.text?.clear()
                    cargarHistorial()
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
            } catch (_: Exception) {}
        }
    }

    private fun firmarTomaHoraria(med: MedicacionItem, toma: String) {
        val res = residenteSeleccionado ?: return
        lifecycleScope.launch {
            try {
                val dto = FirmaTomaDTO(
                    pauta_id = med.id,
                    residente_id = res.id,
                    toma = toma
                )
                val resp = api.postFirmarToma(token, dto)
                if (resp.isSuccessful) {
                    val respuestaTexto = resp.body()?.mensaje ?: "Operación completada"
                    Toast.makeText(this@MainActivity, "✓ $respuestaTexto", Toast.LENGTH_SHORT).show()

                    // RECARGAR la sábana para que el cuadrado cambie a verde (firmado) o gris (anulado)
                    cargarMedicacion()
                } else {
                    Toast.makeText(this@MainActivity, "No se pudo actualizar la toma", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}