package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Card
import com.example.data.model.Rank
import com.example.data.model.Suit
import com.example.ui.components.CardView
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary

@Composable
fun HandRankingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
    ) {
        // Header: bg-[#2B2930] border-b border-[#49454F]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkSurface)
                .border(0.5.dp, ElegantDarkBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    .testTag("rankings_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ElegantDarkTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Teen Patti Hand Rankings & Rules",
                color = ElegantDarkTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Hands Ranking (Highest to Lowest)",
                    color = ElegantDarkPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                HandRankItem(
                    rankNumber = 1,
                    title = "Trail / Trio / Set",
                    description = "Three cards of the same rank. A-A-A is the highest trail, 2-2-2 is the lowest.",
                    cards = listOf(
                        Card(Rank.ACE, Suit.SPADES),
                        Card(Rank.ACE, Suit.HEARTS),
                        Card(Rank.ACE, Suit.DIAMONDS)
                    )
                )
            }

            item {
                HandRankItem(
                    rankNumber = 2,
                    title = "Pure Sequence (Straight Flush)",
                    description = "Three consecutive cards of the same suit. In Teen Patti, A-2-3 is the highest pure sequence, followed by A-K-Q.",
                    cards = listOf(
                        Card(Rank.ACE, Suit.HEARTS),
                        Card(Rank.TWO, Suit.HEARTS),
                        Card(Rank.THREE, Suit.HEARTS)
                    )
                )
            }

            item {
                HandRankItem(
                    rankNumber = 3,
                    title = "Sequence (Straight / Run)",
                    description = "Three consecutive cards not all in the same suit. A-2-3 is the highest sequence, followed by A-K-Q.",
                    cards = listOf(
                        Card(Rank.ACE, Suit.SPADES),
                        Card(Rank.KING, Suit.HEARTS),
                        Card(Rank.QUEEN, Suit.CLUBS)
                    )
                )
            }

            item {
                HandRankItem(
                    rankNumber = 4,
                    title = "Color (Flush)",
                    description = "Three cards of the same suit that are not in sequence. High card breaks ties.",
                    cards = listOf(
                        Card(Rank.KING, Suit.SPADES),
                        Card(Rank.TEN, Suit.SPADES),
                        Card(Rank.FOUR, Suit.SPADES)
                    )
                )
            }

            item {
                HandRankItem(
                    rankNumber = 5,
                    title = "Pair (Two of a Kind)",
                    description = "Two cards of the same rank. Higher pair wins; higher third card (kicker) breaks ties.",
                    cards = listOf(
                        Card(Rank.QUEEN, Suit.HEARTS),
                        Card(Rank.QUEEN, Suit.CLUBS),
                        Card(Rank.EIGHT, Suit.DIAMONDS)
                    )
                )
            }

            item {
                HandRankItem(
                    rankNumber = 6,
                    title = "High Card",
                    description = "When no player has a pair or better, the hand with the highest card wins.",
                    cards = listOf(
                        Card(Rank.ACE, Suit.DIAMONDS),
                        Card(Rank.JACK, Suit.SPADES),
                        Card(Rank.SIX, Suit.CLUBS)
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Gameplay rules
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElegantDarkSurfaceInset)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Game Rules & Tactics",
                            color = ElegantDarkPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Blind: You play without looking at your cards, paying half the regular chaal.\n• Chaal: Once you see your cards, you must bet 2x the blind amount.\n• Show: When only 2 players remain, either player can ask for a Showdown.\n• Sideshow: A seen player can request a private card comparison with the previous seen player.",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HandRankItem(
    rankNumber: Int,
    title: String,
    description: String,
    cards: List<Card>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rankNumber",
                            color = ElegantDarkOnPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = title,
                        color = ElegantDarkTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = description,
                    color = ElegantDarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                cards.forEach { card ->
                    CardView(
                        card = card,
                        isFaceUp = true,
                        width = 32.dp,
                        height = 46.dp
                    )
                }
            }
        }
    }
}
