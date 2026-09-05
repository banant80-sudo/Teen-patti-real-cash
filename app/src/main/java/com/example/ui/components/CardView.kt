package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Card

@Composable
fun CardView(
    card: Card?,
    isFaceUp: Boolean,
    modifier: Modifier = Modifier,
    width: Dp = 54.dp,
    height: Dp = 78.dp,
    elevation: Dp = 4.dp
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    Card(
        modifier = modifier
            .width(width)
            .height(height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (rotation <= 90f) {
            // Face Down Card Back
            FaceDownCard()
        } else {
            // Face Up Card Front (counter-rotate to render text right-side up)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                if (card != null) {
                    FaceUpCard(card)
                } else {
                    FaceDownCard()
                }
            }
        }
    }
}

@Composable
fun FaceDownCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B0000),
                        Color(0xFF5A000A),
                        Color(0xFF8B0000)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(6.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0x66FFD700), RoundedCornerShape(4.dp))
                .background(Color(0x33000000)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♠♥\n♦♣",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun FaceUpCard(card: Card) {
    val suitColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF212121)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(6.dp))
            .padding(3.dp)
    ) {
        // Top Left Rank & Suit
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.display,
                color = suitColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.sp
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
        }

        // Center Big Suit
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom Right Rank & Suit (upside down)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer { rotationZ = 180f },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.display,
                color = suitColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.sp
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 10.sp,
                lineHeight = 10.sp
            )
        }
    }
}
