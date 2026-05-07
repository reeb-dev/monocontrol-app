package com.reeb.controlmonotributoar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

val CompactTypography = AppTypography.copy(
    headlineMedium = AppTypography.headlineMedium.copy(fontSize = 25.sp, lineHeight = 31.sp),
    titleLarge = AppTypography.titleLarge.copy(fontSize = 20.sp, lineHeight = 25.sp),
    titleMedium = AppTypography.titleMedium.copy(fontSize = 16.sp, lineHeight = 21.sp),
    bodyLarge = AppTypography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = AppTypography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = AppTypography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = AppTypography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = AppTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp)
)