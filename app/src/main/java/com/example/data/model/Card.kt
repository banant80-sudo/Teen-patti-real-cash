package com.example.data.model

enum class Suit(val symbol: String, val isRed: Boolean) {
    SPADES("♠", false),
    HEARTS("♥", true),
    CLUBS("♣", false),
    DIAMONDS("♦", true)
}

enum class Rank(val value: Int, val display: String) {
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A")
}

data class Card(
    val rank: Rank,
    val suit: Suit
) : Comparable<Card> {
    override fun compareTo(other: Card): Int = this.rank.value.compareTo(other.rank.value)

    fun shortLabel(): String = "${rank.display}${suit.symbol}"
}

enum class HandType(val rankWeight: Int, val displayName: String) {
    TRAIL(6, "Trail (Set / Trio)"),
    PURE_SEQUENCE(5, "Pure Sequence"),
    SEQUENCE(4, "Sequence"),
    COLOR(3, "Color (Flush)"),
    PAIR(2, "Pair"),
    HIGH_CARD(1, "High Card")
}

data class EvaluatedHand(
    val type: HandType,
    val primaryValue: Int,
    val secondaryValue: Int = 0,
    val kicker: Int = 0,
    val description: String
) : Comparable<EvaluatedHand> {
    override fun compareTo(other: EvaluatedHand): Int {
        if (this.type.rankWeight != other.type.rankWeight) {
            return this.type.rankWeight.compareTo(other.type.rankWeight)
        }
        if (this.primaryValue != other.primaryValue) {
            return this.primaryValue.compareTo(other.primaryValue)
        }
        if (this.secondaryValue != other.secondaryValue) {
            return this.secondaryValue.compareTo(other.secondaryValue)
        }
        return this.kicker.compareTo(other.kicker)
    }
}
