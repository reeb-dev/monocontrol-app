package com.example.lmitemonotributo

import com.reeb.controlmonotributoar.utils.ArgentinaHolidays
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ArgentinaHolidaysTest {

    @Test
    fun includesFixedNationalHoliday() {
        val diaIndependencia = LocalDate.of(2026, 7, 9)
        assertTrue(ArgentinaHolidays.isHoliday(diaIndependencia))
    }

    @Test
    fun carnivalDaysAreDetected() {
        val holidays = ArgentinaHolidays.holidaysForYear(2026).map { it.name }
        assertTrue(holidays.contains("Lunes de Carnaval"))
        assertTrue(holidays.contains("Martes de Carnaval"))
    }
}
