package com.rotina.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.rotina.ui.theme.EmeraldOnPrimaryContainer
import com.rotina.ui.theme.EmeraldPrimaryContainer
import com.rotina.ui.theme.SkyOnSecondaryContainer
import com.rotina.ui.theme.SkySecondaryContainer
import com.rotina.ui.theme.VioletOnTertiaryContainer
import com.rotina.ui.theme.VioletTertiaryContainer

data class InfoIconeMeta(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

fun obterIconeMeta(nome: String): InfoIconeMeta {
    val n = nome.lowercase()
    return when {
        n.contains("água") || n.contains("agua") || n.contains("hidrat") || n.contains("copo") -> {
            InfoIconeMeta(
                icon = Icons.Filled.WaterDrop,
                containerColor = SkySecondaryContainer,
                contentColor = SkyOnSecondaryContainer
            )
        }
        n.contains("estud") || n.contains("ler") || n.contains("livro") || n.contains("leit") || n.contains("curs") -> {
            InfoIconeMeta(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                containerColor = VioletTertiaryContainer,
                contentColor = VioletOnTertiaryContainer
            )
        }
        n.contains("trein") || n.contains("acad") || n.contains("muscul") || n.contains("supin") || n.contains("biceps") || n.contains("perna") -> {
            InfoIconeMeta(
                icon = Icons.Filled.FitnessCenter,
                containerColor = EmeraldPrimaryContainer,
                contentColor = EmeraldOnPrimaryContainer
            )
        }
        n.contains("cardio") || n.contains("corri") || n.contains("esteir") || n.contains("caminh") || n.contains("bike") || n.contains("pedal") -> {
            InfoIconeMeta(
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                containerColor = Color(0xFF134E4A),
                contentColor = Color(0xFF2DD4BF)
            )
        }
        n.contains("código") || n.contains("codigo") || n.contains("program") || n.contains("dev") || n.contains("software") || n.contains("app") -> {
            InfoIconeMeta(
                icon = Icons.Filled.Code,
                containerColor = Color(0xFF312E81),
                contentColor = Color(0xFF818CF8)
            )
        }
        n.contains("sono") || n.contains("dorm") || n.contains("acord") || n.contains("descans") -> {
            InfoIconeMeta(
                icon = Icons.Filled.Bedtime,
                containerColor = Color(0xFF1E1B4B),
                contentColor = Color(0xFFA5B4FC)
            )
        }
        n.contains("comid") || n.contains("dieta") || n.contains("refei") || n.contains("alimen") || n.contains("calori") -> {
            InfoIconeMeta(
                icon = Icons.Filled.Restaurant,
                containerColor = Color(0xFF78350F),
                contentColor = Color(0xFFFBBF24)
            )
        }
        n.contains("oraç") || n.contains("orac") || n.contains("medit") || n.contains("respir") -> {
            InfoIconeMeta(
                icon = Icons.Filled.SelfImprovement,
                containerColor = Color(0xFF701A75),
                contentColor = Color(0xFFF472B6)
            )
        }
        else -> {
            InfoIconeMeta(
                icon = Icons.Filled.CheckCircle,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF94A3B8)
            )
        }
    }
}
