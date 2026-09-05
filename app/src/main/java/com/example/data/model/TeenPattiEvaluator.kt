package com.example.data.model

object TeenPattiEvaluator {

    fun createStandardDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                deck.add(Card(rank, suit))
            }
        }
        deck.shuffle()
        return deck
    }

    fun evaluate(cards: List<Card>): EvaluatedHand {
        require(cards.size == 3) { "Teen Patti requires exactly 3 cards" }
        val sorted = cards.sortedByDescending { it.rank.value }
        val r1 = sorted[0].rank.value
        val r2 = sorted[1].rank.value
        val r3 = sorted[2].rank.value

        val sameSuit = sorted[0].suit == sorted[1].suit && sorted[1].suit == sorted[2].suit

        // Check for Trail (Trio / Three of a Kind)
        if (r1 == r2 && r2 == r3) {
            return EvaluatedHand(
                type = HandType.TRAIL,
                primaryValue = r1,
                description = "Trail of ${sorted[0].rank.display}s"
            )
        }

        // Check for Sequence (A-2-3 is highest in Teen Patti rules: values 14, 3, 2, normalized to rank weight 15)
        val isA23 = r1 == 14 && r2 == 3 && r3 == 2
        val isStandardSequence = (r1 - r2 == 1) && (r2 - r3 == 1)
        val isSequence = isA23 || isStandardSequence
        val sequenceRank = if (isA23) 15 else r1

        if (sameSuit && isSequence) {
            val label = if (isA23) "A-2-3" else "${sorted[0].rank.display}-${sorted[1].rank.display}-${sorted[2].rank.display}"
            return EvaluatedHand(
                type = HandType.PURE_SEQUENCE,
                primaryValue = sequenceRank,
                description = "Pure Sequence ($label of ${sorted[0].suit.symbol})"
            )
        }

        if (isSequence) {
            val label = if (isA23) "A-2-3" else "${sorted[0].rank.display}-${sorted[1].rank.display}-${sorted[2].rank.display}"
            return EvaluatedHand(
                type = HandType.SEQUENCE,
                primaryValue = sequenceRank,
                description = "Sequence ($label)"
            )
        }

        if (sameSuit) {
            return EvaluatedHand(
                type = HandType.COLOR,
                primaryValue = r1,
                secondaryValue = r2,
                kicker = r3,
                description = "Color of ${sorted[0].suit.symbol} (${sorted[0].rank.display} High)"
            )
        }

        // Check Pair
        if (r1 == r2) {
            return EvaluatedHand(
                type = HandType.PAIR,
                primaryValue = r1,
                kicker = r3,
                description = "Pair of ${sorted[0].rank.display}s"
            )
        } else if (r2 == r3) {
            return EvaluatedHand(
                type = HandType.PAIR,
                primaryValue = r2,
                kicker = r1,
                description = "Pair of ${sorted[1].rank.display}s"
            )
        } else if (r1 == r3) {
            return EvaluatedHand(
                type = HandType.PAIR,
                primaryValue = r1,
                kicker = r2,
                description = "Pair of ${sorted[0].rank.display}s"
            )
        }

        // High Card
        return EvaluatedHand(
            type = HandType.HIGH_CARD,
            primaryValue = r1,
            secondaryValue = r2,
            kicker = r3,
            description = "High Card ${sorted[0].rank.display}"
        )
    }
}
