package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TransactionEntity
import com.example.ui.components.formatChips
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkOnPrimaryContainer
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
    ) {
        // Header: bg-[#2B2930] border-b border-[#49454F]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurface)
                .border(0.5.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkSurfaceElevated)
                    .border(1.dp, ElegantDarkBorder, CircleShape)
                    .testTag("history_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ElegantDarkTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Real-Time Transaction History",
                color = ElegantDarkTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "No Transactions Found",
                        color = ElegantDarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your chip purchases and game payouts will appear here in real-time.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { txn ->
                    TransactionItemCard(txn)
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(txn: TransactionEntity) {
    val isPositive = txn.amountChips >= 0
    val amountColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val amountPrefix = if (isPositive) "+" else ""

    val typeLabel = when (txn.type) {
        "RECHARGE" -> "Store Deposit (${txn.paymentMethod ?: "UPI"})"
        "GAME_WIN" -> "Teen Patti Table Win"
        "GAME_BET" -> "Table Bet"
        "DAILY_REWARD" -> "Daily Streak Bonus"
        "SPIN_BONUS" -> "Lucky Wheel Bonus"
        "WELCOME_BONUS" -> "Welcome Bonus"
        else -> txn.type
    }

    val icon = when (txn.type) {
        "RECHARGE" -> "💳"
        "GAME_WIN" -> "🏆"
        "GAME_BET" -> "🎲"
        "DAILY_REWARD" -> "🎁"
        "SPIN_BONUS" -> "🎰"
        else -> "🪙"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkSurfaceElevated)
                        .border(1.dp, ElegantDarkBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = typeLabel,
                        color = ElegantDarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ref: ${txn.referenceId}",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp)),
                        color = ElegantDarkTextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${formatChips(txn.amountChips)} Chips",
                    color = amountColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                if (txn.amountInr != null) {
                    Text(
                        text = "₹${txn.amountInr}",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElegantDarkPrimaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = txn.status,
                        color = ElegantDarkOnPrimaryContainer,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
