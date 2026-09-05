package com.example.data.repository

import com.example.audio.GameSoundManager
import com.example.data.model.AiDifficulty
import com.example.data.model.AiOpponentEngine
import com.example.data.model.AiTurnDecision
import com.example.data.model.Card
import com.example.data.model.ChatMessage
import com.example.data.model.EvaluatedHand
import com.example.data.model.GameEconomyRules
import com.example.data.model.HandType
import com.example.data.model.Player
import com.example.data.model.TableStake
import com.example.data.model.TeenPattiEvaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class GamePhase {
    IDLE,
    DEALING,
    IN_PROGRESS,
    SHOWDOWN,
    ROUND_OVER
}

data class TableUiState(
    val stake: TableStake,
    val phase: GamePhase = GamePhase.IDLE,
    val potAmount: Long = 0,
    val currentStake: Long = 0,
    val activePlayerIndex: Int = 0,
    val turnTimeRemaining: Int = 15,
    val players: List<Player> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val winnerPlayer: Player? = null,
    val winnerHand: EvaluatedHand? = null,
    val showSideShowDialog: Boolean = false,
    val sideShowTarget: Player? = null,
    val statusMessage: String = "Welcome to the table!",
    val tableAiDifficulty: AiDifficulty = AiDifficulty.ADVANCED,
    val isInGameRechargeActive: Boolean = false,
    val inGameRechargeSecondsRemaining: Int = 300,
    val commissionAmount: Long = 0,
    val netPayout: Long = 0,
    val fairPlayCertified: Boolean = true
)

