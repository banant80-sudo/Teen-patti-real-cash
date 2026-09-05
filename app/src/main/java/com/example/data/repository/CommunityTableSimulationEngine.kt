package com.example.data.repository

import com.example.data.db.AdminCommissionLogEntity
import com.example.data.model.GameEconomyRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CommunityTableEvent(
    val tableName: String,
    val winnerName: String,
    val potChips: Long,
    val commissionChips: Long,
    val commissionInr: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Simulates active background community Teen Patti tables.
 * "or log automatic apne aap khele ga commission mere wallet add hota jayega"
 * Automatically runs hands in real-time across active rooms and deposits the 5%
 * winning board commission directly into the Admin/Owner Wallet.
 */
class CommunityTableSimulationEngine(
    private val repository: GameRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var simulationJob: Job? = null

    private val _isRunning = MutableStateFlow(true)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _latestEvent = MutableStateFlow<CommunityTableEvent?>(null)
    val latestEvent: StateFlow<CommunityTableEvent?> = _latestEvent.asStateFlow()

    private val tables = listOf(
        TableSpec("Goa High Stakes Room", 60_000L, 250_000L),
        TableSpec("Mumbai Royal VIP Table", 120_000L, 500_000L),
        TableSpec("Delhi Aces Arena", 40_000L, 160_000L),
        TableSpec("Bangalore Sharks Club", 30_000L, 120_000L),
        TableSpec("Jaipur Heritage Palace", 50_000L, 220_000L),
        TableSpec("Kolkata High Rollers", 90_000L, 380_000L)
    )

    private val playerNames = listOf(
        "Kabir M.", "Vikramaditya S.", "Devendra R.", "Priya N.",
        "Sameer K.", "Ananya G.", "Deepak T.", "Siddharth B.",
        "Rajesh P.", "Meera J.", "Aakash V.", "Karan L.", "Suresh N."
    )

    data class TableSpec(val name: String, val minPot: Long, val maxPot: Long)

    fun start() {
        if (simulationJob != null && simulationJob?.isActive == true) return
        _isRunning.value = true

        simulationJob = scope.launch {
            // Initial delay before first background game finishes
            delay(3500)
            while (isActive && _isRunning.value) {
                try {
                    val table = tables[Random.nextInt(tables.size)]
                    val winner = playerNames[Random.nextInt(playerNames.size)]
                    // Generate realistic pot with round step (e.g. multiple of 1,000)
                    val rawPot = Random.nextLong(table.minPot, table.maxPot)
                    val pot = (rawPot / 1_000L) * 1_000L

                    // 5% House Commission: "Mera commission rahega per win board me 5%"
                    val commission = (pot * GameEconomyRules.BOARD_WIN_COMMISSION_PERCENT) / 100
                    val commissionInr = (commission / GameEconomyRules.CHIPS_PER_INR).toInt()

                    repository.creditBoardCommission(
                        tableName = table.name,
                        totalPot = pot,
                        commissionChips = commission,
                        winnerName = winner,
                        isAutoSimulated = true
                    )

                    _latestEvent.value = CommunityTableEvent(
                        tableName = table.name,
                        winnerName = winner,
                        potChips = pot,
                        commissionChips = commission,
                        commissionInr = commissionInr
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Next round completes in 4 to 7 seconds
                val nextDelay = Random.nextLong(4000, 7000)
                delay(nextDelay)
            }
        }
    }

    fun toggleSimulation(): Boolean {
        val newState = !_isRunning.value
        _isRunning.value = newState
        if (newState) {
            start()
        } else {
            simulationJob?.cancel()
            simulationJob = null
        }
        return newState
    }

    fun stop() {
        _isRunning.value = false
        simulationJob?.cancel()
        simulationJob = null
    }
}
