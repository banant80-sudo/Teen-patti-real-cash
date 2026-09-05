package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Player
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary

@Composable
fun PlayerSeatView(
    player: Player,
    isActiveTurn: Boolean,
    turnProgress: Float, // 0.0 to 1.0
    showCardsFaceUp: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (player.isFolded) 0.45f else 1.0f

    val timerColor = when {
        turnProgress > 0.5f -> ElegantDarkPrimary
        turnProgress > 0.25f -> Color(0xFFFFB300)
        else -> Color(0xFFE53935)
    }

    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Cards Row (3 Cards with slight overlap / fanning)
            if (player.cards.isNotEmpty()) {
                Box(
                    modifier = Modifier.padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    player.cards.forEachIndexed { index, card ->
                        val rotation = (index - 1) * 12f
                        val offsetX = ((index - 1) * 16).dp
                        val offsetY = if (index == 1) (-3).dp else 0.dp

                        CardView(
                            card = card,
                            isFaceUp = showCardsFaceUp && !player.isBlind,
                            width = 38.dp,
                            height = 54.dp,
                            modifier = Modifier
                                .offset(x = offsetX, y = offsetY)
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
            }

            // Avatar Container + Turn Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                if (isActiveTurn && !player.isFolded) {
                    CircularProgressIndicator(
                        progress = { turnProgress },
                        modifier = Modifier.size(56.dp),
                        color = timerColor,
                        strokeWidth = 3.5.dp,
                        trackColor = Color(0x33FFFFFF)
                    )
                }

                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(player.avatarBgColor))
                        .border(
                            width = if (player.isWinner) 2.5.dp else 1.dp,
                            color = if (player.isWinner) ElegantDarkPrimary else ElegantDarkBorder,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.avatarEmoji,
                        fontSize = 22.sp
                    )
                }

                // Winner Crown / Badge
                if (player.isWinner) {
                    Text(
                        text = "👑",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-12).dp)
                    )
                }
            }

            // Name & Chip Stack Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElegantDarkSurface.copy(alpha = 0.94f))
                    .border(1.dp, if (player.isWinner) ElegantDarkPrimary else ElegantDarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = player.name,
                        color = if (player.isUser) ElegantDarkPrimary else ElegantDarkTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 10.sp
                        )
                        Text(
                            text = formatChips(player.chips),
                            color = ElegantDarkPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Status Pill (e.g. "Chaal ₹200", "Blind", "Folded", "Winner")
            if (player.lastAction.isNotBlank()) {
                val badgeBg = when {
                    player.isWinner -> Brush.horizontalGradient(listOf(ElegantDarkPrimary, ElegantDarkPrimaryContainer))
                    player.isFolded -> Brush.horizontalGradient(listOf(Color(0xFF333138), Color(0xFF49454F)))
                    player.isBlind -> Brush.horizontalGradient(listOf(Color(0xFF6750A4), Color(0xFF7D5260)))
                    else -> Brush.horizontalGradient(listOf(Color(0xFF4F378B), Color(0xFF381E72)))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = player.lastAction,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Floating Reaction Emoji
        AnimatedVisibility(
            visible = player.reactionEmoji != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-48).dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ElegantDarkSurfaceElevated)
                    .border(1.dp, ElegantDarkPrimary, CircleShape)
                    .padding(6.dp)
            ) {
                Text(
                    text = player.reactionEmoji ?: "",
                    fontSize = 24.sp
                )
            }
        }
    }
}
