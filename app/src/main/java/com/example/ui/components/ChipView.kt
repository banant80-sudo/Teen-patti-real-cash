package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChipBlue
import com.example.ui.theme.ChipGreen
import com.example.ui.theme.ChipRed

fun formatChips(amount: Long): String {
    return when {
        amount >= 10_000_000 -> "${amount / 1_000_000}Cr"
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0).replace(".0M", "M")
        amount >= 100_000 -> "${amount / 1000}K"
        amount >= 1_000 -> String.format("%.1fK", amount / 1000.0).replace(".0K", "K")
        else -> amount.toString()
    }
}

@Composable
fun ChipView(
    amount: Long,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val (primaryColor, edgeColor) = when {
        amount >= 100_000 -> Color(0xFFFFD700) to Color(0xFFB78103)
        amount >= 10_000 -> ChipRed to Color(0xFF8B0000)
        amount >= 1_000 -> ChipBlue to Color(0xFF0D47A1)
        amount >= 200 -> ChipGreen to Color(0xFF1B5E20)
        else -> Color(0xFF455A64) to Color(0xFF263238)
    }

    val textColor = if (amount >= 100_000) Color(0xFF1A1A1A) else Color.White

    Box(
        modifier = modifier
            .size(size)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(primaryColor, edgeColor)
                )
            )
            .border(1.5.dp, Color(0xFFFFF9C4), CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size - 8.dp)
                .border(1.dp, Color(0x66FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatChips(amount),
                color = textColor,
                fontSize = if (size > 36.dp) 11.sp else 9.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
