package com.example.resigest.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.resigest.data.api.ResiGestService
import com.example.resigest.data.model.LoginRequest
import com.example.resigest.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val api = ResiGestService.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Debe introducir usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ejecutarLogin(user, pass)
        }
    }

    private fun ejecutarLogin(user: String, pass: String) {
        binding.pbCargando.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val resp = api.login(LoginRequest(user, pass))
                binding.pbCargando.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    val prefs = getSharedPreferences("resigest_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("token", "Bearer " + body.token)
                        .putString("usuario_nombre", body.usuario.nombreCompleto)
                        .putString("usuario_categoria", body.usuario.categoria)
                        .apply()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val codigoHttp = resp.code()
                    val errorCuerpo = resp.errorBody()?.string() ?: "Sin cuerpo de error"
                    Toast.makeText(this@LoginActivity, "Error $codigoHttp: $errorCuerpo", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.pbCargando.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}