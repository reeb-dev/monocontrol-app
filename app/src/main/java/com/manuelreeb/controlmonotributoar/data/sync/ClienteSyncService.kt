package com.reeb.controlmonotributoar.data.sync

import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.data.repository.ClienteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Sincroniza la tabla `cliente` (Room) con la colección Firestore
 * `users/{uid}/clientes/{clienteId}`.
 *
 * Estrategia **offline-first** simple con last-write-wins por timestamp:
 *
 * 1. **Push**: para cada cliente con `syncPendiente = true`, sube el documento
 *    a Firestore. Si está marcado como `eliminado`, lo borra remoto y luego
 *    borra físico local.
 * 2. **Pull**: lee toda la colección remota y, para cada doc, si su
 *    `actualizadoEn` es mayor al local, lo guarda localmente con
 *    `syncPendiente = false` (para no rebotarlo).
 *
 * No requiere reglas de Firestore especiales: cada usuario sólo lee/escribe
 * en su propia subcolección `users/{uid}/clientes`.
 *
 * Si el usuario no está logueado o no hay red, `sync()` devuelve `false`
 * silenciosamente y se reintentará en la próxima ocasión.
 */
class ClienteSyncService(
    private val repo: ClienteRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun coleccionDelUsuario(uid: String) =
        firestore.collection("users").document(uid).collection("clientes")

    suspend fun sync(): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return runCatching {
            pushPendientes(uid)
            pullRemotos(uid)
            true
        }.getOrElse { false }
    }

    private suspend fun pushPendientes(uid: String) {
        val pendientes = repo.pendientesDeSync()
        if (pendientes.isEmpty()) return

        val coll = coleccionDelUsuario(uid)
        for (cliente in pendientes) {
            val docId = cliente.id.toString()
            if (cliente.eliminado) {
                runCatching { coll.document(docId).delete().await() }
                repo.eliminarFisico(cliente.id)
            } else {
                val data = cliente.toFirestoreMap(uid)
                runCatching { coll.document(docId).set(data, SetOptions.merge()).await() }
                    .onSuccess { repo.marcarSincronizado(cliente.id) }
            }
        }
    }

    private suspend fun pullRemotos(uid: String) {
        val coll = coleccionDelUsuario(uid)
        val snapshot = runCatching { coll.get().await() }.getOrNull() ?: return

        val nuevos = mutableListOf<ClienteEntity>()
        for (doc in snapshot.documents) {
            val remoto = doc.toClienteEntity(uid) ?: continue
            val local = repo.porId(remoto.id)
            // Si no existe local, o si remoto es más nuevo que local sincronizado,
            // aplicamos el remoto. Si local tiene cambios pendientes, ganan los locales
            // (se subirán en el próximo push).
            if (local == null) {
                nuevos.add(remoto.copy(syncPendiente = false))
            } else if (!local.syncPendiente && remoto.actualizadoEn > local.actualizadoEn) {
                nuevos.add(remoto.copy(syncPendiente = false))
            }
        }
        if (nuevos.isNotEmpty()) repo.guardarRemoto(nuevos)
    }
}

// ── Mapping ClienteEntity ⇄ Firestore Map ───────────────────────────────

private fun ClienteEntity.toFirestoreMap(uid: String): Map<String, Any?> = mapOf(
    "id" to id,
    "nombre" to nombre,
    "cuit" to cuit,
    "categoria" to categoria,
    "esVentaMuebles" to esVentaMuebles,
    "email" to email,
    "telefono" to telefono,
    "estadoCliente" to estadoCliente,
    "notas" to notas,
    "proximoVencimientoEpoch" to proximoVencimientoEpoch,
    "ingresoAnualCliente" to ingresoAnualCliente,
    "anioIngresoCliente" to anioIngresoCliente,
    "mesIngresoCliente" to mesIngresoCliente,
    "ingresosMensualesJson" to ingresosMensualesJson,
    "dni" to dni,
    "actividadArca" to actividadArca,
    "puntoVenta" to puntoVenta,
    "fechaAltaMonotributoEpoch" to fechaAltaMonotributoEpoch,
    "obraSocial" to obraSocial,
    "domicilioFiscal" to domicilioFiscal,
    "periodoRecategorizacion" to periodoRecategorizacion,
    "tieneEmpleados" to tieneEmpleados,
    "cantidadEmpleados" to cantidadEmpleados,
    "creadoEn" to creadoEn,
    "emailAsunto" to emailAsunto,
    "emailMensaje" to emailMensaje,
    "emailBorrador" to emailBorrador,
    "emailAdjuntoUri" to emailAdjuntoUri,
    "emailAdjuntoNombre" to emailAdjuntoNombre,
    "envioAutomatico" to envioAutomatico,
    "envioDiaMes" to envioDiaMes,
    "envioHora" to envioHora,
    "envioMinuto" to envioMinuto,
    "whatsappNumero" to whatsappNumero,
    "whatsappEnviar" to whatsappEnviar,
    "whatsappMensaje" to whatsappMensaje,
    "honorarioMonto" to honorarioMonto,
    "honorarioDiaVencimiento" to honorarioDiaVencimiento,
    "honorarioFrecuencia" to honorarioFrecuencia,
    "honorarioActivo" to honorarioActivo,
    "honorarioPagosJson" to honorarioPagosJson,
    "actualizadoEn" to actualizadoEn,
    "eliminado" to eliminado,
    "ownerUid" to uid
)

