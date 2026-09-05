package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.WithdrawalRequestEntity
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkError
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AdminWithdrawalsTab(
    withdrawals: List<WithdrawalRequestEntity>,
    onApproveWithdrawal: (requestId: Long, utrTransferId: String) -> Unit,
    onRejectWithdrawal: (requestId: Long, reason: String) -> Unit
) {
    var filterPendingOnly by remember { mutableStateOf(false) }
    var selectedForPayout by remember { mutableStateOf<WithdrawalRequestEntity?>(null) }
    var selectedForReject by remember { mutableStateOf<WithdrawalRequestEntity?>(null) }
    var transferUtrText by remember { mutableStateOf("IMPS-" + UUID.randomUUID().toString().take(8).uppercase()) }
    var rejectReasonText by remember { mutableStateOf("") }

    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING_VERIFICATION" }
    val displayedWithdrawals = if (filterPendingOnly) pendingWithdrawals else withdrawals

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_withdrawals_tab"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Notification Alert Banner if pending requests exist
        if (pendingWithdrawals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF332010)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CasinoGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 WITHDRAWAL NOTIFICATION (${pendingWithdrawals.size} Pending)",
                            color = CasinoGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val top = pendingWithdrawals.first()
                        Text(
                            text = "User '${top.userName}' has requested ₹${top.amountInr} payout (Wallet balance: ${formatChips(top.userChipsAtRequest)} chips). Check anti-fraud verification before transfer.",
                            color = ElegantDarkTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Anti-Fraud Policy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF19251D)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Anti-Fraud Policy: Verify user gameplay telemetry, table hands, and wallet balance before initiating bank/UPI transfer. No fraud guaranteed.",
                    color = Color(0xFFA5D6A7),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Filter and Counter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payout Requests (${displayedWithdrawals.size})",
                color = ElegantDarkTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!filterPendingOnly) ElegantDarkPrimary else ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .clickable { filterPendingOnly = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "All (${withdrawals.size})",
                        color = if (!filterPendingOnly) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (filterPendingOnly) CasinoGold else ElegantDarkSurface)
                        .border(1.dp, if (filterPendingOnly) CasinoGold else ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .clickable { filterPendingOnly = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Pending (${pendingWithdrawals.size})",
                        color = if (filterPendingOnly) Color.Black else CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Withdrawals List
        if (displayedWithdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (filterPendingOnly) "No pending withdrawal requests" else "No withdrawal requests yet",
                        color = ElegantDarkTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedWithdrawals) { req ->
                    WithdrawalItemCard(
                        withdrawal = req,
                        onApproveClick = {
                            transferUtrText = "IMPS-" + UUID.randomUUID().toString().take(8).uppercase()
                            selectedForPayout = req
                        },
                        onRejectClick = { selectedForReject = req }
                    )
                }
            }
        }
    }

    // Payout Approval Dialog
    if (selectedForPayout != null) {
        val req = selectedForPayout!!
        Dialog(onDismissRequest = { selectedForPayout = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Transfer Payout to User",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Transfer ₹${req.netInr} (after 5% fee ₹${req.feeInr}) to user's account.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 13.sp
                    )

                    // Destination Info Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElegantDarkSurfaceInset)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Recipient: ${req.userName} (User ID: ${req.userId})",
                                color = ElegantDarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Target: ${req.destinationType} -> ${req.destinationDetails}",
                                color = Color(0xFF81D4FA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Net Transfer Amount: ₹${req.netInr} (Requested ₹${req.amountInr})",
                                color = Color(0xFF81C784),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Anti-Fraud Status: ${req.riskStatus}",
                                color = if (req.riskStatus == "VERIFIED_GENUINE") Color(0xFF4CAF50) else CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = transferUtrText,
                        onValueChange = { transferUtrText = it },
                        label = { Text("Transfer Ref / IMPS UTR") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedForPayout = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                onApproveWithdrawal(req.id, transferUtrText)
                                selectedForPayout = null
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Confirm Transfer",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Reject & Refund Dialog
    if (selectedForReject != null) {
        val req = selectedForReject!!
        Dialog(onDismissRequest = { selectedForReject = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, ElegantDarkError, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = ElegantDarkError,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Reject & Refund Chips",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Rejecting withdrawal of ₹${req.amountInr}. The reserved ${formatChips(req.chipsDeducted)} chips will be automatically REFUNDED back to user's wallet.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Rejection / Refund Reason") },
                        placeholder = { Text("e.g. Invalid UPI ID / Bank details mismatch / Fraud detected") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkError,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedForReject = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                onRejectWithdrawal(
                                    req.id,
                                    rejectReasonText.ifBlank { "Rejected by admin: Details mismatch or safety policy." }
                                )
                                selectedForReject = null
                                rejectReasonText = ""
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkError),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Refund & Reject",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalItemCard(
    withdrawal: WithdrawalRequestEntity,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val isPending = withdrawal.status == "PENDING_VERIFICATION"
    val isTransferred = withdrawal.status == "APPROVED_TRANSFERRED"

    val statusColor = when (withdrawal.status) {
        "APPROVED_TRANSFERRED" -> Color(0xFF4CAF50)
        "REJECTED_REFUNDED" -> Color(0xFFEF5350)
        else -> CasinoGold
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isPending) CasinoGold.copy(alpha = 0.6f) else ElegantDarkBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) Color(0xFF241F28) else ElegantDarkSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: User Info & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = withdrawal.referenceId,
                        color = ElegantDarkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${withdrawal.userName}",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (withdrawal.status) {
                            "APPROVED_TRANSFERRED" -> "TRANSFERRED TO USER"
                            "REJECTED_REFUNDED" -> "REJECTED & REFUNDED"
                            else -> "PENDING VERIFICATION"
                        },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Financial Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${withdrawal.netInr} Net Payout",
                        color = Color(0xFF81C784),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Gross: ₹${withdrawal.amountInr} (5% Admin Fee: ₹${withdrawal.feeInr})",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatChips(withdrawal.userChipsAtRequest)} Chips",
                        color = CasinoGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User Balance at Request",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Destination Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElegantDarkSurfaceInset)
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Transfer to: ${withdrawal.destinationType}",
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(withdrawal.timestamp)),
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = withdrawal.destinationDetails,
                        color = Color(0xFF81D4FA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Anti-Fraud Telemetry Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1B2720))
                    .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = if (withdrawal.riskStatus == "VERIFIED_GENUINE") Color(0xFF4CAF50) else CasinoGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "Anti-Fraud Check: ${withdrawal.riskStatus}",
                            color = if (withdrawal.riskStatus == "VERIFIED_GENUINE") Color(0xFF81C784) else CasinoGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = withdrawal.fraudCheckNotes ?: "Gameplay telemetry clean. No multi-account collusion.",
                            color = Color(0xFFA5D6A7),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            if (!withdrawal.utrTransferId.isNullOrBlank()) {
                Text(
                    text = "Transfer UTR / IMPS: ${withdrawal.utrTransferId}",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!withdrawal.rejectionReason.isNullOrBlank()) {
                Text(
                    text = "Rejection Reason: ${withdrawal.rejectionReason}",
                    color = ElegantDarkError,
                    fontSize = 11.sp
                )
            }

            // Pending Actions
            if (isPending) {
                HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkError.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1.5f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Verify & Transfer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
