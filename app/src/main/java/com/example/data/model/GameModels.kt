package com.example.data.model

import androidx.compose.ui.graphics.Color

data class Player(
    val id: String,
    val name: String,
    val isUser: Boolean = false,
    val chips: Long,
    val cards: List<Card> = emptyList(),
    val isBlind: Boolean = true,
    val isFolded: Boolean = false,
    val currentBet: Long = 0,
    val avatarEmoji: String = "🤠",
    val avatarBgColor: Long = 0xFF37474F,
    val lastAction: String = "",
    val reactionEmoji: String? = null,
    val isWinner: Boolean = false,
    val aiDifficulty: AiDifficulty = AiDifficulty.CASUAL
)

enum class AiDifficulty(
    val title: String,
    val badge: String,
    val color: Long,
    val description: String
) {
    BEGINNER(
        title = "Beginner",
        badge = "BEGINNER",
        color = 0xFF4CAF50,
        description = "Practice-friendly: plays casually, stays blind, folds easily under pressure. 0% bluffing."
    ),
    CASUAL(
        title = "Casual",
        badge = "CASUAL",
        color = 0xFF2196F3,
        description = "Balanced style: checks cards after 1-2 rounds, raises on Colors/Sequences, 10% bluffing."
    ),
    ADVANCED(
        title = "Advanced",
        badge = "ADVANCED",
        color = 0xFFFF9800,
        description = "Strategic: computes pot odds, disciplined folds on weak high cards, semi-bluffs."
    ),
    EXPERT(
        title = "Expert",
        badge = "EXPERT PRO",
        color = 0xFFE91E63,
        description = "Pro AI: mathematically optimal Teen Patti strategy, deception bluffs, sharp showdown timing."
    )
}

object GameEconomyRules {
    const val BOARD_WIN_COMMISSION_PERCENT = 5 // 5% house commission on winning board
    const val WITHDRAWAL_FEE_PERCENT = 5 // 5% withdrawal fee
    const val MIN_WITHDRAWAL_INR = 500 // Min ₹500
    const val MAX_WITHDRAWAL_DAILY_INR = 20000 // Max ₹20,000 per day
    const val MIN_DEPOSIT_INR = 300 // Min ₹300
    const val MAX_DEPOSIT_INR = 10000 // Max ₹10,000
    const val CHIPS_PER_INR = 1000L // 1 INR = 1,000 chips
    const val IN_GAME_RECHARGE_RESERVE_SECONDS = 300 // 5 minutes window for in-game add cash

    val STORE_PACKS = listOf(
        PaymentPack("pack_300", 300000L, 50000L, 300, "STARTER"),
        PaymentPack("pack_500", 500000L, 100000L, 500, "POPULAR", isPopular = true),
        PaymentPack("pack_1000", 1000000L, 250000L, 1000, "HOT"),
        PaymentPack("pack_2500", 2500000L, 750000L, 2500, "VIP"),
        PaymentPack("pack_5000", 5000000L, 2000000L, 5000, "PRO"),
        PaymentPack("pack_10000", 10000000L, 5000000L, 10000, "MEGA")
    )
}

data class WithdrawalSummary(
    val referenceId: String,
    val amountInr: Int,
    val feeInr: Int,
    val netAmountInr: Int,
    val destination: String,
    val chipsDeducted: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val message: String,
    val isSystem: Boolean = false,
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class TableStake(
    val id: String,
    val title: String,
    val bootAmount: Long,
    val minChaal: Long,
    val maxChaal: Long,
    val minEntryChips: Long,
    val playerIcon: String = "🎲",
    val badge: String? = null
)

data class PaymentPack(
    val id: String,
    val chips: Long,
    val bonusChips: Long = 0,
    val priceInr: Int,
    val badge: String? = null,
    val isPopular: Boolean = false
)

enum class PaymentCategory(val title: String, val iconName: String) {
    UPI("UPI Fast Pay", "upi"),
    CARDS("Debit / Credit Cards", "card"),
    NET_BANKING("Net Banking", "bank"),
    WALLET("Wallets", "wallet")
}

data class PaymentOption(
    val id: String,
    val category: PaymentCategory,
    val name: String,
    val subtitle: String,
    val badge: String? = null
)
