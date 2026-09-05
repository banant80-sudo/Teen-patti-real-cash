package com.example.data.model

import kotlin.random.Random

/**
 * 100% Fair, Certified RNG AI Opponent Decision Engine for Teen Patti.
 *
 * NO CHEATING / ZERO CARD PEEKING:
 * The AI ONLY analyzes its own 3 cards, the current pot, table stakes,
 * and game position. It has zero knowledge of opponents' cards or the undealt deck.
 *
 * Difficulty levels:
 * - BEGINNER: Relaxed, high blind rate, rarely raises, folds easily to pressure (great for practice).
 * - CASUAL: Standard table player, checks cards after 1-2 turns, raises on sequence/color, 10% bluff.
 * - ADVANCED: Pot-odds calculator, aggressive on strong hands, semi-bluffs, folds weak high cards.
 * - EXPERT: Master-level combinatorial Teen Patti player, calculated deception, optimal showdown timing.
 */
sealed class AiTurnDecision {
    object Fold : AiTurnDecision()
    data class Bet(val isBlind: Boolean, val multiplier: Int = 1) : AiTurnDecision()
    object Show : AiTurnDecision()
}

object AiOpponentEngine {

    /**
     * Decides whether the bot should view its cards or stay blind.
     */
    fun shouldSeeCards(
        player: Player,
        currentStake: Long,
        potAmount: Long,
        turnNumber: Int,
        difficulty: AiDifficulty
    ): Boolean {
        if (!player.isBlind) return false // already seen

        return when (difficulty) {
            AiDifficulty.BEGINNER -> {
                // Stays blind randomly for 1-4 turns
                turnNumber >= 3 || Random.nextFloat() > 0.6f
            }
            AiDifficulty.CASUAL -> {
                // Stays blind for 1-2 turns
                turnNumber >= 2 || Random.nextFloat() > 0.5f
            }
            AiDifficulty.ADVANCED -> {
                // If stakes are rising relative to chips, look at cards
                if (currentStake * 2 > player.chips * 0.1f) true
                else turnNumber >= 2 || Random.nextFloat() > 0.45f
            }
            AiDifficulty.EXPERT -> {
                // Expert stays blind up to 3 turns if pot is small to induce cheaper chaals,
                // but checks when pot grows or player count drops to 2
                if (potAmount > currentStake * 8) true
                else turnNumber >= 3 || Random.nextFloat() > 0.4f
            }
        }
    }

    /**
     * Decides the next action (Fold, Bet, or Show) for an AI player.
     */
    fun decideAction(
        player: Player,
        currentStake: Long,
        potAmount: Long,
        activePlayersCount: Int,
        bootAmount: Long,
        maxChaal: Long,
        difficulty: AiDifficulty
    ): AiTurnDecision {
        val isBlind = player.isBlind
        val evaluatedHand = TeenPattiEvaluator.evaluate(player.cards)
        val handRank = evaluatedHand.type.rankWeight // 1 (High Card) to 6 (Trail)
        val highCardVal = evaluatedHand.primaryValue

        // If 2 players left, consider Show
        if (activePlayersCount == 2) {
            val shouldShow = when (difficulty) {
                AiDifficulty.BEGINNER -> {
                    // Requests show randomly on pair or better
                    handRank >= HandType.PAIR.rankWeight && Random.nextFloat() < 0.6f
                }
                AiDifficulty.CASUAL -> {
                    handRank >= HandType.COLOR.rankWeight || (handRank >= HandType.PAIR.rankWeight && Random.nextFloat() < 0.7f)
                }
                AiDifficulty.ADVANCED -> {
                    // Show when holding medium/strong hand or pot is large
                    handRank >= HandType.SEQUENCE.rankWeight || (handRank >= HandType.PAIR.rankWeight && potAmount > currentStake * 6)
                }
                AiDifficulty.EXPERT -> {
                    // Optimal showdown timing based on pot odds
                    if (handRank >= HandType.SEQUENCE.rankWeight) {
                        // Very strong hand: might raise to extract value or call show
                        Random.nextFloat() < 0.55f
                    } else if (handRank == HandType.PAIR.rankWeight) {
                        // Show if pair is high (Queen/King/Ace)
                        highCardVal >= 12 || Random.nextFloat() < 0.6f
                    } else {
                        // High card: fold if pot requires big call, otherwise bluff show
                        Random.nextFloat() < 0.2f
                    }
                }
            }

            if (shouldShow) {
                return AiTurnDecision.Show
            }
        }

        // Decision logic while playing Blind
        if (isBlind) {
            val blindFoldChance = when (difficulty) {
                AiDifficulty.BEGINNER -> 0.05f
                AiDifficulty.CASUAL -> 0.08f
                AiDifficulty.ADVANCED -> 0.12f
                AiDifficulty.EXPERT -> 0.15f // Knows when to stop blind if table is aggressive
            }
            if (Random.nextFloat() < blindFoldChance && currentStake > bootAmount * 3) {
                return AiTurnDecision.Fold
            }
            // Blind bet
            return AiTurnDecision.Bet(isBlind = true, multiplier = 1)
        }

        // Decision logic for Seen Cards
        return when (difficulty) {
            AiDifficulty.BEGINNER -> decideBeginnerAction(handRank)
            AiDifficulty.CASUAL -> decideCasualAction(handRank, highCardVal, currentStake, potAmount)
            AiDifficulty.ADVANCED -> decideAdvancedAction(handRank, highCardVal, currentStake, potAmount, activePlayersCount)
            AiDifficulty.EXPERT -> decideExpertAction(handRank, highCardVal, currentStake, potAmount, activePlayersCount)
        }
    }

