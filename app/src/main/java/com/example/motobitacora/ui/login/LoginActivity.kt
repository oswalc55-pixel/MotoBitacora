package com.example.motobitacora.ui.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.motobitacora.R
import com.example.motobitacora.databinding.ActivityLoginBinding
import com.example.motobitacora.ui.main.MainActivity
import com.example.motobitacora.util.Sesion

/**
 * Primera pantalla de la app. Pide la huella con BiometricPrompt y contempla:
 * sin sensor, sin huella registrada, huella no reconocida, cancelación y bloqueo.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var temporizador: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estar en el login significa que no hay sesión abierta.
        Sesion.cerrar()

        prepararBiometria()

        binding.btnHuella.setOnClickListener { iniciarAutenticacion() }
        binding.btnConfigurar.setOnClickListener { abrirAjustesDeHuella() }

        // Se pide la huella automáticamente solo al abrir la pantalla (no al rotarla).
        if (savedInstanceState == null) iniciarAutenticacion()
    }

    override fun onResume() {
        super.onResume()
        // Por si el usuario regresó de Ajustes después de registrar una huella.
        val estado = BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG)
        binding.btnConfigurar.visibility =
            if (estado == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) View.VISIBLE else View.GONE
        if (estado == BiometricManager.BIOMETRIC_SUCCESS &&
            binding.tvMensaje.text == getString(R.string.login_sin_huella)
        ) {
            binding.tvMensaje.setText(R.string.login_mensaje_inicial)
        }
    }

    override fun onDestroy() {
        temporizador?.cancel()
        super.onDestroy()
    }

    private fun prepararBiometria() {
        val ejecutor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, ejecutor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Sesion.iniciar()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                // finish() saca el login de la pila: con el botón atrás no se regresa a él.
                finish()
            }

            // La huella se leyó pero no coincide (el diálogo sigue abierto para reintentar).
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                mostrarMensaje(R.string.login_no_reconocida)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> mostrarMensaje(R.string.login_cancelada)

                    BiometricPrompt.ERROR_LOCKOUT -> iniciarBloqueoTemporal()

                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> mostrarMensaje(R.string.login_bloqueo_permanente)

                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        mostrarMensaje(R.string.login_sin_huella)
                        binding.btnConfigurar.visibility = View.VISIBLE
                    }

                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> mostrarMensaje(R.string.login_sin_sensor)

                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> mostrarMensaje(R.string.login_sensor_no_disponible)

                    else -> binding.tvMensaje.text = getString(R.string.login_error_generico, errString)
                }
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_prompt_titulo))
            .setSubtitle(getString(R.string.login_prompt_subtitulo))
            .setNegativeButtonText(getString(R.string.login_prompt_cancelar))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
    }

    /** Revisa el estado del sensor y, si todo está bien, muestra el diálogo de huella. */
    private fun iniciarAutenticacion() {
        when (BiometricManager.from(this).canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(promptInfo)

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> mostrarMensaje(R.string.login_sin_sensor)

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> mostrarMensaje(R.string.login_sensor_no_disponible)

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                mostrarMensaje(R.string.login_sin_huella)
                binding.btnConfigurar.visibility = View.VISIBLE
            }

            else -> mostrarMensaje(R.string.login_no_soportado)
        }
    }

    private fun mostrarMensaje(@StringRes mensaje: Int) {
        binding.tvMensaje.setText(mensaje)
    }

    /** Android bloquea el sensor 30 segundos tras varios intentos fallidos. */
    private fun iniciarBloqueoTemporal() {
        binding.btnHuella.isEnabled = false
        temporizador?.cancel()
        temporizador = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(milisRestantes: Long) {
                binding.tvMensaje.text =
                    getString(R.string.login_bloqueo_temporal, (milisRestantes / 1000).toInt() + 1)
            }

            override fun onFinish() {
                binding.btnHuella.isEnabled = true
                binding.tvMensaje.setText(R.string.login_mensaje_inicial)
            }
        }.start()
    }

    /** Abre la pantalla del sistema para registrar una huella. */
    private fun abrirAjustesDeHuella() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}
