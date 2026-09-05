package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.UserEntity
import com.example.data.model.GameEconomyRules
import com.example.data.model.PaymentPack
import com.example.data.model.WithdrawalSummary
import com.example.ui.components.WithdrawalSheet
import com.example.ui.components.formatChips
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
fun StoreScreen(
    user: UserEntity?,
    packs: List<PaymentPack>,
    todayWithdrawnInr: Int = 0,
    onSelectPack: (PaymentPack) -> Unit,
    onRequestWithdrawal: (amountInr: Int, destinationType: String, destinationDetails: String, onResult: (Result<WithdrawalSummary>) -> Unit) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Deposit / Store, 1: Withdraw Cash

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
    ) {
        // Top Bar: bg-[#2B2930] border-b border-[#49454F]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurface)
                .border(0.5.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
                        .testTag("store_back_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ElegantDarkTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (selectedTab == 0) "Cash Deposit & Chips" else "Wallet Cash Withdrawal",
                    color = ElegantDarkTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Current Chips Balance Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(ElegantDarkBackground)
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🪙", fontSize = 12.sp)
                    Text(
                        text = formatChips(user?.chips ?: 0L),
                        color = ElegantDarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Navigation Tabs: Deposit vs Withdrawal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 0) ElegantDarkPrimary else ElegantDarkSurfaceElevated)
                    .border(1.dp, if (selectedTab == 0) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DEPOSIT (₹300 - ₹10K)",
                    color = if (selectedTab == 0) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 1) ElegantDarkPrimary else ElegantDarkSurfaceElevated)
                    .border(1.dp, if (selectedTab == 1) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WITHDRAW (5% FEE)",
                    color = if (selectedTab == 1) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (selectedTab == 0) {
            // Deposit Content
            // Trust & Security Banner: bg-[#211F26]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantDarkSurfaceInset)
                    .border(0.5.dp, ElegantDarkBorder)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Deposit Limit: ₹${GameEconomyRules.MIN_DEPOSIT_INR} - ₹${GameEconomyRules.MAX_DEPOSIT_INR} • 100% Instant Delivery",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Select Chip Package (₹${GameEconomyRules.MIN_DEPOSIT_INR} to ₹${GameEconomyRules.MAX_DEPOSIT_INR})",
                        color = ElegantDarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(packs, key = { it.id }) { pack ->
                    StorePackCard(pack = pack, onBuy = { onSelectPack(pack) })
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Payment Gateways Supported: bg-[#211F26] rounded-2xl border border-[#49454F]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElegantDarkSurfaceInset)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ElegantDarkPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Supported Payment Methods",
                                    color = ElegantDarkTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "• Google Pay, PhonePe, Paytm, BHIM & All UPI VPAs\n• Visa, MasterCard, RuPay Debit & Credit Cards\n• Net Banking (SBI, HDFC, ICICI, Axis & 50+ Banks)\n• Deposit limits strictly enforced: Min ₹300, Max ₹10,000",
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Withdrawal Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    WithdrawalSheet(
                        user = user,
                        todayWithdrawnInr = todayWithdrawnInr,
                        onConfirmWithdrawal = onRequestWithdrawal
                    )
                }
            }
        }
    }
}

@Composable
fun StorePackCard(
    pack: PaymentPack,
    onBuy: () -> Unit
) {
    val totalChips = pack.chips + pack.bonusChips

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.5.dp,
                if (pack.isPopular) ElegantDarkPrimary else ElegantDarkBorder,
                RoundedCornerShape(24.dp)
            )
            .clickable { onBuy() }
            .testTag("store_pack_${pack.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (pack.isPopular) ElegantDarkSurfaceElevated else ElegantDarkSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🪙", fontSize = 28.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${formatChips(totalChips)} Chips",
                                color = ElegantDarkPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (pack.badge != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElegantDarkPrimaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = pack.badge,
                                        color = ElegantDarkOnPrimaryContainer,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        if (pack.bonusChips > 0) {
                            Text(
                                text = "Base ${formatChips(pack.chips)} + ${formatChips(pack.bonusChips)} FREE Bonus",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "₹${pack.priceInr}",
                        color = ElegantDarkOnPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
