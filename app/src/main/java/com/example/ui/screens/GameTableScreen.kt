package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.example.audio.GameSoundManager
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GameEconomyRules
import com.example.data.repository.GamePhase
import com.example.data.repository.TeenPattiTableEngine
import com.example.ui.components.AiDifficultySelectorDialog
import com.example.ui.components.CardView
import com.example.ui.components.FullChatBottomSheet
import com.example.ui.components.InGameAddCashDialog
import com.example.ui.components.PlayerSeatView
import com.example.ui.components.QuickChatBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTableScreen(
    engine: TeenPattiTableEngine,
    onLeaveTable: () -> Unit,
    onOpenStore: () -> Unit,
    onInGameDeposit: (amountInr: Int, chips: Long, method: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by engine.uiState.collectAsStateWithLifecycle()
    val isSoundEnabled by GameSoundManager.isSoundEnabled.collectAsStateWithLifecycle()
    var isChatSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var betMultiplier by remember { mutableIntStateOf(1) }
    var isDifficultyDialogOpen by remember { mutableStateOf(false) }
    var isAddCashDialogOpen by remember { mutableStateOf(false) }

    val userPlayer = uiState.players.firstOrNull { it.isUser }
    val isUserTurn = uiState.activePlayerIndex == 0 && uiState.phase == GamePhase.IN_PROGRESS && userPlayer?.isFolded == false
    val activeNonFoldedCount = uiState.players.count { !it.isFolded }
    val canShow = activeNonFoldedCount == 2 && isUserTurn

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantDarkSurface)
                    .border(1.dp, ElegantDarkBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onLeaveTable,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkBorder, CircleShape)
                            .testTag("leave_table_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Table",
                            tint = ElegantDarkTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = uiState.stake.title,
                            color = ElegantDarkTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Boot: ₹${uiState.stake.bootAmount}",
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // AI Difficulty Picker & In-Game Add Cash & Chips Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // AI Difficulty Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkPrimary, RoundedCornerShape(12.dp))
                            .clickable { isDifficultyDialogOpen = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("ai_difficulty_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🤖", fontSize = 11.sp)
                            Text(
                                text = uiState.tableAiDifficulty.title.uppercase(),
                                color = ElegantDarkPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // In-Game Add Cash Button (5-Minute Window Protection)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2E7D32))
                            .border(1.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                            .clickable {
                                engine.startInGameRecharge()
                                isAddCashDialogOpen = true
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("in_game_add_cash_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "+ CASH",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // User Chip Count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(20.dp))
                            .clickable { onOpenStore() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("table_chips_balance")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Text(
                                text = formatChips(userPlayer?.chips ?: 0L),
                                color = ElegantDarkPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Sound Effects Toggle Button
                    IconButton(
                        onClick = { GameSoundManager.toggleSound() },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                            .border(1.dp, if (isSoundEnabled) ElegantDarkPrimary else ElegantDarkBorder, CircleShape)
                            .testTag("table_sound_toggle")
                    ) {
                        Text(
                            text = if (isSoundEnabled) "🔊" else "🔇",
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // In-Game Recharge 5-Minute Warning Banner
            if (uiState.isInGameRechargeActive) {
                val min = uiState.inGameRechargeSecondsRemaining / 60
                val sec = uiState.inGameRechargeSecondsRemaining % 60
                val formattedTime = String.format("%02d:%02d", min, sec)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD84315))
                        .clickable { isAddCashDialogOpen = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
                            Text(text = "⏱️", fontSize = 12.sp)
                            Text(
                                text = "RUNNING GAME SEAT RESERVED (5 MIN)",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formattedTime,
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Felt Table Area: Sleek modern dark felt
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Oval Casino Felt Table styled in Elegant Dark
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(160.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2D2938),
                                    Color(0xFF221E2C),
                                    Color(0xFF181620)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF4F378B),
                                    ElegantDarkPrimary,
                                    Color(0xFF4F378B)
                                )
                            ),
                            shape = RoundedCornerShape(160.dp)
                        )
                        .border(
                            width = 7.dp,
                            color = Color(0xFF141218),
                            shape = RoundedCornerShape(160.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Pot Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TOTAL POT",
                            color = ElegantDarkTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(ElegantDarkSurface.copy(alpha = 0.92f))
                                .border(1.5.dp, ElegantDarkPrimary, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🏆", fontSize = 18.sp)
                            Text(
                                text = "₹${formatChips(uiState.potAmount)}",
                                color = ElegantDarkPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Status Ticker
                        Text(
                            text = uiState.statusMessage,
                            color = ElegantDarkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Start Round Button if not running
                        if (uiState.phase == GamePhase.IDLE || uiState.phase == GamePhase.ROUND_OVER) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { engine.startNewRound() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("start_round_button")
                            ) {
                                Text(
                                    text = if (uiState.phase == GamePhase.IDLE) "START GAME" else "NEXT ROUND ♠",
                                    color = ElegantDarkOnPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Positioned Player Seats
                    // Seat 1: Aarav (Top Left)
                    val p1 = uiState.players.getOrNull(1)
                    if (p1 != null) {
                        PlayerSeatView(
                            player = p1,
                            isActiveTurn = uiState.activePlayerIndex == 1,
                            turnProgress = if (uiState.activePlayerIndex == 1) uiState.turnTimeRemaining / 15f else 0f,
                            showCardsFaceUp = uiState.phase == GamePhase.SHOWDOWN || uiState.phase == GamePhase.ROUND_OVER,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 14.dp, top = 20.dp)
                        )
                    }

                    // Seat 2: Priya (Top Center)
                    val p2 = uiState.players.getOrNull(2)
                    if (p2 != null) {
                        PlayerSeatView(
                            player = p2,
                            isActiveTurn = uiState.activePlayerIndex == 2,
                            turnProgress = if (uiState.activePlayerIndex == 2) uiState.turnTimeRemaining / 15f else 0f,
                            showCardsFaceUp = uiState.phase == GamePhase.SHOWDOWN || uiState.phase == GamePhase.ROUND_OVER,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                        )
                    }

                    // Seat 3: Vikram (Top Right)
                    val p3 = uiState.players.getOrNull(3)
                    if (p3 != null) {
                        PlayerSeatView(
                            player = p3,
                            isActiveTurn = uiState.activePlayerIndex == 3,
                            turnProgress = if (uiState.activePlayerIndex == 3) uiState.turnTimeRemaining / 15f else 0f,
                            showCardsFaceUp = uiState.phase == GamePhase.SHOWDOWN || uiState.phase == GamePhase.ROUND_OVER,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 14.dp, top = 20.dp)
                        )
                    }

                    // Seat 4: Neha (Right Middle)
                    val p4 = uiState.players.getOrNull(4)
                    if (p4 != null) {
                        PlayerSeatView(
                            player = p4,
                            isActiveTurn = uiState.activePlayerIndex == 4,
                            turnProgress = if (uiState.activePlayerIndex == 4) uiState.turnTimeRemaining / 15f else 0f,
                            showCardsFaceUp = uiState.phase == GamePhase.SHOWDOWN || uiState.phase == GamePhase.ROUND_OVER,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        )
                    }

                    // Seat 0: USER (Bottom Center)
                    if (userPlayer != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // User Cards (Larger format)
                            if (userPlayer.cards.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    userPlayer.cards.forEach { card ->
                                        CardView(
                                            card = card,
                                            isFaceUp = !userPlayer.isBlind || uiState.phase == GamePhase.ROUND_OVER,
                                            width = 46.dp,
                                            height = 66.dp
                                        )
                                    }
                                }

                                // "See Cards" Button if user is currently blind
                                if (userPlayer.isBlind && uiState.phase == GamePhase.IN_PROGRESS) {
                                    Button(
                                        onClick = {
                                            GameSoundManager.playDealCard()
                                            engine.seeCards()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElegantDarkSurfaceElevated
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .border(1.dp, ElegantDarkPrimary, RoundedCornerShape(20.dp))
                                            .testTag("see_cards_action_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = ElegantDarkPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "SEE CARDS",
                                                color = ElegantDarkPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // User Player Seat info
                            PlayerSeatView(
                                player = userPlayer,
                                isActiveTurn = uiState.activePlayerIndex == 0,
                                turnProgress = if (uiState.activePlayerIndex == 0) uiState.turnTimeRemaining / 15f else 0f,
                                showCardsFaceUp = !userPlayer.isBlind || uiState.phase == GamePhase.ROUND_OVER
                            )
                        }
                    }
                }

                // Winner Fanfare Modal
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.phase == GamePhase.ROUND_OVER && uiState.winnerPlayer != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    val winner = uiState.winnerPlayer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.5.dp, ElegantDarkPrimary, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "👑", fontSize = 36.sp)
                            Text(
                                text = "${winner?.name} WINS!",
                                color = ElegantDarkPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            // 5% Board Commission Breakdown per user requirement
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ElegantDarkSurfaceInset)
                                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Board Total Pot:", color = ElegantDarkTextSecondary, fontSize = 12.sp)
                                        Text("₹${formatChips(uiState.potAmount)}", color = ElegantDarkTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Board Commission (5%):", color = Color(0xFFFF8A80), fontSize = 12.sp)
                                        Text("- ₹${formatChips(uiState.commissionAmount)}", color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        "👑 5% house cut sent directly to Admin Commission Wallet",
                                        color = Color(0xFFFFD700),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Net Payout Credited:", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("₹${formatChips(uiState.netPayout)}", color = Color(0xFF81C784), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            if (uiState.winnerHand != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElegantDarkPrimaryContainer)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .border(1.dp, ElegantDarkPrimary, RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = uiState.winnerHand!!.description,
                                        color = ElegantDarkOnPrimaryContainer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { engine.startNewRound() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("winner_next_round_button")
                            ) {
                                Text(
                                    text = "Play Next Round ♠",
                                    color = ElegantDarkOnPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // In-Game Action Bar for Active Player
            if (uiState.phase == GamePhase.IN_PROGRESS && userPlayer?.isFolded == false) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PACK / FOLD Button
                        Button(
                            onClick = { engine.userFold() },
                            enabled = isUserTurn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7D5260),
                                disabledContainerColor = ElegantDarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("action_pack_button")
                        ) {
                            Text(
                                text = "PACK",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // SHOW Button (enabled when 2 players left)
                        if (canShow) {
                            Button(
                                onClick = { engine.userShow() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimaryContainer),
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("action_show_button")
                            ) {
                                Text(
                                    text = "SHOW",
                                    color = ElegantDarkOnPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Multiplier stepper (1x, 2x)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElegantDarkSurfaceElevated)
                                .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = isUserTurn) {
                                    betMultiplier = if (betMultiplier == 1) 2 else 1
                                }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "${betMultiplier}X",
                                color = ElegantDarkPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // CHAAL / BLIND Bet Button
                        val isBlind = userPlayer.isBlind
                        val base = uiState.currentStake
                        val betAmount = if (isBlind) base * betMultiplier else base * 2 * betMultiplier
                        val label = if (isBlind) "BLIND ₹$betAmount" else "CHAAL ₹$betAmount"

                        Button(
                            onClick = { engine.userBet(!isBlind, betMultiplier) },
                            enabled = isUserTurn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBlind) Color(0xFF6750A4) else ElegantDarkPrimary,
                                disabledContainerColor = ElegantDarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("action_chaal_bet_button")
                        ) {
                            Text(
                                text = label,
                                color = if (isUserTurn && !isBlind) ElegantDarkOnPrimary else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Quick Chat & Emojis Bar
            QuickChatBar(
                onSendQuickChat = { engine.sendUserChat(it) },
                onSendEmoji = { engine.sendUserReaction(it) },
                onOpenFullChat = { isChatSheetOpen = true }
            )
        }

        // Full Chat Bottom Sheet
        if (isChatSheetOpen) {
            FullChatBottomSheet(
                sheetState = sheetState,
                messages = uiState.chatMessages,
                onSendMessage = { engine.sendUserChat(it) },
                onDismiss = { isChatSheetOpen = false }
            )
        }

        // AI Difficulty Selection Dialog
        if (isDifficultyDialogOpen) {
            AiDifficultySelectorDialog(
                currentDifficulty = uiState.tableAiDifficulty,
                onSelectDifficulty = { level ->
                    engine.setAiDifficulty(level)
                },
                onDismiss = { isDifficultyDialogOpen = false }
            )
        }

        // In-Game Add Cash Dialog (5-Minute Window)
        if (isAddCashDialogOpen) {
            InGameAddCashDialog(
                secondsRemaining = uiState.inGameRechargeSecondsRemaining,
                storePacks = GameEconomyRules.STORE_PACKS,
                onDismiss = {
                    isAddCashDialogOpen = false
                    engine.cancelInGameRecharge()
                },
                onConfirmAddCash = { inr, chips, method ->
                    isAddCashDialogOpen = false
                    GameSoundManager.playBetChips()
                    onInGameDeposit(inr, chips, method)
                }
            )
        }
    }
}
