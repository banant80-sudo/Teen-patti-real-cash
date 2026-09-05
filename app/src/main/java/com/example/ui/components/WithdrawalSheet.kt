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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.audio.GameSoundManager
import com.example.data.db.UserEntity
import com.example.data.model.GameEconomyRules
import com.example.data.model.WithdrawalSummary
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
fun WithdrawalSheet(
    user: UserEntity?,
    todayWithdrawnInr: Int,
    onConfirmWithdrawal: (amountInr: Int, destinationType: String, destinationDetails: String, onResult: (Result<WithdrawalSummary>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("1000") }
    var selectedTab by remember { mutableStateOf(0) } // 0: UPI, 1: Bank Transfer
    var upiId by remember { mutableStateOf("king777@okhdfcbank") }
    var accountNumber by remember { mutableStateOf("918234567890") }
    var ifscCode by remember { mutableStateOf("HDFC0001234") }
    var accountHolderName by remember { mutableStateOf(user?.name ?: "Arjun Sharma") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successSummary by remember { mutableStateOf<WithdrawalSummary?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val userChips = user?.chips ?: 0L
    val maxAvailableInr = (userChips / GameEconomyRules.CHIPS_PER_INR).toInt()
    val remainingDailyQuota = (GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR - todayWithdrawnInr).coerceAtLeast(0)

    val currentAmount = amountText.toIntOrNull() ?: 0
    val feeInr = (currentAmount * GameEconomyRules.WITHDRAWAL_FEE_PERCENT) / 100
    val netPayoutInr = (currentAmount - feeInr).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Limit & Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ElegantDarkBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Withdrawable Cash Balance",
                            color = ElegantDarkTextSecondary,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "₹$maxAvailableInr",
                                color = ElegantDarkTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(${formatChips(userChips)} Chips)",
                                color = CasinoGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B5E20))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Instant 24x7",
                            color = Color(0xFFA5D6A7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Limit: ₹${GameEconomyRules.MIN_WITHDRAWAL_INR} - ₹${GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR}",
                            color = ElegantDarkTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Today Withdrawn: ₹$todayWithdrawnInr • Remaining Quota: ₹$remainingDailyQuota",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElegantDarkSurfaceInset)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "5% Fee",
                            color = ElegantDarkPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Amount Input Field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Enter Withdrawal Amount (INR):",
                color = ElegantDarkTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 6) {
                        amountText = input
                        errorMessage = null
                    }
                },
                placeholder = { Text("500 to 20000", color = ElegantDarkTextSecondary) },
                prefix = { Text("₹ ", color = ElegantDarkPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDarkPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedTextColor = ElegantDarkTextPrimary,
                    unfocusedTextColor = ElegantDarkTextPrimary,
                    focusedContainerColor = ElegantDarkSurface,
                    unfocusedContainerColor = ElegantDarkSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdrawal_amount_input")
            )
        }

        // Quick Amount Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val quickAmounts = listOf(500, 1000, 2000, 5000, 10000, 20000)
            items(quickAmounts) { amt ->
                val isSelected = amountText == amt.toString()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ElegantDarkPrimaryContainer else ElegantDarkSurfaceElevated)
                        .border(1.dp, if (isSelected) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            amountText = amt.toString()
                            errorMessage = null
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("quick_withdraw_$amt")
                ) {
                    Text(
                        text = "₹$amt",
                        color = if (isSelected) ElegantDarkOnPrimaryContainer else ElegantDarkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5% Commission / Fee Live Breakdown Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ElegantDarkSurfaceInset)
                .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gross Withdrawal Amount:", color = ElegantDarkTextSecondary, fontSize = 12.sp)
                    Text(text = "₹$currentAmount", color = ElegantDarkTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Wallet Cash Withdrawal Fee (5%):", color = Color(0xFFFF8A80), fontSize = 12.sp)
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(12.dp))
                    }
                    Text(text = "- ₹$feeInr", color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Net Amount Credited to You:", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "₹$netPayoutInr", color = Color(0xFF81C784), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Destination Selection Tabs (UPI vs Bank Account)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 0) ElegantDarkPrimaryContainer else ElegantDarkSurfaceElevated)
                    .border(1.dp, if (selectedTab == 0) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = if (selectedTab == 0) ElegantDarkOnPrimaryContainer else ElegantDarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "UPI Instant",
                        color = if (selectedTab == 0) ElegantDarkOnPrimaryContainer else ElegantDarkTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 1) ElegantDarkPrimaryContainer else ElegantDarkSurfaceElevated)
                    .border(1.dp, if (selectedTab == 1) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (selectedTab == 1) ElegantDarkOnPrimaryContainer else ElegantDarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Bank IMPS",
                        color = if (selectedTab == 1) ElegantDarkOnPrimaryContainer else ElegantDarkTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Destination Inputs
        if (selectedTab == 0) {
            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it; errorMessage = null },
                label = { Text("Enter UPI ID (VPA)") },
                placeholder = { Text("e.g. mobile@upi or username@okhdfcbank") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDarkPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedTextColor = ElegantDarkTextPrimary,
                    unfocusedTextColor = ElegantDarkTextPrimary,
                    focusedContainerColor = ElegantDarkSurface,
                    unfocusedContainerColor = ElegantDarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("withdrawal_upi_input")
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = accountHolderName,
                    onValueChange = { accountHolderName = it; errorMessage = null },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it; errorMessage = null },
                    label = { Text("Bank Account Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ifscCode,
                    onValueChange = { ifscCode = it.uppercase(); errorMessage = null },
                    label = { Text("Bank IFSC Code") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Error message display
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

        // Submit Button
        Button(
            onClick = {
                val amt = amountText.toIntOrNull() ?: 0
                if (amt < GameEconomyRules.MIN_WITHDRAWAL_INR) {
                    errorMessage = "Minimum withdrawal is ₹${GameEconomyRules.MIN_WITHDRAWAL_INR}"
                    return@Button
                }
                if (amt > GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR) {
                    errorMessage = "Maximum per transaction withdrawal is ₹${GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR}"
                    return@Button
                }
                if (amt > remainingDailyQuota) {
                    errorMessage = "Exceeds daily limit! Remaining today: ₹$remainingDailyQuota (Daily Limit: ₹20,000)"
                    return@Button
                }
                if (amt > maxAvailableInr) {
                    errorMessage = "Insufficient balance! Available: ₹$maxAvailableInr"
                    return@Button
                }

                val destType = if (selectedTab == 0) "UPI" else "Bank IMPS"
                val destDetails = if (selectedTab == 0) upiId else "$accountNumber ($ifscCode)"

                if (selectedTab == 0 && !upiId.contains("@")) {
                    errorMessage = "Please enter a valid UPI ID (e.g. name@upi)"
                    return@Button
                }

                isProcessing = true
                onConfirmWithdrawal(amt, destType, destDetails) { result ->
                    isProcessing = false
                    result.onSuccess { summary ->
                        GameSoundManager.playWinFanfare()
                        successSummary = summary
                        errorMessage = null
                    }.onFailure { err ->
                        errorMessage = err.message ?: "Withdrawal failed"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
            shape = RoundedCornerShape(14.dp),
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_withdrawal_button")
        ) {
            Text(
                text = if (isProcessing) "Processing..." else "WITHDRAW ₹$currentAmount (Net: ₹$netPayoutInr)",
                color = ElegantDarkOnPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Trust badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Regulated Payout Gateway • 5% Deduction Fee • 100% Guaranteed",
                color = ElegantDarkTextSecondary,
                fontSize = 10.sp
            )
        }
    }

    // Success Receipt Dialog
    successSummary?.let { summary ->
        Dialog(onDismissRequest = { successSummary = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(24.dp))
                    .testTag("withdrawal_success_dialog"),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B5E20)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Withdrawal Initiated!",
                        color = ElegantDarkTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "₹${summary.netAmountInr} is on its way to your account",
                        color = Color(0xFF81C784),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElegantDarkSurfaceInset)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref ID:", color = ElegantDarkTextSecondary, fontSize = 11.sp)
                                Text(summary.referenceId, color = ElegantDarkPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Requested Amount:", color = ElegantDarkTextSecondary, fontSize = 11.sp)
                                Text("₹${summary.amountInr}", color = ElegantDarkTextPrimary, fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("5% Platform Fee:", color = Color(0xFFFF8A80), fontSize = 11.sp)
                                Text("- ₹${summary.feeInr}", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Payout:", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${summary.netAmountInr}", color = Color(0xFF81C784), fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Destination:", color = ElegantDarkTextSecondary, fontSize = 11.sp)
                                Text(summary.destination, color = ElegantDarkTextPrimary, fontSize = 11.sp)
                            }
                        }
                    }

                    Button(
                        onClick = { successSummary = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DONE", color = ElegantDarkOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
