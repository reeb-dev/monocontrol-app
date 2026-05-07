package com.reeb.controlmonotributoar.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

data class HolidayInfo(
    val date: LocalDate,
    val name: String
)

object ArgentinaHolidays {

    fun holidaysForYear(year: Int): List<HolidayInfo> {
        val easter = easterSunday(year)
        val fixed = listOf(
            HolidayInfo(LocalDate.of(year, Month.JANUARY, 1), "Anio Nuevo"),
            HolidayInfo(LocalDate.of(year, Month.MARCH, 24), "Dia Nacional de la Memoria"),
            HolidayInfo(LocalDate.of(year, Month.APRIL, 2), "Dia del Veterano y de los Caidos"),
            HolidayInfo(LocalDate.of(year, Month.MAY, 1), "Dia del Trabajador"),
            HolidayInfo(LocalDate.of(year, Month.MAY, 25), "Revolucion de Mayo"),
            HolidayInfo(LocalDate.of(year, Month.JUNE, 17), "Paso a la Inmortalidad de M. M. de Guemes"),
            HolidayInfo(LocalDate.of(year, Month.JUNE, 20), "Paso a la Inmortalidad de M. Belgrano"),
            HolidayInfo(LocalDate.of(year, Month.JULY, 9), "Dia de la Independencia"),
            HolidayInfo(LocalDate.of(year, Month.NOVEMBER, 20), "Dia de la Soberania Nacional"),
            HolidayInfo(LocalDate.of(year, Month.DECEMBER, 8), "Inmaculada Concepcion de Maria"),
            HolidayInfo(LocalDate.of(year, Month.DECEMBER, 25), "Navidad")
        )

        val movable = listOf(
            HolidayInfo(easter.minusDays(48), "Lunes de Carnaval"),
            HolidayInfo(easter.minusDays(47), "Martes de Carnaval"),
            HolidayInfo(easter.minusDays(2), "Viernes Santo")
        )

        return (fixed + movable).sortedBy { it.date }
    }

    fun isHoliday(date: LocalDate): Boolean {
        return holidaysForYear(date.year).any { it.date == date }
    }

    fun holidayName(date: LocalDate): String? {
        return holidaysForYear(date.year).firstOrNull { it.date == date }?.name
    }

    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    // Algoritmo de Meeus/Jones/Butcher para calendario gregoriano.
    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
