package com.reeb.controlmonotributoar.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val BlancoSuave = Color(0xFFF0F6FF)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)

@Composable
fun DonacionesDialog(
    onDismiss: () -> Unit,
    alias: String = "acatar.borne.isla.mp",
    cbu: String = "0000003100038131145602",
    mensaje: String = "Esta app es 100% gratuita y la mantengo en mi tiempo libre. " +
        "Si te resulta útil, podés colaborar para seguir mejorándola 💛"
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Amarillo.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("☕", fontSize = 28.sp)
            }
        },
        title = {
            Text(
                "Invitame un café",
                fontWeight = FontWeight.ExtraBold,
                color = TextoOscuro
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    mensaje.ifBlank {
                        "Esta app es 100% gratuita y la mantengo en mi tiempo libre. " +
                            "Si te resulta útil, podés colaborar para seguir mejorándola 💛"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro
                )

                // Alias MP
                DonacionItem(
                    label = "Alias Mercado Pago",
                    valor = alias,
                    icono = Icons.Default.ContentCopy,
                    accion = "Copiar",
                    onClick = {
                        clipboard.setText(AnnotatedString(alias))
                        Toast.makeText(context, "Alias copiado ✅", Toast.LENGTH_SHORT).show()
                    }
                )

                // CBU
                DonacionItem(
                    label = "CBU",
                    valor = cbu,
                    icono = Icons.Default.ContentCopy,
                    accion = "Copiar",
                    selectable = true,
                    onClick = {
                        clipboard.setText(AnnotatedString(cbu))
                        Toast.makeText(context, "CBU copiado ✅", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = Amarillo, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "¡Gracias por tu apoyo!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Celeste)
            ) {
                Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun DonacionItem(
    label: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    accion: String,
    selectable: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlancoSuave)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = TextoSuave,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectable) {
                SelectionContainer(modifier = Modifier.weight(1f)) {
                    Text(
                        valor,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoOscuro,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    valor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            TextButton(onClick = onClick) {
                Icon(icono, null, tint = CelesteOsc, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(accion, color = CelesteOsc, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

