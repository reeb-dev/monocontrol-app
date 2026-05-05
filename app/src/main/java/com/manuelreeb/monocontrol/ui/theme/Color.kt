package com.manuelreeb.monocontrol.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// 🎨 Paleta profesional — Monotributo Al Día
// Inspirada en la bandera argentina con feel de app financiera moderna.
// ─────────────────────────────────────────────────────────────────────────────

// ── Azules (primarios) ──────────────────────────────────────────────────────
val CelesteArgentino     = Color(0xFF4A90D9)   // celeste bandera — identidad
val CelesteArgentino2    = Color(0xFF82B4E8)   // variante clara
val CelesteOscuro        = Color(0xFF1A5FA8)   // botones, íconos activos
val CelesteMuyOscuro     = Color(0xFF0D3B6E)   // dark fuerte, titulos
val CelesteSuperficie    = Color(0xFFEFF6FF)   // fondo de cards activas
val CelesteBorde         = Color(0xFFBDD7F5)   // bordes suaves

// ── Blancos / Neutros ───────────────────────────────────────────────────────
val BlancoArgentino      = Color(0xFFFFFFFF)
val BlancoSuave          = Color(0xFFF5F8FE)   // fondo global
val GrisCeleste          = Color(0xFFDCEAF8)   // superficies secundarias
val GrisMedio            = Color(0xFF8A9BB0)   // texto secundario
val GrisClaro            = Color(0xFFECF0F5)   // separadores, chips inactivos

// ── Sol (acentos) ───────────────────────────────────────────────────────────
val SolAmarillo          = Color(0xFFF9A825)   // acento principal — CTA, badges
val SolAmarillo2         = Color(0xFFFFC107)   // hover / pressed
val SolSuave             = Color(0xFFFFF8E1)   // fondo de chips amarillos
val SolMarron            = Color(0xFF7B4F12)   // texto sobre fondo amarillo

// ── Semánticos ──────────────────────────────────────────────────────────────
val VerdeExito           = Color(0xFF2E7D32)   // al día, guardado, ok
val VerdeClaro           = Color(0xFFE8F5E9)   // fondo verde
val VerdeMedio           = Color(0xFF43A047)   // chips positivos

val NaranjaAlerta        = Color(0xFFE65100)   // por vencer, advertencia
val NaranjaClaro         = Color(0xFFFFF3E0)   // fondo naranja

val RojoError            = Color(0xFFC62828)   // vencido, error crítico
val RojoClaro            = Color(0xFFFFEBEE)   // fondo rojo
val RojoMedio            = Color(0xFFE53935)   // acciones destructivas

val AzulInfo             = Color(0xFF0277BD)   // informativo
val AzulInfoClaro        = Color(0xFFE1F5FE)   // fondo info

// ── Textos ──────────────────────────────────────────────────────────────────
val TextoOscuro          = Color(0xFF0D2A4A)   // texto principal
val TextoMedio           = Color(0xFF3D5A73)   // subtítulos
val TextoSuave           = Color(0xFF6B8099)   // texto terciario, placeholders

// ── Gradientes (usados en headers) ─ como string de componentes ─────────────
val GradientePrimario    = listOf(CelesteOscuro, CelesteArgentino)
val GradienteSol         = listOf(SolAmarillo, Color(0xFFFFCA28))