class TeenPattiTableEngine(
    private val scope: CoroutineScope,
    private val repository: GameRepository,
    private val tableStake: TableStake,
    private val userPlayerName: String
) {
    private val _uiState = MutableStateFlow(
        TableUiState(
            stake = tableStake,
            currentStake = tableStake.bootAmount
        )
    )
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    private var turnJob: Job? = null
    private var inGameRechargeJob: Job? = null

    private val botProfiles = listOf(
        Triple("Aarav_Pro", "🦁", AiDifficulty.EXPERT),
        Triple("Priya_Ace", "💃", AiDifficulty.ADVANCED),
        Triple("Vikram_Blr", "⚡", AiDifficulty.CASUAL),
        Triple("Neha_Goa", "🌺", AiDifficulty.BEGINNER)
    )

    init {
        initializeTable()
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        val updatedPlayers = _uiState.value.players.map { player ->
            if (!player.isUser) {
                player.copy(aiDifficulty = difficulty)
            } else {
                player
            }
        }
        _uiState.update {
            it.copy(
                tableAiDifficulty = difficulty,
                players = updatedPlayers,
                statusMessage = "AI Difficulty set to ${difficulty.title} (${difficulty.badge})"
            )
        }
        addChatMessage("Dealer", "🤖 AI Difficulty changed to ${difficulty.title} (${difficulty.badge}). Fair play RNG active!", isSystem = true)
    }

    fun initializeTable() {
        val initialPlayers = mutableListOf<Player>()
        // User seat 0
        initialPlayers.add(
            Player(
                id = "user_0",
                name = userPlayerName,
                isUser = true,
                chips = 50000L,
                avatarEmoji = "👑",
                avatarBgColor = 0xFF1B5E20
            )
        )

        // 4 Online Bots with configured difficulty levels
        val colors = listOf(0xFFB71C1C, 0xFF0D47A1, 0xFFE65100, 0xFF4A148C)
        botProfiles.forEachIndexed { index, triple ->
            initialPlayers.add(
                Player(
                    id = "player_${index + 1}",
                    name = triple.first,
                    isUser = false,
                    chips = (tableStake.bootAmount * (40..150).random()),
                    avatarEmoji = triple.second,
                    avatarBgColor = colors[index % colors.size],
                    aiDifficulty = triple.third
                )
            )
        }

        _uiState.update {
            it.copy(
                players = initialPlayers,
                phase = GamePhase.IDLE,
                statusMessage = "Press Start Game to play with 4 AI players (Beginner to Expert)!"
            )
        }

        addChatMessage("Dealer", "Welcome to ${tableStake.title}! Boot is ₹${tableStake.bootAmount}. 🛡️ 100% Fair Play Guaranteed.", isSystem = true)
    }

    fun startNewRound() {
        turnJob?.cancel()
        GameSoundManager.playDealCard()
        val deck = TeenPattiEvaluator.createStandardDeck()

        val boot = tableStake.bootAmount
        var currentPot = 0L

        val updatedPlayers = _uiState.value.players.map { player ->
            val cards = listOf(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0))
            val effectiveChips = (player.chips - boot).coerceAtLeast(0)
            currentPot += boot
            player.copy(
                chips = effectiveChips,
                cards = cards,
                isBlind = true,
                isFolded = false,
                currentBet = boot,
                lastAction = "Boot ${boot}",
                reactionEmoji = null,
                isWinner = false
            )
        }

        // Deduct user chips in repository
        scope.launch {
            repository.deductChips(boot, "GAME_BET", "BOOT-${tableStake.id}")
        }

        _uiState.update {
            it.copy(
                phase = GamePhase.IN_PROGRESS,
                potAmount = currentPot,
                currentStake = boot,
                players = updatedPlayers,
                activePlayerIndex = 0,
                turnTimeRemaining = 15,
                winnerPlayer = null,
                winnerHand = null,
                statusMessage = "Cards dealt! Round in progress."
            )
        }

        addChatMessage("Dealer", "Boot collected from all players. Game on!", isSystem = true)
        startTurnTimer()
    }

    private fun startTurnTimer() {
        turnJob?.cancel()
        if (_uiState.value.isInGameRechargeActive) {
            // Turn timer paused for active in-game recharge
            return
        }

        turnJob = scope.launch {
            for (t in 15 downTo 0) {
                _uiState.update { it.copy(turnTimeRemaining = t) }
                delay(1000)
            }
            // Timeout: if active player timed out, fold or blind bet
            handleTurnTimeout()
        }

        val active = getActivePlayer()
        if (active != null && !active.isUser) {
            handleBotTurn(active)
        }
    }

    fun startInGameRecharge() {
        turnJob?.cancel()
        inGameRechargeJob?.cancel()
        _uiState.update {
            it.copy(
                isInGameRechargeActive = true,
                inGameRechargeSecondsRemaining = GameEconomyRules.IN_GAME_RECHARGE_RESERVE_SECONDS,
                statusMessage = "Seat reserved for 5:00 minutes while adding cash!"
            )
        }
        addChatMessage("Dealer", "⏱️ Seat reserved for 5:00 Minutes! Complete your deposit without timing out.", isSystem = true)

        inGameRechargeJob = scope.launch {
            for (sec in GameEconomyRules.IN_GAME_RECHARGE_RESERVE_SECONDS downTo 0) {
                _uiState.update { it.copy(inGameRechargeSecondsRemaining = sec) }
                delay(1000)
            }
            // 5 minutes timeout expired
            _uiState.update { it.copy(isInGameRechargeActive = false) }
            addChatMessage("Dealer", "5-minute recharge window ended. Game resumed.", isSystem = true)
            startTurnTimer()
        }
    }

    fun completeInGameRecharge(addedChips: Long, inrAmount: Int) {
        inGameRechargeJob?.cancel()
        val userIndex = _uiState.value.players.indexOfFirst { it.isUser }
        if (userIndex != -1) {
            val players = _uiState.value.players.toMutableList()
            val user = players[userIndex]
            players[userIndex] = user.copy(chips = user.chips + addedChips)
            _uiState.update {
                it.copy(
                    players = players,
                    isInGameRechargeActive = false,
                    statusMessage = "Added ₹$inrAmount (+${com.example.ui.components.formatChips(addedChips)}) to table seat!"
                )
            }
        } else {
            _uiState.update { it.copy(isInGameRechargeActive = false) }
        }
        addChatMessage("Dealer", "✅ In-Game Cash Added: ₹$inrAmount (+${com.example.ui.components.formatChips(addedChips)} chips). Table stack updated!", isSystem = true)
        startTurnTimer()
    }

    fun cancelInGameRecharge() {
        inGameRechargeJob?.cancel()
        _uiState.update { it.copy(isInGameRechargeActive = false) }
        startTurnTimer()
    }

    private fun handleTurnTimeout() {
        if (_uiState.value.isInGameRechargeActive) {
            // Do not fold user during 5-minute recharge window
            return
        }
        val active = getActivePlayer() ?: return
        if (active.isUser) {
            // User timed out -> Fold
            userFold()
        } else {
            // Bot timed out -> Fold
            botFold(active)
        }
    }

    fun seeCards() {
        val userIndex = _uiState.value.players.indexOfFirst { it.isUser }
        if (userIndex != -1) {
            val players = _uiState.value.players.toMutableList()
            val user = players[userIndex]
            if (user.isBlind) {
                players[userIndex] = user.copy(isBlind = false, lastAction = "Seen Cards")
                _uiState.update { it.copy(players = players) }
                addChatMessage("Dealer", "${user.name} saw their cards.", isSystem = true)
            }
        }
    }

    fun userBet(isChaal: Boolean, multiplier: Int = 1) {
        val userIndex = _uiState.value.players.indexOfFirst { it.isUser }
        if (userIndex != _uiState.value.activePlayerIndex) return

        val user = _uiState.value.players[userIndex]
        val baseStake = _uiState.value.currentStake
        // Blind pays 1x currentStake, Seen pays 2x currentStake
        val betAmount = if (user.isBlind) baseStake * multiplier else baseStake * 2 * multiplier
        val cappedBet = betAmount.coerceIn(tableStake.bootAmount, tableStake.maxChaal)

        val updatedChips = (user.chips - cappedBet).coerceAtLeast(0)
        val actionText = if (user.isBlind) "Blind +$cappedBet" else "Chaal +$cappedBet"

        scope.launch {
            repository.deductChips(cappedBet, "GAME_BET", "BET-${tableStake.id}")
        }

        val updatedPlayers = _uiState.value.players.toMutableList()
        updatedPlayers[userIndex] = user.copy(
            chips = updatedChips,
            currentBet = user.currentBet + cappedBet,
            lastAction = actionText
        )

        GameSoundManager.playBetChips()

        _uiState.update {
            it.copy(
                potAmount = it.potAmount + cappedBet,
                currentStake = if (user.isBlind) cappedBet else cappedBet / 2,
                players = updatedPlayers,
                statusMessage = "${user.name} played $actionText"
            )
        }

        advanceToNextPlayer()
    }

    fun userFold() {
        val userIndex = _uiState.value.players.indexOfFirst { it.isUser }
        if (userIndex != -1) {
            GameSoundManager.playFold()
            val updatedPlayers = _uiState.value.players.toMutableList()
            updatedPlayers[userIndex] = updatedPlayers[userIndex].copy(
                isFolded = true,
                lastAction = "Packed"
            )
            _uiState.update {
                it.copy(
                    players = updatedPlayers,
                    statusMessage = "You folded!"
                )
            }
            addChatMessage("Dealer", "${userPlayerName} folded.", isSystem = true)
            checkRoundConditions()
        }
    }

    fun userShow() {
        // Can only show if 2 players remaining
        val activePlayers = _uiState.value.players.filter { !it.isFolded }
        if (activePlayers.size == 2) {
            GameSoundManager.playShowdown()
            evaluateShowdown()
        }
    }

    private fun handleBotTurn(bot: Player) {
        scope.launch {
            // Realistic hesitation delay
            delay((1200L..2600L).random())

            val currentState = _uiState.value
            if (currentState.phase != GamePhase.IN_PROGRESS) return@launch
            if (currentState.activePlayerIndex != currentState.players.indexOf(bot)) return@launch

            val botIndex = currentState.players.indexOf(bot)
            if (bot.isFolded) {
                advanceToNextPlayer()
                return@launch
            }

            // 100% Fair RNG: Bot analyzes only its own 3 cards and table state
            var isBlind = bot.isBlind
            val turnNum = (bot.currentBet / tableStake.bootAmount).toInt().coerceAtLeast(1)
            if (isBlind && AiOpponentEngine.shouldSeeCards(
                    player = bot,
                    currentStake = currentState.currentStake,
                    potAmount = currentState.potAmount,
                    turnNumber = turnNum,
                    difficulty = bot.aiDifficulty
                )
            ) {
                isBlind = false
                addChatMessage("Dealer", "${bot.name} (${bot.aiDifficulty.badge}) inspected cards.", isSystem = true)
            }

            val activeCount = currentState.players.count { !it.isFolded }
            val decision = AiOpponentEngine.decideAction(
                player = bot.copy(isBlind = isBlind),
                currentStake = currentState.currentStake,
                potAmount = currentState.potAmount,
                activePlayersCount = activeCount,
                bootAmount = tableStake.bootAmount,
                maxChaal = tableStake.maxChaal,
                difficulty = bot.aiDifficulty
            )

            // Occasional in-game reaction or chat
            if (Math.random() > 0.72) {
                val eval = TeenPattiEvaluator.evaluate(bot.cards)
                postRandomBotChat(bot.name, eval.type.rankWeight)
            }

            when (decision) {
                is AiTurnDecision.Show -> {
                    GameSoundManager.playShowdown()
                    addChatMessage("Dealer", "${bot.name} asked for a SHOWDOWN!", isSystem = true)
                    evaluateShowdown()
                }
                is AiTurnDecision.Fold -> {
                    botFold(bot)
                }
                is AiTurnDecision.Bet -> {
                    val base = currentState.currentStake
                    val mult = decision.multiplier
                    val betAmount = if (decision.isBlind) base * mult else base * 2 * mult
                    val cappedBet = betAmount.coerceIn(tableStake.bootAmount, tableStake.maxChaal)

                    val updatedPlayers = currentState.players.toMutableList()
                    val actionName = if (decision.isBlind) {
                        if (mult > 1) "Blind +$cappedBet" else "Blind $cappedBet"
                    } else {
                        if (mult > 1) "Raised Chaal $cappedBet" else "Chaal $cappedBet"
                    }

                    val updatedBot = bot.copy(
                        isBlind = decision.isBlind,
                        chips = (bot.chips - cappedBet).coerceAtLeast(0),
                        currentBet = bot.currentBet + cappedBet,
                        lastAction = actionName
                    )
                    updatedPlayers[botIndex] = updatedBot

                    GameSoundManager.playBetChips()

                    _uiState.update {
                        it.copy(
                            potAmount = it.potAmount + cappedBet,
                            currentStake = if (decision.isBlind) cappedBet / mult else (cappedBet / 2) / mult,
                            players = updatedPlayers,
                            statusMessage = "${bot.name} (${bot.aiDifficulty.badge}): $actionName"
                        )
                    }

                    advanceToNextPlayer()
                }
            }
        }
    }

    private fun botFold(bot: Player) {
        val botIndex = _uiState.value.players.indexOf(bot)
        if (botIndex != -1) {
            GameSoundManager.playFold()
            val updatedPlayers = _uiState.value.players.toMutableList()
            updatedPlayers[botIndex] = bot.copy(isFolded = true, lastAction = "Packed")
            _uiState.update {
                it.copy(
                    players = updatedPlayers,
                    statusMessage = "${bot.name} folded."
                )
            }
            addChatMessage("Dealer", "${bot.name} folded.", isSystem = true)
            checkRoundConditions()
        }
    }

    private fun advanceToNextPlayer() {
        val players = _uiState.value.players
        var nextIndex = (_uiState.value.activePlayerIndex + 1) % players.size

        // Find next non-folded player
        var attempts = 0
        while (players[nextIndex].isFolded && attempts < players.size) {
            nextIndex = (nextIndex + 1) % players.size
            attempts++
        }

        checkRoundConditions(nextIndex)
    }

    private fun checkRoundConditions(forcedNextIndex: Int? = null) {
        val activePlayers = _uiState.value.players.filter { !it.isFolded }
        if (activePlayers.size <= 1) {
            // Single remaining player wins!
            val winner = activePlayers.firstOrNull() ?: _uiState.value.players.first()
            declareWinner(winner, null)
        } else {
            val next = forcedNextIndex ?: run {
                var idx = (_uiState.value.activePlayerIndex + 1) % _uiState.value.players.size
                while (_uiState.value.players[idx].isFolded) {
                    idx = (idx + 1) % _uiState.value.players.size
                }
                idx
            }
            _uiState.update { it.copy(activePlayerIndex = next) }
            startTurnTimer()
        }
    }

    private fun evaluateShowdown() {
        turnJob?.cancel()
        val activePlayers = _uiState.value.players.filter { !it.isFolded }

        var bestHand: EvaluatedHand? = null
        var bestPlayer: Player? = null

        for (player in activePlayers) {
            val hand = TeenPattiEvaluator.evaluate(player.cards)
            if (bestHand == null || hand > bestHand) {
                bestHand = hand
                bestPlayer = player
            }
        }

        declareWinner(bestPlayer ?: activePlayers.first(), bestHand)
    }

    private fun declareWinner(winner: Player, hand: EvaluatedHand?) {
        turnJob?.cancel()
        inGameRechargeJob?.cancel()
        GameSoundManager.playWinFanfare()
        val pot = _uiState.value.potAmount

        // 5% Board Commission: "Mera commission rahega per win board me 5%"
        val commission = (pot * GameEconomyRules.BOARD_WIN_COMMISSION_PERCENT) / 100
        val netPayout = pot - commission

        val updatedPlayers = _uiState.value.players.map {
            if (it.id == winner.id) {
                it.copy(
                    chips = it.chips + netPayout,
                    isWinner = true,
                    isBlind = false,
                    lastAction = "WINNER! +$netPayout"
                )
            } else {
                it.copy(isBlind = false)
            }
        }

        _uiState.update {
            it.copy(
                phase = GamePhase.ROUND_OVER,
                winnerPlayer = winner,
                winnerHand = hand,
                players = updatedPlayers,
                commissionAmount = commission,
                netPayout = netPayout,
                statusMessage = "${winner.name} won ₹$netPayout! (Pot: ₹$pot | 5% Board Commission: -₹$commission)"
            )
        }

        val desc = hand?.description ?: "Last player standing"
        addChatMessage(
            "Dealer",
            "🏆 ${winner.name} WON ₹$netPayout ($desc)! Total Pot: ₹$pot • 5% Board Comm: ₹$commission deducted",
            isSystem = true
        )

        // Record stats and user chips
        scope.launch {
            repository.recordGameFinished(
                userWon = winner.isUser,
                chipsWon = if (winner.isUser) netPayout else 0L,
                chipsBet = winner.currentBet,
                commissionChips = commission,
                totalPot = pot,
                tableName = tableStake.title,
                winnerName = winner.name
            )
        }
    }

    fun sendUserChat(text: String) {
        if (text.isBlank()) return
        addChatMessage(userPlayerName, text.trim(), isUser = true)

        // Random bot reply sometimes
        if (Math.random() > 0.4) {
            scope.launch {
                delay((1000L..2500L).random())
                val bot = botProfiles.random()
                val responses = listOf(
                    "Well played!", "Luck was on your side 👍", "Let's see the next round!",
                    "Chaal badhao!", "Khelte raho!", "Nice bluff haha", "Practice makes perfect!"
                )
                addChatMessage(bot.first, responses.random(), isUser = false)
            }
        }
    }

    fun sendUserReaction(emoji: String) {
        val userIndex = _uiState.value.players.indexOfFirst { it.isUser }
        if (userIndex != -1) {
            val players = _uiState.value.players.toMutableList()
            players[userIndex] = players[userIndex].copy(reactionEmoji = emoji)
            _uiState.update { it.copy(players = players) }

            // Clear after 3 seconds
            scope.launch {
                delay(3000)
                val p = _uiState.value.players.toMutableList()
                if (p.indices.contains(userIndex)) {
                    p[userIndex] = p[userIndex].copy(reactionEmoji = null)
                    _uiState.update { it.copy(players = p) }
                }
            }
        }
    }

    private fun postRandomBotChat(botName: String, handStrength: Int) {
        val botMessages = if (handStrength >= 4) {
            listOf("Bada hand hai mera! 🔥", "Koi Show karega?", "Chaal double karo bhai!", "Jackpot round lag raha hai!")
        } else {
            listOf("Dekhte hain cards kya boltay...", "Blind mein hi maza hai!", "Pack hone ka time aa gaya kya?", "Slow and steady...")
        }
        addChatMessage(botName, botMessages.random(), isUser = false)
    }

    private fun addChatMessage(sender: String, message: String, isSystem: Boolean = false, isUser: Boolean = false) {
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = sender,
            message = message,
            isSystem = isSystem,
            isUser = isUser
        )
        _uiState.update {
            it.copy(chatMessages = (it.chatMessages + newMsg).takeLast(40))
        }
    }

    private fun getActivePlayer(): Player? {
        val idx = _uiState.value.activePlayerIndex
        return _uiState.value.players.getOrNull(idx)
    }
}