    private fun decideBeginnerAction(handRank: Int): AiTurnDecision {
        // Beginner: plays straightforward. Folds weak high card. Never bluffs.
        return when {
            handRank >= HandType.SEQUENCE.rankWeight -> AiTurnDecision.Bet(isBlind = false, multiplier = 1)
            handRank >= HandType.PAIR.rankWeight -> {
                if (Random.nextFloat() < 0.85f) AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                else AiTurnDecision.Fold
            }
            else -> {
                // High Card: 75% fold
                if (Random.nextFloat() < 0.75f) AiTurnDecision.Fold
                else AiTurnDecision.Bet(isBlind = false, multiplier = 1)
            }
        }
    }

    private fun decideCasualAction(
        handRank: Int,
        highCardVal: Int,
        currentStake: Long,
        potAmount: Long
    ): AiTurnDecision {
        return when {
            handRank >= HandType.PURE_SEQUENCE.rankWeight -> {
                val mult = if (Random.nextFloat() < 0.5f) 2 else 1
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank >= HandType.SEQUENCE.rankWeight -> {
                val mult = if (Random.nextFloat() < 0.35f) 2 else 1
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank == HandType.COLOR.rankWeight -> {
                AiTurnDecision.Bet(isBlind = false, multiplier = 1)
            }
            handRank == HandType.PAIR.rankWeight -> {
                if (Random.nextFloat() < 0.85f) AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                else AiTurnDecision.Fold
            }
            else -> {
                // High Card: 10% bluff chance, otherwise folds under Queen
                val isBluff = Random.nextFloat() < 0.10f
                if (isBluff || highCardVal >= 13) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else {
                    AiTurnDecision.Fold
                }
            }
        }
    }

    private fun decideAdvancedAction(
        handRank: Int,
        highCardVal: Int,
        currentStake: Long,
        potAmount: Long,
        activePlayersCount: Int
    ): AiTurnDecision {
        val potOdds = if (potAmount > 0) (currentStake * 2).toFloat() / potAmount.toFloat() else 0.5f

        return when {
            handRank == HandType.TRAIL.rankWeight -> {
                // Monster hand: raise aggressively (2x)
                AiTurnDecision.Bet(isBlind = false, multiplier = 2)
            }
            handRank >= HandType.SEQUENCE.rankWeight -> {
                val mult = if (Random.nextFloat() < 0.6f) 2 else 1
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank == HandType.COLOR.rankWeight -> {
                AiTurnDecision.Bet(isBlind = false, multiplier = 1)
            }
            handRank == HandType.PAIR.rankWeight -> {
                // High pair continues, low pair checks pot odds
                if (highCardVal >= 10 || potOdds < 0.3f) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else {
                    if (Random.nextFloat() < 0.4f) AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                    else AiTurnDecision.Fold
                }
            }
            else -> {
                // High Card: Semi-bluff (18% chance) or Fold
                val canBluff = activePlayersCount <= 3 && Random.nextFloat() < 0.18f
                if (canBluff) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = if (Random.nextFloat() < 0.3f) 2 else 1)
                } else if (highCardVal == 14 && potOdds < 0.15f) { // Ace high with cheap pot odds
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else {
                    AiTurnDecision.Fold
                }
            }
        }
    }

    private fun decideExpertAction(
        handRank: Int,
        highCardVal: Int,
        currentStake: Long,
        potAmount: Long,
        activePlayersCount: Int
    ): AiTurnDecision {
        val potOdds = if (potAmount > 0) (currentStake * 2).toFloat() / potAmount.toFloat() else 0.5f

        return when {
            handRank == HandType.TRAIL.rankWeight -> {
                // Slow-play trap (1x) or value raise (2x)
                val mult = if (activePlayersCount > 2 && Random.nextFloat() < 0.4f) 1 else 2
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank >= HandType.PURE_SEQUENCE.rankWeight -> {
                AiTurnDecision.Bet(isBlind = false, multiplier = 2)
            }
            handRank >= HandType.SEQUENCE.rankWeight -> {
                val mult = if (Random.nextFloat() < 0.7f) 2 else 1
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank == HandType.COLOR.rankWeight -> {
                val mult = if (activePlayersCount <= 2 && Random.nextFloat() < 0.5f) 2 else 1
                AiTurnDecision.Bet(isBlind = false, multiplier = mult)
            }
            handRank == HandType.PAIR.rankWeight -> {
                // Expert evaluates pair height and opponent density
                if (highCardVal >= 11) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else if (activePlayersCount <= 2) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else if (potOdds < 0.25f) {
                    AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                } else {
                    if (Random.nextFloat() < 0.3f) AiTurnDecision.Bet(isBlind = false, multiplier = 1)
                    else AiTurnDecision.Fold
                }
            }
            else -> {
                // High card: Master deception bluff (22% frequency, high pressure)
                val isDeceptionBluff = activePlayersCount <= 2 && Random.nextFloat() < 0.22f
                if (isDeceptionBluff) {
                    val mult = if (Random.nextFloat() < 0.45f) 2 else 1
                    AiTurnDecision.Bet(isBlind = false, multiplier = mult)
                } else {
                    // Disciplined fold to prevent chip leak
                    AiTurnDecision.Fold
                }
            }
        }
    }
}
