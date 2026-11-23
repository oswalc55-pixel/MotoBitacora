# MotoBitácora

Bitácora de mantenimiento y gastos de una motocicleta. Permite registrar servicios, combustible, piezas y kilometraje, y está protegida con **huella dactilar** para que nadie altere el historial.

Proyecto libre: app Android nativa con **Kotlin** y **ViewBinding**.

## Funciones

- **Login con huella** (primera pantalla) con `BiometricPrompt`.
- **Historial**: lista de registros (RecyclerView con adaptador propio).
- **Registro**: formulario con validación para crear o editar.
- **Resumen**: gasto total, gasto por tipo, kilometraje y último servicio.
- Eliminar con diálogo de confirmación.
- Menú con Resumen, cambio de tema claro/oscuro y cierre de sesión.

## Manejo del login con huella

| Caso | Qué hace la app |
| --- | --- |
| Dispositivo sin sensor | Mensaje claro; no se puede entrar |
| Sin huella registrada | Mensaje + botón que abre Ajustes para registrar una |
| Huella no reconocida | Mensaje "Huella no reconocida" y se puede reintentar |
| Cancelación | Mensaje y botón para volver a intentar |
| Bloqueo por intentos fallidos | Temporal: botón deshabilitado con cuenta regresiva de 30 s. Permanente: mensaje para desbloquear con PIN/patrón |

**Acceso protegido:** el login se cierra con `finish()` al autenticarse, y al cerrar sesión se abre con `CLEAR_TASK`, por lo que el botón atrás nunca da acceso sin huella. Además, si la app pasa a segundo plano se vuelve a bloquear.

## Cumplimiento de la rúbrica

| Requisito | Dónde |
| --- | --- |
| Kotlin | Todo el código en `app/src/main/java` |
| ViewBinding | `buildFeatures { viewBinding = true }` en `app/build.gradle`; sin `findViewById` |
| Login con huella primero | `ui/login/LoginActivity.kt` (actividad launcher) |
| minSdk 24 | `app/build.gradle` (justificación en comentario) |
| 3 pantallas + login | Historial, Registro, Resumen |
| Navegación | Navigation Component (`res/navigation/nav_graph.xml`) |

Elementos del apartado 4 (se cubren 7 de 9):

1. RecyclerView con adaptador propio (`RegistroAdapter`)
2. Formularios con validación (`FormularioFragment`)
3. CRUD con SQLite (`DbHelper`)
4. Fragments + Navigation Component
5. Menú y diálogos
6. Modo oscuro / tema personalizado
7. Cierre de sesión que regresa al login

## Estructura

```
com.example.motobitacora
├── MotoApp.kt              Aplica el tema guardado al abrir
├── data/                   Registro (modelo) y DbHelper (SQLite / CRUD)
├── util/                   Sesion, Ajustes (tema) y Formato (fechas, dinero)
└── ui/
    ├── login/              LoginActivity
    ├── main/               MainActivity (contenedor de fragments)
    ├── historial/          HistorialFragment y RegistroAdapter
    ├── formulario/         FormularioFragment
    └── resumen/            ResumenFragment
```

## Dependencias

| Librería | Versión | Propósito |
| --- | --- | --- |
| androidx.core:core-ktx | 1.12.0 | Extensiones de Kotlin |
| androidx.appcompat:appcompat | 1.6.1 | Compatibilidad y tema día/noche |
| com.google.android.material:material | 1.11.0 | Componentes Material Design |
| androidx.recyclerview:recyclerview | 1.3.2 | Lista de registros |
| androidx.biometric:biometric | 1.1.0 | Login con huella |
| androidx.navigation:navigation-fragment-ktx / ui-ktx | 2.7.7 | Navegación entre pantallas |

## Cómo compilar y ejecutar

**Requisitos:** Android Studio (Hedgehog o posterior), JDK 17 y un dispositivo o emulador con huella configurada.

1. Clona el repositorio y ábrelo con *File > Open* en Android Studio.
2. Espera a que termine la sincronización de Gradle.
3. Ejecuta en un dispositivo real o emulador.
   - En el emulador: *Settings > Security > Fingerprint* para registrar una huella, y usa *Extended controls > Fingerprint* para simular el toque.
4. APK de depuración: *Build > Build Bundle(s) / APK(s) > Build APK(s)*.

## Capturas

_Agregar aquí las capturas de Login, Historial, Registro y Resumen._
