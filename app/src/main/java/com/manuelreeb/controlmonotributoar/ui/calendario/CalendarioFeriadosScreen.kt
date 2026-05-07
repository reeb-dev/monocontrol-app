package com.reeb.controlmonotributoar.ui.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reeb.controlmonotributoar.utils.ArgentinaHolidays
import com.reeb.controlmonotributoar.utils.NoLaborablesPrefs
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val Blanco = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val Celeste = Color(0xFF75AADB)
private val CelesteOsc = Color(0xFF4A86C8)
private val GrisTexto = Color(0xFF6B7B8C)
private val Rojo = Color(0xFFE53935)
private val Verde = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioFeriadosScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var customNoLaborables by remember { mutableStateOf(NoLaborablesPrefs.getAll(context)) }

    val feriadosAnio = remember(month) { ArgentinaHolidays.holidaysForYear(month.year) }
    val days = remember(month) { monthDaysGrid(month) }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Calendario laboral", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blanco)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { month = month.minusMonths(1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
                            }
                            Text(
                                text = month.month.getDisplayName(TextStyle.FULL, Locale("es", "AR"))
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "AR")) else it.toString() } +
                                    " ${month.year}",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = CelesteOsc
                            )
                            IconButton(onClick = { month = month.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
                            }
                        }
                        WeekHeader()
                        CalendarGrid(
                            days = days,
                            today = today,
                            customNoLaborables = customNoLaborables,
                            onDayClick = { date ->
                                if (!ArgentinaHolidays.isHoliday(date)) {
                                    NoLaborablesPrefs.toggle(context, date)
                                    customNoLaborables = NoLaborablesPrefs.getAll(context)
                                }
                            }
                        )
                        Text(
                            "Tocá un día para marcar/desmarcar como no laborable (si no es feriado nacional).",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisTexto
                        )
                    }
                }
            }

            item {
                LeyendaCard()
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Feriados nacionales ${month.year}", fontWeight = FontWeight.Bold, color = CelesteOsc)
                        feriadosAnio.filter { it.date.month == month.month }.forEach {
                            Text("• ${it.date.dayOfMonth} ${month.month.getDisplayName(TextStyle.SHORT, Locale("es", "AR"))}: ${it.name}")
                        }
                    }
                }
            }

            if (customNoLaborables.isNotEmpty()) {
                item {
                    Text("No laborables personalizados", color = CelesteOsc, fontWeight = FontWeight.Bold)
                }
                items(customNoLaborables.sorted()) { date ->
                    Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    NoLaborablesPrefs.toggle(context, date)
                                    customNoLaborables = NoLaborablesPrefs.getAll(context)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${date.dayOfMonth}/${date.monthValue}/${date.year}", modifier = Modifier.weight(1f))
                            Text("Quitar", color = Rojo, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WeekHeader() {
    val labels = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    Row(Modifier.fillMaxWidth()) {
        labels.forEach {
            Text(
                it,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = GrisTexto
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<LocalDate?>,
    today: LocalDate,
    customNoLaborables: Set<LocalDate>,
    onDayClick: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isHoliday = date != null && ArgentinaHolidays.isHoliday(date)
                    val isCustom = date != null && customNoLaborables.contains(date)
                    val isToday = date == today
                    val isWeekend = date != null && ArgentinaHolidays.isWeekend(date)
                    val bg = when {
                        isHoliday -> Rojo.copy(alpha = 0.15f)
                        isCustom -> Verde.copy(alpha = 0.15f)
                        isToday -> Celeste.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(bg, RoundedCornerShape(8.dp))
                            .clickable(enabled = date != null) { date?.let(onDayClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString().orEmpty(),
                            color = when {
                                isHoliday -> Rojo
                                isCustom -> Verde
                                isWeekend -> GrisTexto
                                else -> Color(0xFF1B3552)
                            },
                            fontWeight = if (isToday || isHoliday || isCustom) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeyendaCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Leyenda", fontWeight = FontWeight.Bold, color = CelesteOsc)
            Text("• Rojo: feriado nacional argentino")
            Text("• Verde: no laborable personalizado")
            Text("• Azul: dia actual")
        }
    }
}

private fun monthDaysGrid(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = (first.dayOfWeek.value + 6) % 7
    val total = month.lengthOfMonth()
    val cells = mutableListOf<LocalDate?>()
    repeat(offset) { cells.add(null) }
    for (d in 1..total) {
        cells.add(month.atDay(d))
    }
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells
}
