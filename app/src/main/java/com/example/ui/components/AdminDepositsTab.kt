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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.db.DepositRequestEntity
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

@Composable
fun AdminDepositsTab(
    deposits: List<DepositRequestEntity>,
    onApproveDeposit: (requestId: Long, adminNote: String?) -> Unit,
    onRejectDeposit: (requestId: Long, reason: String) -> Unit
) {
    var filterPendingOnly by remember { mutableStateOf(false) }
    var selectedDepositForReject by remember { mutableStateOf<DepositRequestEntity?>(null) }
    var selectedDepositForApprove by remember { mutableStateOf<DepositRequestEntity?>(null) }
    var rejectReasonText by remember { mutableStateOf("") }
    var adminNoteText by remember { mutableStateOf("Payment verified in Admin account. Chips added.") }

    val displayedDeposits = remember(deposits, filterPendingOnly) {
        if (filterPendingOnly) {
            deposits.filter { it.status == "PENDING" }
        } else {
            deposits
        }
    }

    val pendingCount = deposits.count { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_deposits_tab"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Notification / Instruction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2430)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29B6F6))
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F3048)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF29B6F6),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "User Cash Deposits (Admin Account Direct)",
                        color = Color(0xFF81D4FA),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "When users add cash, payment arrives in Admin account. Once you verify the UTR in bank/UPI statement, confirm payment here to credit chips to user wallet.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Filter and Counter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Deposit Requests (${displayedDeposits.size})",
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
                        text = "All (${deposits.size})",
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
                        text = "Pending ($pendingCount)",
                        color = if (filterPendingOnly) Color.Black else CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Deposits List
        if (displayedDeposits.isEmpty()) {
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
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (filterPendingOnly) "No pending deposit requests" else "No deposit requests yet",
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
                items(displayedDeposits) { deposit ->
                    DepositItemCard(
                        deposit = deposit,
                        onApproveClick = { selectedDepositForApprove = deposit },
                        onRejectClick = { selectedDepositForReject = deposit }
                    )
                }
            }
        }
    }

    // Approve Deposit Confirmation Dialog
    if (selectedDepositForApprove != null) {
        val dep = selectedDepositForApprove!!
        Dialog(onDismissRequest = { selectedDepositForApprove = null }) {
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
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Confirm Payment Credit",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Have you verified that ₹${dep.amountInr} was received in your Admin UPI/Bank account for UTR ${dep.utrNumber}?",
                        color = ElegantDarkTextSecondary,
                        fontSize = 13.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElegantDarkSurfaceInset)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Player: ${dep.userName} (ID: ${dep.userId})",
                                color = ElegantDarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Amount: ₹${dep.amountInr} -> Chips to Credit: +${formatChips(dep.chipsToCredit)}",
                                color = CasinoGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UTR: ${dep.utrNumber} • ${dep.paymentMethod}",
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = adminNoteText,
                        onValueChange = { adminNoteText = it },
                        label = { Text("Admin Verification Note") },
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
                            onClick = { selectedDepositForApprove = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                onApproveDeposit(dep.id, adminNoteText)
                                selectedDepositForApprove = null
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Credit Chips",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Reject Deposit Dialog
    if (selectedDepositForReject != null) {
        val dep = selectedDepositForReject!!
        Dialog(onDismissRequest = { selectedDepositForReject = null }) {
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
                            text = "Reject Deposit Request",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Rejecting deposit for ₹${dep.amountInr} (UTR: ${dep.utrNumber}). No chips will be credited.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. UTR not found in bank statement, amount mismatch...") },
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
                            onClick = { selectedDepositForReject = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                onRejectDeposit(
                                    dep.id,
                                    rejectReasonText.ifBlank { "Payment not found in Admin bank/UPI records." }
                                )
                                selectedDepositForReject = null
                                rejectReasonText = ""
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkError),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Confirm Reject",
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
private fun DepositItemCard(
    deposit: DepositRequestEntity,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val isPending = deposit.status == "PENDING"
    val isApproved = deposit.status == "APPROVED"

    val statusColor = when (deposit.status) {
        "APPROVED" -> Color(0xFF4CAF50)
        "REJECTED" -> Color(0xFFEF5350)
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
            containerColor = if (isPending) Color(0xFF221F28) else ElegantDarkSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Ref ID and Status Badge
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
                        text = deposit.referenceId,
                        color = ElegantDarkTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${deposit.userName}",
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
                        text = when (deposit.status) {
                            "APPROVED" -> "CONFIRMED & CREDITED"
                            "REJECTED" -> "REJECTED"
                            else -> "PENDING CONFIRMATION"
                        },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Amount and Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${deposit.amountInr} Cash Paid",
                        color = Color(0xFF81C784),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "To Admin Account: ${deposit.paymentMethod}",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+${formatChips(deposit.chipsToCredit)} Chips",
                        color = CasinoGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "User Wallet Credit",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // UTR & Bank Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElegantDarkSurfaceInset)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bank / UPI Ref (UTR):",
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = deposit.utrNumber,
                            color = Color(0xFF81D4FA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(deposit.timestamp)),
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            if (!deposit.adminNote.isNullOrBlank()) {
                Text(
                    text = "Admin Note: ${deposit.adminNote}",
                    color = Color(0xFFA5D6A7),
                    fontSize = 11.sp
                )
            }

            if (!deposit.rejectionReason.isNullOrBlank()) {
                Text(
                    text = "Rejection Reason: ${deposit.rejectionReason}",
                    color = ElegantDarkError,
                    fontSize = 11.sp
                )
            }

            // Action Buttons for Pending Deposits
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
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Confirm & Add Chips", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
