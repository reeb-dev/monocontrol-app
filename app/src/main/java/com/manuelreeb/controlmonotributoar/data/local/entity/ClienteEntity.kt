package com.reeb.controlmonotributoar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * "Preset" de cliente para que un contador pueda guardar varios y cambiar
 * rápido entre ellos. Cada cliente trae nombre, CUIT, categoría forzada y rubro.
 *
 * También guarda la configuración de envío de recordatorios:
 * email personalizado (asunto, cuerpo, adjunto), borrador, programación
 * de envío automático y WhatsApp.
 */
@Entity(tableName = "cliente")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val cuit: String = "",
    /** Letra de la categoría forzada (A..K). null = automática. */
    val categoria: String? = null,
    val esVentaMuebles: Boolean = false,
    /** Email del cliente para enviarle notificaciones de vencimiento */
    val email: String = "",
    val creadoEn: Long = System.currentTimeMillis(),

    // ── Configuración de envío de recordatorios ─────────────────────────
    /** Asunto personalizado. Si está vacío se usa el default. */
    val emailAsunto: String = "",
    /** Cuerpo personalizado del email. Si está vacío se usa el default. */
    val emailMensaje: String = "",
    /** Borrador editado (no enviado todavía). Se preserva entre sesiones. */
    val emailBorrador: String = "",
    /** URI (string) del archivo adjunto extra elegido por el contador (PDF/imagen). */
    val emailAdjuntoUri: String = "",
    /** Nombre legible del adjunto, para mostrar en la UI. */
    val emailAdjuntoNombre: String = "",

    // ── Programación automática ─────────────────────────────────────────
    /** Si está activo, se programa una notificación recurrente para enviar. */
    val envioAutomatico: Boolean = false,
    /** Día del mes (1..28) en que se dispara el recordatorio. Default 18. */
    val envioDiaMes: Int = 18,
    /** Hora (0..23). */
    val envioHora: Int = 9,
    /** Minuto (0..59). */
    val envioMinuto: Int = 0,

    // ── WhatsApp ────────────────────────────────────────────────────────
    /** Número con código de país sin "+" ni espacios (ej: 5491122334455). */
    val whatsappNumero: String = "",
    /** Si está activo, además del email también se abre WhatsApp con el mensaje. */
    val whatsappEnviar: Boolean = false,
    /** Plantilla dedicada para WhatsApp guardada por cliente. */
    val whatsappMensaje: String = "",

    // ── Sincronización Firestore ─────────────────────────────────────────
    /** Timestamp de la última modificación local. Sirve para resolver conflictos. */
    val actualizadoEn: Long = System.currentTimeMillis(),
    /** Si está en true significa que falta sincronizar con Firestore (cambios offline). */
    val syncPendiente: Boolean = true,
    /** Si está en true es porque fue eliminado localmente y hay que borrar también en Firestore. */
    val eliminado: Boolean = false,
    /** UID del usuario dueño (de FirebaseAuth). Vacío si nunca se sincronizó. */
    val ownerUid: String = "",

    // ── Datos adicionales del cliente ──────────────────────────────────
    /** Teléfono del cliente (con o sin código de país). */
    val telefono: String = "",
    /** Estado del cliente: "activo" | "inactivo" | "por_vencer" | "vencido". */
    val estadoCliente: String = "activo",
    /** Notas del contador (texto libre). */
    val notas: String = "",
    /** Epoch en milisegundos del próximo vencimiento. 0 = no definido. */
    val proximoVencimientoEpoch: Long = 0L,

    // ── Ingresos del cliente (cargados manualmente por el contador) ─────
    /** Ingresos brutos anuales acumulados del cliente (cargado por el contador). */
    val ingresoAnualCliente: Double = 0.0,
    /** Año al que corresponde el ingreso anual registrado (ej: 2025). */
    val anioIngresoCliente: Int = 0,
    /** Mes hasta el cual se acumularon los ingresos (1-12). 0 = no especificado. */
    val mesIngresoCliente: Int = 0,
    /** Historial de ingresos mensuales del cliente como JSON simple: "mes:monto,mes:monto,...". */
    val ingresosMensualesJson: String = "",

    // ── Datos ARCA / Argentina ──────────────────────────────────────────
    /** DNI del cliente. */
    val dni: String = "",
    /** Código + descripción de actividad ARCA (ej: "749900 - Otras actividades de servicios"). */
    val actividadArca: String = "",
    /** Número de punto de venta (0 = no definido). */
    val puntoVenta: Int = 0,
    /** Epoch de la fecha de alta en Monotributo. 0 = no definido. */
    val fechaAltaMonotributoEpoch: Long = 0L,
    /** Nombre de la obra social elegida. */
    val obraSocial: String = "",
    /** Domicilio fiscal del cliente. */
    val domicilioFiscal: String = "",
    /** Período de recategorización vigente: "ene-jun" o "jul-dic". Vacío = automático. */
    val periodoRecategorizacion: String = "",
    /** Si el cliente tiene empleados (afecta cuota). */
    val tieneEmpleados: Boolean = false,
    /** Cantidad de empleados (si tieneEmpleados = true). */
    val cantidadEmpleados: Int = 0,

    /**
     * Historial de pagos del Monotributo como JSON compacto.
     * Formato: "YYYY-MM:estado,YYYY-MM:estado,..."
     * Estados: "pagado" | "pendiente" | "vencido"
     * Ej: "2026-04:pagado,2026-03:pagado,2026-02:vencido"
     */
    val pagosMonotributoJson: String = "",

    // ── Honorarios del contador ─────────────────────────────────────────
    /** Monto del honorario de servicio del contador para este cliente. */
    val honorarioMonto: Double = 0.0,
    /** Día de vencimiento del honorario (1..28) para evitar problemas de meses cortos. */
    val honorarioDiaVencimiento: Int = 10,
    /** Frecuencia: mensual | trimestral. */
    val honorarioFrecuencia: String = "mensual",
    /** Activa/desactiva el seguimiento de cobranza de honorarios para este cliente. */
    val honorarioActivo: Boolean = false,
    /** Historial de pagos de honorarios: "YYYY-MM:epoch,YYYY-MM:epoch". */
    val honorarioPagosJson: String = ""
)
