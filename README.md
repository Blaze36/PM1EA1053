# PM1EA1053 · Agenda de Contactos

**Examen Primer Parcial · Programación Móvil I (PMO-0602) · Versión A**
Universidad Tecnológica de Honduras (UTH) · Periodo III / 2026
Catedrático: Ing. Ricardo Enrique Lagos Mendoza

| | |
|---|---|
| **Estudiante** | **[COMPLETAR: nombre completo]** |
| **Cuenta** | 202310011053 |
| **Modalidad** | Individual |

Aplicación Android (Java) que permite registrar contactos con su fotografía, consultarlos,
compartirlos y llamarlos. Usa el patrón **MVC + DAO** y una base de datos **SQLite**.

## Pantallas y rúbrica

### Pantalla 1 · Registro (`RegistroActivity`)
| Requisito | Dónde está |
|---|---|
| Guardar el contacto (con la ruta de su foto) en SQLite | `ContactoController.guardar()` → `ContactoDAO.insertar()` |
| Clase modelo `Contacto` (id, país, nombre, teléfono, nota, imagen) | `model/Contacto.java` |
| Alertas si un campo obligatorio está vacío | `RegistroActivity.mostrarErrores()` (AlertDialog + error en el campo) |
| Regex: nombre solo letras y teléfono de 8 dígitos | `ContactoController.PATRON_NOMBRE` y `PATRON_TELEFONO` |

Campos obligatorios: país, nombre, teléfono y fotografía. La nota es opcional.
La foto se toma con la cámara o se elige de la galería; se copia al almacenamiento interno
de la app (`files/fotos/`) y en la base de datos solo se guarda su **ruta**.

### Pantalla 2 · Lista (`ListaActivity`)
Al tocar un contacto se abre un menú con las opciones:

| Requisito | Dónde está |
|---|---|
| Listar contactos desde la base de datos | `ContactoDAO.listar()` + `ContactoAdapter` |
| Eliminar el contacto seleccionado | `ListaActivity.confirmarEliminar()` → `ContactoDAO.eliminar()` |
| Actualizar el contacto seleccionado | abre `RegistroActivity` en modo edición → `ContactoDAO.actualizar()` |
| Compartir entre aplicaciones (`Intent.ACTION_SEND`) | `ListaActivity.compartir()` (texto + imagen vía `FileProvider`) |
| Ver la imagen guardada | `ListaActivity.verImagen()` |

### Pantalla 3 · Llamar (`LlamarActivity`)
| Requisito | Dónde está |
|---|---|
| Marcar con `ACTION_CALL` | `LlamarActivity.realizarLlamada()` |
| Solicitar `CALL_PHONE` en tiempo de ejecución | `LlamarActivity.verificarPermiso()` (+ declarado en el `AndroidManifest.xml`) |
| Confirmar con `AlertDialog.Builder` («¿Desea llamar a …?») | `LlamarActivity.confirmarLlamada()` |

## Estructura (MVC + DAO)

```
app/src/main/java/com/uth/pm1ea1053/
├── model/        Contacto.java
├── dao/          DbHelper.java, ContactoDAO.java
├── controller/   ContactoController.java
├── util/         ImageUtils.java
└── view/         RegistroActivity, ListaActivity, LlamarActivity, ContactoAdapter
```

## Cómo ejecutarla
1. Abrir la carpeta del proyecto con **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar en un dispositivo real o emulador (minSdk 24).
3. Para probar la llamada conviene un dispositivo físico con SIM.
