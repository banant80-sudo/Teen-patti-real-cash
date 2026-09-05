package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GameEconomyRules
import com.example.data.model.PaymentPack
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOnPrimaryContainer
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary

@Composable
fun InGameAddCashDialog(
    secondsRemaining: Int,
    storePacks: List<PaymentPack>,
    onDismiss: () -> Unit,
    onConfirmAddCash: (amountInr: Int, chipsToAdd: Long, method: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPack by remember { mutableStateOf<PaymentPack?>(storePacks.firstOrNull { it.priceInr == 500 } ?: storePacks.firstOrNull()) }
    var customAmountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("UPI Instant (GPay / PhonePe)") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, ElegantDarkPrimary, RoundedCornerShape(24.dp))
                .testTag("in_game_add_cash_dialog"),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with 5-Minute Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🪙", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "In-Game Add Cash",
                                color = ElegantDarkTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Seat Reserved for 5:00 Minutes",
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .testTag("close_in_game_recharge")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElegantDarkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 5-Minute Window Pill Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2A1C16))
                        .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Table Seat Protected",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Turn timeout is paused while adding chips",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF9800))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formattedTime,
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Deposit Limits Info Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElegantDarkSurfaceInset)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deposit Limits",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Min ₹${GameEconomyRules.MIN_DEPOSIT_INR} • Max ₹${GameEconomyRules.MAX_DEPOSIT_INR}",
                            color = ElegantDarkPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Select Quick Pack:",
                    color = ElegantDarkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Quick Packs Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(storePacks.filter { it.priceInr in GameEconomyRules.MIN_DEPOSIT_INR..GameEconomyRules.MAX_DEPOSIT_INR }) { pack ->
                        val isSelected = selectedPack?.id == pack.id && customAmountText.isEmpty()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElegantDarkPrimaryContainer else ElegantDarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) ElegantDarkPrimary else ElegantDarkBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedPack = pack
                                    customAmountText = ""
                                    errorMessage = null
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("in_game_pack_${pack.priceInr}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "₹${pack.priceInr}",
                                    color = if (isSelected) ElegantDarkOnPrimaryContainer else ElegantDarkTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatChips(pack.chips + pack.bonusChips),
                                    color = if (isSelected) ElegantDarkPrimary else CasinoGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Custom Amount Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Or Enter Custom Amount (₹300 - ₹10,000):",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 5) {
                                customAmountText = input
                                selectedPack = null
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("e.g. 500, 1000, 5000", color = ElegantDarkTextSecondary, fontSize = 13.sp) },
                        prefix = { Text("₹ ", color = ElegantDarkPrimary, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary,
                            focusedContainerColor = ElegantDarkBackground,
                            unfocusedContainerColor = ElegantDarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_game_custom_deposit_input")
                    )
                }

                // Error Message if any
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Fast UPI Payment Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val methods = listOf("UPI Fast Pay", "PhonePe", "Paytm", "GPay")
                    methods.forEach { method ->
                        val isChosen = selectedMethod.contains(method)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChosen) ElegantDarkPrimaryContainer else ElegantDarkSurfaceInset)
                                .border(0.5.dp, if (isChosen) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedMethod = method }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                color = if (isChosen) ElegantDarkOnPrimaryContainer else ElegantDarkTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Add Cash Confirm Button
                Button(
                    onClick = {
                        val amount = if (customAmountText.isNotEmpty()) {
                            customAmountText.toIntOrNull() ?: 0
                        } else {
                            selectedPack?.priceInr ?: 0
                        }

                        if (amount < GameEconomyRules.MIN_DEPOSIT_INR) {
                            errorMessage = "Minimum deposit limit is ₹${GameEconomyRules.MIN_DEPOSIT_INR}"
                            return@Button
                        }
                        if (amount > GameEconomyRules.MAX_DEPOSIT_INR) {
                            errorMessage = "Maximum deposit limit is ₹${GameEconomyRules.MAX_DEPOSIT_INR}"
                            return@Button
                        }

                        val chips = if (selectedPack != null && customAmountText.isEmpty()) {
                            selectedPack!!.chips + selectedPack!!.bonusChips
                        } else {
                            val base = amount * GameEconomyRules.CHIPS_PER_INR
                            base + (base * 15) / 100 // 15% in-game recharge bonus
                        }

                        onConfirmAddCash(amount, chips, selectedMethod)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("in_game_confirm_deposit_button")
                ) {
                    val amountDisplay = if (customAmountText.isNotEmpty()) customAmountText else "${selectedPack?.priceInr ?: 500}"
                    Text(
                        text = "ADD ₹$amountDisplay TO TABLE STACK",
                        color = ElegantDarkOnPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Instant Credit • Verified Bank & UPI Gateways",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
