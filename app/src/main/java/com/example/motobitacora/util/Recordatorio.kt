package com.example.motobitacora.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.motobitacora.R
import com.example.motobitacora.data.DbHelper
import com.example.motobitacora.ui.main.MainActivity

/** Muestra una notificación cuando se acerca (o se pasó) el próximo servicio. */
object Recordatorio {
    private const val CANAL_ID = "recordatorio_servicio"
    private const val NOTIFICACION_ID = 1
    private const val UMBRAL_KM = 300

    @SuppressLint("MissingPermission") // el permiso se revisa más abajo
    fun revisar(context: Context) {
        if (!Ajustes.recordatoriosActivados(context)) return
        val intervalo = Ajustes.intervaloServicio(context)
        if (intervalo <= 0) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val registros = DbHelper.obtener(context).obtenerTodos()
        val tipoServicio = context.resources.getStringArray(R.array.tipos_registro)[0]
        val estado = ProximoServicio.calcular(registros, intervalo, tipoServicio) ?: return
        if (estado.faltan > UMBRAL_KM) return

        val texto = if (estado.faltan >= 0) {
            context.getString(R.string.notif_cerca, Formato.numero(estado.faltan), Formato.numero(estado.proximoKm))
        } else {
            context.getString(R.string.notif_vencido, Formato.numero(-estado.faltan))
        }

        crearCanal(context)

        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendiente = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(context.getString(R.string.notif_titulo))
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICACION_ID, notificacion)
    }

    private fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                context.getString(R.string.notif_canal_nombre),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_canal_descripcion) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }
}