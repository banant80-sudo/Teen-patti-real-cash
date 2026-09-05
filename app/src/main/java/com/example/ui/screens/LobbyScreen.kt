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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AdminWalletEntity
import com.example.data.db.UserEntity
import com.example.data.model.TableStake
import com.example.data.repository.CommunityTableEvent
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
fun LobbyScreen(
    user: UserEntity?,
    tables: List<TableStake>,
    adminWallet: AdminWalletEntity? = null,
    latestCommunityEvent: CommunityTableEvent? = null,
    onJoinTable: (TableStake) -> Unit,
    onOpenDailyRewards: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenHandRankings: () -> Unit,
    onOpenAdminPortal: () -> Unit = {},
    pendingAdminCount: Int = 0,
    onOpenSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val chips = user?.chips ?: 50000L

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
    ) {
        // Elegant Dark Header Bar: bg-[#2B2930] shadow-md border-b border-[#49454F]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurface)
                .border(0.5.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar & Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkPrimary)
                            .border(1.dp, ElegantDarkOnPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.avatarEmoji ?: "A",
                            color = ElegantDarkOnPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = user?.name ?: "Arjun Sharma",
                            color = ElegantDarkTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = (user?.vipStatus ?: "Gold Member").uppercase(),
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Chips Counter + Admin Commission + Recharge & History Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Admin Commission Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2A1C00))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(50.dp))
                            .clickable { onOpenAdminPortal() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("admin_commission_header_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "👑", fontSize = 12.sp)
                            Text(
                                text = "₹${adminWallet?.availableCommissionInr ?: 0}",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (pendingAdminCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE53935)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$pendingAdminCount",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Chips Pill: bg-[#1C1B1F] rounded-full px-3 py-1 border border-[#49454F]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(ElegantDarkBackground)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(50.dp))
                            .clickable { onOpenStore() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("wallet_balance_header")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🪙", fontSize = 14.sp)
                            Text(
                                text = formatChips(chips),
                                color = ElegantDarkTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Chips",
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Support / Complaints Button
                    IconButton(
                        onClick = onOpenSupport,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("user_support_complaints_button")
                    ) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = "Complaints & Support",
                            tint = Color(0xFFCE93D8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("transaction_history_button")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Transaction History",
                            tint = ElegantDarkTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Live Multiplayer status banner: bg-[#211F26] border-b border-[#49454F]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurfaceInset)
                .padding(horizontal = 16.dp, vertical = 6.dp)
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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Text(
                        text = "14,850 Active Online Players",
                        color = Color(0xFFA5D6A7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Standard Teen Patti",
                    color = ElegantDarkTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Live Auto-Play Community 5% Commission Ticker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF221600))
                .border(0.5.dp, Color(0xFF664D00))
                .clickable { onOpenAdminPortal() }
                .padding(horizontal = 16.dp, vertical = 5.dp)
                .testTag("admin_live_commission_ticker")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "👑", fontSize = 12.sp)
                    Text(
                        text = latestCommunityEvent?.let {
                            "Auto-Play: ${it.tableName} • 5% Comm (+₹${it.commissionInr}) credited"
                        } ?: "Auto-Play: 5% table cut automatically deposits to Admin Wallet",
                        color = Color(0xFFFFE082),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                Text(
                    text = "ADMIN WALLET >",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main Scrollable Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Daily Reward Banner: bg-gradient-to-r from-[#4F378B] to-[#381E72] rounded-3xl p-4
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElegantDarkPrimaryContainer, ElegantDarkOnPrimary)
                            )
                        )
                        .clickable { onOpenDailyRewards() }
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("daily_rewards_lobby_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Reward",
                                color = ElegantDarkOnPrimaryContainer,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Collect up to 500k chips • 7-Day Streak & Wheel",
                                color = ElegantDarkPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(ElegantDarkOnPrimaryContainer)
                                .clickable { onOpenDailyRewards() }
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLAIM",
                                color = ElegantDarkOnPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                // Feature PLAY NOW Hero: bg-[#2B2930] rounded-3xl p-5 border border-[#49454F]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                        .clickable {
                            val quickTable = tables.firstOrNull { chips >= it.minEntryChips } ?: tables.firstOrNull()
                            quickTable?.let { onJoinTable(it) }
                        }
                        .testTag("quick_play_hero_card"),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "PLAY NOW",
                                    color = ElegantDarkPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic
                                )
                                Text(
                                    text = "Standard Teen Patti • 5.2k Online",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(text = "🎴", fontSize = 32.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Text(
                                    text = "Matchmaking Ready",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(ElegantDarkPrimary)
                                    .clickable {
                                        val quickTable = tables.firstOrNull { chips >= it.minEntryChips } ?: tables.firstOrNull()
                                        quickTable?.let { onJoinTable(it) }
                                    }
                                    .padding(horizontal = 22.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "START",
                                    color = ElegantDarkOnPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Secondary Quick Action Tiles Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tournaments Card: bg-[#2B2930] rounded-3xl p-4 border border-[#49454F]
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                            .clickable { onOpenHandRankings() }
                            .testTag("tournaments_lobby_card"),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Tournaments",
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Tournaments",
                                color = ElegantDarkTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "3 Live Now • Rules",
                                color = ElegantDarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Chip Store Card: bg-[#2B2930] rounded-3xl p-4 border border-[#49454F]
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                            .clickable { onOpenStore() }
                            .testTag("chip_store_lobby_card"),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Shop",
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Chip Store",
                                color = ElegantDarkTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instant UPI Recharges",
                                color = ElegantDarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Multiplayer Table",
                        color = ElegantDarkTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📖 Rules & Hands",
                        color = ElegantDarkPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onOpenHandRankings() }
                            .padding(4.dp)
                    )
                }
            }

            items(tables, key = { it.id }) { table ->
                TableCardItem(
                    table = table,
                    userChips = chips,
                    onJoin = { onJoinTable(table) }
                )
            }

            item {
                // Verified Security Badge: bg-[#211F26] rounded-2xl p-3 border border-dashed border-[#49454F]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElegantDarkSurfaceInset)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Real-time Secure Transactions",
                                color = ElegantDarkTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = 14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0x33E6E1E5))
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = 14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0x33E6E1E5))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom Navigation Bar: h-20 bg-[#2B2930] border-t border-[#49454F] px-6 flex items-center justify-between
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(ElegantDarkSurface)
                .border(0.5.dp, ElegantDarkBorder)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { /* Already Home */ }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Home",
                    color = ElegantDarkPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Rules / Hand Rankings
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenHandRankings() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = "Rules",
                    tint = ElegantDarkTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Rules",
                    color = ElegantDarkTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Shop
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenStore() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Shop",
                    tint = ElegantDarkTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Shop",
                    color = ElegantDarkTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Admin Portal (5% Commission & Withdrawal)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenAdminPortal() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("admin_portal_nav_item")
            ) {
                Text(text = "👑", fontSize = 18.sp)
                Text(
                    text = "Admin",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // History / Menu
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenHistory() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "History",
                    tint = ElegantDarkTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "History",
                    color = ElegantDarkTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TableCardItem(
    table: TableStake,
    userChips: Long,
    onJoin: () -> Unit
) {
    val canEnter = userChips >= table.minEntryChips

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                if (table.badge == "VIP Only") ElegantDarkPrimary else ElegantDarkBorder,
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = canEnter) { onJoin() }
            .testTag("table_card_${table.id}"),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = table.playerIcon, fontSize = 20.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = table.title,
                                color = ElegantDarkTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (table.badge != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElegantDarkPrimaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = table.badge,
                                        color = ElegantDarkOnPrimaryContainer,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = "5 Players • Fast Table",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onJoin,
                    enabled = canEnter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        disabledContainerColor = ElegantDarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (canEnter) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (canEnter) "PLAY NOW" else "LOW CHIPS",
                            color = if (canEnter) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stakes Bar: bg-[#211F26] rounded-2xl
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElegantDarkSurfaceInset)
                    .border(0.5.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "BOOT",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${formatChips(table.bootAmount)}",
                        color = ElegantDarkPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "CHAAL LIMIT",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${formatChips(table.minChaal)} - ₹${formatChips(table.maxChaal)}",
                        color = ElegantDarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MIN BUY-IN",
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${formatChips(table.minEntryChips)}",
                        color = CasinoGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