private fun com.google.firebase.firestore.DocumentSnapshot.toClienteEntity(
    uid: String
): ClienteEntity? = runCatching {
    ClienteEntity(
        id = getLong("id") ?: id.toLongOrNull() ?: return null,
        nombre = getString("nombre").orEmpty(),
        cuit = getString("cuit").orEmpty(),
        categoria = getString("categoria"),
        esVentaMuebles = getBoolean("esVentaMuebles") ?: false,
        email = getString("email").orEmpty(),
        telefono = getString("telefono").orEmpty(),
        estadoCliente = getString("estadoCliente")?.ifBlank { "activo" } ?: "activo",
        notas = getString("notas").orEmpty(),
        proximoVencimientoEpoch = getLong("proximoVencimientoEpoch") ?: 0L,
        ingresoAnualCliente = getDouble("ingresoAnualCliente") ?: 0.0,
        anioIngresoCliente = (getLong("anioIngresoCliente") ?: 0L).toInt(),
        mesIngresoCliente = (getLong("mesIngresoCliente") ?: 0L).toInt(),
        ingresosMensualesJson = getString("ingresosMensualesJson").orEmpty(),
        dni = getString("dni").orEmpty(),
        actividadArca = getString("actividadArca").orEmpty(),
        puntoVenta = (getLong("puntoVenta") ?: 0L).toInt(),
        fechaAltaMonotributoEpoch = getLong("fechaAltaMonotributoEpoch") ?: 0L,
        obraSocial = getString("obraSocial").orEmpty(),
        domicilioFiscal = getString("domicilioFiscal").orEmpty(),
        periodoRecategorizacion = getString("periodoRecategorizacion").orEmpty(),
        tieneEmpleados = getBoolean("tieneEmpleados") ?: false,
        cantidadEmpleados = (getLong("cantidadEmpleados") ?: 0L).toInt(),
        creadoEn = getLong("creadoEn") ?: System.currentTimeMillis(),
        emailAsunto = getString("emailAsunto").orEmpty(),
        emailMensaje = getString("emailMensaje").orEmpty(),
        emailBorrador = getString("emailBorrador").orEmpty(),
        emailAdjuntoUri = getString("emailAdjuntoUri").orEmpty(),
        emailAdjuntoNombre = getString("emailAdjuntoNombre").orEmpty(),
        envioAutomatico = getBoolean("envioAutomatico") ?: false,
        envioDiaMes = (getLong("envioDiaMes") ?: 18L).toInt(),
        envioHora = (getLong("envioHora") ?: 9L).toInt(),
        envioMinuto = (getLong("envioMinuto") ?: 0L).toInt(),
        whatsappNumero = getString("whatsappNumero").orEmpty(),
        whatsappEnviar = getBoolean("whatsappEnviar") ?: false,
        whatsappMensaje = getString("whatsappMensaje").orEmpty(),
        honorarioMonto = getDouble("honorarioMonto") ?: 0.0,
        honorarioDiaVencimiento = (getLong("honorarioDiaVencimiento") ?: 10L).toInt(),
        honorarioFrecuencia = getString("honorarioFrecuencia").orEmpty().ifBlank { "mensual" },
        honorarioActivo = getBoolean("honorarioActivo") ?: false,
        honorarioPagosJson = getString("honorarioPagosJson").orEmpty(),
        actualizadoEn = getLong("actualizadoEn") ?: 0L,
        syncPendiente = false,
        eliminado = getBoolean("eliminado") ?: false,
        ownerUid = uid
    )
}.getOrNull()

