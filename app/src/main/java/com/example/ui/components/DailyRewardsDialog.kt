package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.DailyRewardEntity
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
import kotlinx.coroutines.launch

val DAILY_STREAK_REWARDS = listOf(
    5000L,
    10000L,
    25000L,
    50000L,
    100000L,
    250000L,
    500000L
)

val WHEEL_PRIZES = listOf(
    10000L,
    50000L,
    25000L,
    200000L,
    15000L,
    500000L,
    30000L,
    1000000L
)

val WHEEL_COLORS = listOf(
    Color(0xFFD0BCFF),
    Color(0xFF381E72),
    Color(0xFF4F378B),
    Color(0xFFEADDFF),
    Color(0xFF2B2930),
    Color(0xFF6750A4),
    Color(0xFF49454F),
    Color(0xFF7D5260)
)

@Composable
fun DailyRewardsDialog(
    rewardEntity: DailyRewardEntity?,
    onClaimStreak: (day: Int, amount: Long) -> Unit,
    onSpinWheel: (amount: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentStreak = rewardEntity?.currentStreakDay ?: 0
    val now = System.currentTimeMillis()
    // Can claim if no claim within last 12 hours
    val canClaimStreak = (now - (rewardEntity?.lastClaimTimestamp ?: 0L)) > 1000 * 60 * 60 * 12 || currentStreak == 0
    val nextDayToClaim = if (currentStreak >= 7) 1 else currentStreak + 1

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, ElegantDarkBorder, RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🎁", fontSize = 26.sp)
                        Column {
                            Text(
                                text = "Daily Bonus & Rewards",
                                color = ElegantDarkPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep your streak alive to win 500,000 chips!",
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElegantDarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: styled in Elegant Dark
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ElegantDarkSurfaceElevated,
                    contentColor = ElegantDarkPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElegantDarkPrimary
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "7-Day Streak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) ElegantDarkPrimary else ElegantDarkTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🎰 Lucky Spin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) ElegantDarkPrimary else ElegantDarkTextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // 7-Day Progressive Calendar
                    StreakCalendarContent(
                        currentStreak = currentStreak,
                        canClaim = canClaimStreak,
                        nextDay = nextDayToClaim,
                        onClaim = {
                            val rewardAmount = DAILY_STREAK_REWARDS[nextDayToClaim - 1]
                            onClaimStreak(nextDayToClaim, rewardAmount)
                        }
                    )
                } else {
                    // Lucky Wheel Spin
                    LuckyWheelContent(onPrizeWon = onSpinWheel)
                }
            }
        }
    }
}

@Composable
fun StreakCalendarContent(
    currentStreak: Int,
    canClaim: Boolean,
    nextDay: Int,
    onClaim: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Grid of first 6 days (2 rows of 3)
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..2) {
                    val dayNum = row * 3 + col + 1
                    val reward = DAILY_STREAK_REWARDS[dayNum - 1]
                    val isClaimed = dayNum <= currentStreak
                    val isCurrent = dayNum == nextDay

                    StreakDayCard(
                        day = dayNum,
                        amount = reward,
                        isClaimed = isClaimed,
                        isCurrent = isCurrent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Day 7 Mega Jackpot Card: bg-gradient-to-r from-[#4F378B] to-[#381E72]
        val day7Claimed = currentStreak >= 7
        val day7Current = nextDay == 7
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ElegantDarkPrimaryContainer, ElegantDarkOnPrimary)
                    )
                )
                .border(1.dp, ElegantDarkPrimary, RoundedCornerShape(20.dp))
                .padding(14.dp)
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
                    Text(text = "👑", fontSize = 28.sp)
                    Column {
                        Text(
                            text = "DAY 7 MEGA JACKPOT",
                            color = ElegantDarkOnPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "500,000 CHIPS + VIP BADGE",
                            color = ElegantDarkPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                if (day7Claimed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Claimed",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (day7Current) {
                    Text(
                        text = "READY!",
                        color = ElegantDarkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Claim Button: rounded-full in ElegantDarkPrimary
        Button(
            onClick = onClaim,
            enabled = canClaim,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("claim_daily_streak_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElegantDarkPrimary,
                disabledContainerColor = ElegantDarkSurfaceElevated
            ),
            shape = RoundedCornerShape(50.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = null,
                    tint = if (canClaim) ElegantDarkOnPrimary else ElegantDarkTextSecondary
                )
                Text(
                    text = if (canClaim) "Claim Day $nextDay (+${formatChips(DAILY_STREAK_REWARDS[nextDay - 1])} Chips)" else "Claimed for Today! Check back tomorrow",
                    color = if (canClaim) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StreakDayCard(
    day: Int,
    amount: Long,
    isClaimed: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isClaimed -> ElegantDarkPrimaryContainer
        isCurrent -> ElegantDarkSurfaceElevated
        else -> ElegantDarkSurfaceInset
    }

    val borderColor = when {
        isCurrent -> ElegantDarkPrimary
        isClaimed -> ElegantDarkPrimary.copy(alpha = 0.5f)
        else -> ElegantDarkBorder
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (isCurrent) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Day $day",
                color = if (isCurrent) ElegantDarkPrimary else ElegantDarkTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isClaimed) "✓" else "🪙",
                fontSize = 16.sp
            )
            Text(
                text = formatChips(amount),
                color = if (isClaimed) ElegantDarkOnPrimaryContainer else ElegantDarkTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun LuckyWheelContent(
    onPrizeWon: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var wonPrize by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spin & Win up to 1,000,000 Chips!",
            color = ElegantDarkTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Wheel Canvas
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pointer at top
            Text(
                text = "▼",
                color = ElegantDarkPrimary,
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
            )

            Canvas(
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
            ) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2
                val center = Offset(size.width / 2, size.height / 2)
                val sweepAngle = 360f / WHEEL_PRIZES.size
                val currentRot = rotation.value

                for (i in WHEEL_PRIZES.indices) {
                    val startAngle = currentRot + (i * sweepAngle)
                    drawArc(
                        color = WHEEL_COLORS[i % WHEEL_COLORS.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }

                // Outer border
                drawCircle(
                    color = Color(0xFFD0BCFF),
                    radius = radius - 1,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Inner hub
                drawCircle(
                    color = Color(0xFF1C1B1F),
                    radius = 28.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color(0xFFD0BCFF),
                    radius = 28.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Center Symbol
            Text(
                text = "🎰",
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (wonPrize != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElegantDarkPrimaryContainer)
                    .border(1.dp, ElegantDarkPrimary, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🎉 You Won +${formatChips(wonPrize!!)} Chips!",
                    color = ElegantDarkOnPrimaryContainer,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = {
                if (isSpinning) return@Button
                isSpinning = true
                wonPrize = null
                scope.launch {
                    val randomStopIndex = WHEEL_PRIZES.indices.random()
                    val targetPrize = WHEEL_PRIZES[randomStopIndex]
                    val sweep = 360f / WHEEL_PRIZES.size
                    val targetAngle = 360f * 5 + (270f - (randomStopIndex * sweep + sweep / 2))

                    rotation.animateTo(
                        targetValue = targetAngle,
                        animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
                    )
                    wonPrize = targetPrize
                    isSpinning = false
                    onPrizeWon(targetPrize)
                }
            },
            enabled = !isSpinning,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("spin_wheel_action_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElegantDarkPrimary,
                disabledContainerColor = ElegantDarkSurfaceElevated
            ),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = if (isSpinning) "Spinning Lucky Wheel..." else "SPIN LUCKY WHEEL FREE!",
                color = ElegantDarkOnPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
