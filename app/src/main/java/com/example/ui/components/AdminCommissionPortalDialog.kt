package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ScrollableTabRow
import com.example.audio.GameSoundManager
import com.example.data.db.AdminCommissionLogEntity
import com.example.data.db.AdminWalletEntity
import com.example.data.db.DepositRequestEntity
import com.example.data.db.SupportComplaintEntity
import com.example.data.db.WithdrawalRequestEntity
import com.example.data.model.WithdrawalSummary
import com.example.data.repository.CommunityTableEvent
import com.example.ui.theme.ElegantDarkBackground
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
fun AdminCommissionPortalDialog(
    adminWallet: AdminWalletEntity?,
    recentLogs: List<AdminCommissionLogEntity>,
    latestEvent: CommunityTableEvent?,
    isSimulationRunning: Boolean,
    pendingDeposits: List<DepositRequestEntity> = emptyList(),
    allDeposits: List<DepositRequestEntity> = emptyList(),
    pendingWithdrawals: List<WithdrawalRequestEntity> = emptyList(),
    allWithdrawals: List<WithdrawalRequestEntity> = emptyList(),
    openComplaints: List<SupportComplaintEntity> = emptyList(),
    allComplaints: List<SupportComplaintEntity> = emptyList(),
    onToggleSimulation: () -> Unit,
    onWithdrawCommission: (amountInr: Int, destinationType: String, destinationDetails: String, onResult: (Result<WithdrawalSummary>) -> Unit) -> Unit,
    onApproveDeposit: (requestId: Long, adminNote: String?) -> Unit = { _, _ -> },
    onRejectDeposit: (requestId: Long, reason: String) -> Unit = { _, _ -> },
    onApproveWithdrawal: (requestId: Long, utrTransferId: String) -> Unit = { _, _ -> },
    onRejectWithdrawal: (requestId: Long, reason: String) -> Unit = { _, _ -> },
    onResolveComplaint: (complaintId: Long, notes: String, bonusChips: Long) -> Unit = { _, _, _ -> },
    onRejectComplaint: (complaintId: Long, reason: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Deposits, 2: Withdrawals, 3: Complaints, 4: Logs
    var withdrawAmountText by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // "UPI" or "BANK"
    var upiIdText by remember { mutableStateOf("admin.patti@okhdfcbank") }
    var bankAccountText by remember { mutableStateOf("918273645012") }
    var ifscText by remember { mutableStateOf("HDFC0001234") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successSummary by remember { mutableStateOf<WithdrawalSummary?>(null) }

    val availableInr = adminWallet?.availableCommissionInr ?: 0
    val totalInr = adminWallet?.totalCommissionInr ?: 0
    val totalWithdrawn = adminWallet?.totalWithdrawnInr ?: 0
    val totalBoards = adminWallet?.totalGamesProcessed ?: 0L

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                    .testTag("admin_commission_portal_card"),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF332200))
                                    .border(1.dp, Color(0xFFFFD700), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👑", fontSize = 22.sp)
                            }

                            Column {
                                Text(
                                    text = "ADMIN COMMISSION WALLET",
                                    color = Color(0xFFFFD700),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "App Owner 5% House Payout & Real-time Auto-Tables",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkSurfaceElevated)
                                .testTag("close_admin_portal_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ElegantDarkTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-Play Community Ticker Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSimulationRunning) Color(0xFF1B2E1C) else Color(0xFF2E1C1C)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSimulationRunning) Color(0xFF4CAF50) else Color(0xFFE57373)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSimulationRunning) Color(0xFF4CAF50) else Color(0xFFE57373))
                                )
                                Column {
                                    Text(
                                        text = if (isSimulationRunning) "Auto-Play Tables: RUNNING (6 Active)" else "Auto-Play Tables: PAUSED",
                                        color = if (isSimulationRunning) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val latestText = latestEvent?.let {
                                        "${it.tableName}: ${it.winnerName} won ${it.potChips} chips -> +₹${it.commissionInr} (+${it.commissionChips} chips) to Admin"
                                    } ?: "Players actively playing in background rooms..."
                                    Text(
                                        text = latestText,
                                        color = ElegantDarkTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Button(
                                onClick = onToggleSimulation,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimulationRunning) Color(0xFF388E3C) else Color(0xFFD32F2F)
                                ),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    if (isSimulationRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSimulationRunning) "Pause" else "Resume",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Navigation
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = ElegantDarkSurfaceInset,
                        contentColor = Color(0xFFFFD700),
                        edgePadding = 4.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFFD700)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "💰 WALLET & PAYOUT",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                val pCount = pendingDeposits.size
                                Text(
                                    text = if (pCount > 0) "📥 DEPOSITS ($pCount)" else "📥 DEPOSITS",
                                    color = if (pCount > 0) Color(0xFFFFD700) else Color.Unspecified,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                val pCount = pendingWithdrawals.size
                                Text(
                                    text = if (pCount > 0) "📤 WITHDRAWALS ($pCount)" else "📤 WITHDRAWALS",
                                    color = if (pCount > 0) Color(0xFFFFD700) else Color.Unspecified,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = {
                                val cCount = openComplaints.size
                                Text(
                                    text = if (cCount > 0) "🎫 COMPLAINTS ($cCount)" else "🎫 COMPLAINTS",
                                    color = if (cCount > 0) Color(0xFFCE93D8) else Color.Unspecified,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = {
                                Text(
                                    text = "📊 LOGS (${recentLogs.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedTab) {
                        0 -> {
                        // Overview & Withdrawal View
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                // Balance Overview Cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Available to Withdraw Card
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2000)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "AVAILABLE TO WITHDRAW",
                                                color = Color(0xFFFFD700),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "₹$availableInr",
                                                color = Color.White,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = "${adminWallet?.availableCommissionChips ?: 0} chips",
                                                color = Color(0xFFFFE082),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Total Lifetime Commission
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "LIFETIME 5% CUT",
                                                color = ElegantDarkTextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "₹$totalInr",
                                                color = ElegantDarkPrimary,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "$totalBoards tables taxed",
                                                color = ElegantDarkTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                // Rule Explanation Banner
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceInset),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = "ℹ️", fontSize = 18.sp)
                                        Text(
                                            text = "5% board commission is deducted from every winner's pot and sent straight to your Admin Wallet. Winners do not receive this 5%. You can withdraw anytime with 0% penalty.",
                                            color = ElegantDarkTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            item {
                                // Withdrawal Form
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
                                    shape = RoundedCornerShape(18.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "TRANSFER COMMISSION TO BANK / UPI",
                                            color = ElegantDarkTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Amount Input
                                        OutlinedTextField(
                                            value = withdrawAmountText,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() }) {
                                                    withdrawAmountText = input
                                                    errorMessage = null
                                                }
                                            },
                                            label = { Text("Amount to Withdraw (₹)") },
                                            placeholder = { Text("e.g. 500") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            leadingIcon = {
                                                Text(
                                                    text = "₹",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFFFFD700),
                                                unfocusedBorderColor = ElegantDarkBorder,
                                                focusedLabelColor = Color(0xFFFFD700)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("admin_withdraw_amount_input")
                                        )

                                        // Quick Presets
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(500, 1000, 2500, availableInr).distinct().forEach { preset ->
                                                val label = if (preset == availableInr && preset > 0) "ALL (₹$preset)" else "₹$preset"
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (withdrawAmountText == preset.toString()) Color(0xFFFFD700) else ElegantDarkSurface)
                                                        .border(1.dp, if (withdrawAmountText == preset.toString()) Color(0xFFFFD700) else ElegantDarkBorder, RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            if (preset > 0) {
                                                                withdrawAmountText = preset.toString()
                                                                errorMessage = null
                                                            }
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (withdrawAmountText == preset.toString()) Color.Black else ElegantDarkTextPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        // Payment Mode Selector (UPI vs IMPS Bank)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (selectedPaymentMethod == "UPI") Color(0xFF332200) else ElegantDarkSurface)
                                                    .border(1.dp, if (selectedPaymentMethod == "UPI") Color(0xFFFFD700) else ElegantDarkBorder, RoundedCornerShape(10.dp))
                                                    .clickable { selectedPaymentMethod = "UPI" }
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.QrCode, contentDescription = "UPI", tint = if (selectedPaymentMethod == "UPI") Color(0xFFFFD700) else ElegantDarkTextSecondary, modifier = Modifier.size(16.dp))
                                                    Text("Instant UPI", color = if (selectedPaymentMethod == "UPI") Color(0xFFFFD700) else ElegantDarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (selectedPaymentMethod == "BANK") Color(0xFF332200) else ElegantDarkSurface)
                                                    .border(1.dp, if (selectedPaymentMethod == "BANK") Color(0xFFFFD700) else ElegantDarkBorder, RoundedCornerShape(10.dp))
                                                    .clickable { selectedPaymentMethod = "BANK" }
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = if (selectedPaymentMethod == "BANK") Color(0xFFFFD700) else ElegantDarkTextSecondary, modifier = Modifier.size(16.dp))
                                                    Text("Bank Transfer", color = if (selectedPaymentMethod == "BANK") Color(0xFFFFD700) else ElegantDarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        if (selectedPaymentMethod == "UPI") {
                                            OutlinedTextField(
                                                value = upiIdText,
                                                onValueChange = { upiIdText = it },
                                                label = { Text("Your UPI ID (VPA)") },
                                                placeholder = { Text("e.g. yourname@okhdfcbank") },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFFFFD700),
                                                    unfocusedBorderColor = ElegantDarkBorder
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            OutlinedTextField(
                                                value = bankAccountText,
                                                onValueChange = { bankAccountText = it },
                                                label = { Text("Bank Account Number") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = ifscText,
                                                onValueChange = { ifscText = it.uppercase() },
                                                label = { Text("Bank IFSC Code") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        errorMessage?.let { err ->
                                            Text(
                                                text = err,
                                                color = ElegantDarkError,
                                                fontSize = 12.sp
                                            )
                                        }

                                        // Confirm Button
                                        Button(
                                            onClick = {
                                                val amt = withdrawAmountText.toIntOrNull() ?: 0
                                                if (amt <= 0) {
                                                    errorMessage = "Please enter a valid amount."
                                                    return@Button
                                                }
                                                if (amt > availableInr) {
                                                    errorMessage = "Withdrawal amount exceeds available commission (₹$availableInr)."
                                                    return@Button
                                                }
                                                val destDetails = if (selectedPaymentMethod == "UPI") upiIdText.trim() else "A/C $bankAccountText (IFSC: $ifscText)"
                                                if (destDetails.isBlank()) {
                                                    errorMessage = "Please enter withdrawal destination details."
                                                    return@Button
                                                }

                                                isProcessing = true
                                                errorMessage = null
                                                onWithdrawCommission(amt, selectedPaymentMethod, destDetails) { res ->
                                                    isProcessing = false
                                                    res.onSuccess { summary ->
                                                        GameSoundManager.playWinFanfare()
                                                        successSummary = summary
                                                        withdrawAmountText = ""
                                                    }.onFailure { ex ->
                                                        errorMessage = ex.message ?: "Withdrawal failed."
                                                    }
                                                }
                                            },
                                            enabled = !isProcessing && availableInr > 0,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFD700),
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("submit_admin_withdrawal_button")
                                        ) {
                                            if (isProcessing) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.Black,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = "WITHDRAW COMMISSION (0% FEE)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                        1 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminDepositsTab(
                                    deposits = allDeposits,
                                    onApproveDeposit = onApproveDeposit,
                                    onRejectDeposit = onRejectDeposit
                                )
                            }
                        }
                        2 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminWithdrawalsTab(
                                    withdrawals = allWithdrawals,
                                    onApproveWithdrawal = onApproveWithdrawal,
                                    onRejectWithdrawal = onRejectWithdrawal
                                )
                            }
                        }
                        3 -> {
                            Box(modifier = Modifier.weight(1f)) {
                                AdminComplaintsTab(
                                    complaints = allComplaints,
                                    onResolveComplaint = onResolveComplaint,
                                    onRejectComplaint = onRejectComplaint
                                )
                            }
                        }
                        else -> {
                        // Commission Logs List Tab
                        if (recentLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No commission records yet. Rounds played by users or auto-tables will appear here.",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentLogs, key = { it.id }) { log ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = log.tableName,
                                                        color = ElegantDarkTextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (log.isAutoSimulated) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF213B22))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "AUTO",
                                                                color = Color(0xFFA5D6A7),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF332200))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "YOUR TABLE",
                                                                color = Color(0xFFFFD700),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Winner: ${log.winnerName} • Pot: ${log.totalPotChips} chips",
                                                    color = ElegantDarkTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp)),
                                                    color = ElegantDarkTextSecondary.copy(alpha = 0.7f),
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "+₹${log.commissionInr}",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "+${log.commissionChips} chips (5%)",
                                                    color = Color(0xFF81C784),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Success Receipt Modal
            successSummary?.let { summary ->
                Dialog(onDismissRequest = { successSummary = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "Commission Withdrawn!",
                                color = Color(0xFFFFD700),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "₹${summary.amountInr} has been successfully transferred to ${summary.destination}. 0% admin fees deducted.",
                                color = ElegantDarkTextPrimary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Ref ID: ${summary.referenceId}",
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            )

                            Button(
                                onClick = { successSummary = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}
