package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Teen Patti King",
    val chips: Long = 50000L,
    val level: Int = 1,
    val handsPlayed: Int = 0,
    val handsWon: Int = 0,
    val winStreak: Int = 0,
    val avatarEmoji: String = "👑",
    val vipStatus: String = "Gold Member"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceId: String,
    val type: String, // "RECHARGE", "WITHDRAWAL", "GAME_WIN", "GAME_BET", "COMMISSION", "DAILY_REWARD", "SPIN_BONUS"
    val amountChips: Long,
    val amountInr: Int? = null,
    val feeInr: Int? = null,
    val netInr: Int? = null,
    val paymentMethod: String? = null,
    val status: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_rewards")
data class DailyRewardEntity(
    @PrimaryKey val id: Int = 1,
    val lastClaimTimestamp: Long = 0L,
    val currentStreakDay: Int = 0, // 0 means not started, 1..7
    val totalClaims: Int = 0,
    val lastSpinTimestamp: Long = 0L
)

@Entity(tableName = "admin_wallet")
data class AdminWalletEntity(
    @PrimaryKey val id: Int = 1,
    val totalCommissionChips: Long = 0L,
    val availableCommissionChips: Long = 0L,
    val totalCommissionInr: Int = 0,
    val availableCommissionInr: Int = 0,
    val totalWithdrawnInr: Int = 0,
    val totalGamesProcessed: Long = 0L,
    val isAutoSimulationActive: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_commission_logs")
data class AdminCommissionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceId: String,
    val tableName: String,
    val totalPotChips: Long,
    val commissionChips: Long,
    val commissionInr: Int,
    val winnerName: String,
    val isAutoSimulated: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceId: String,
    val userId: Int = 1,
    val userName: String = "Teen Patti King",
    val amountInr: Int,
    val chipsToCredit: Long,
    val paymentMethod: String,
    val utrNumber: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null,
    val adminNote: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val processedTimestamp: Long? = null
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceId: String,
    val userId: Int = 1,
    val userName: String = "Teen Patti King",
    val userChipsAtRequest: Long,
    val amountInr: Int,
    val feeInr: Int,
    val netInr: Int,
    val chipsDeducted: Long,
    val destinationType: String, // "UPI" or "BANK"
    val destinationDetails: String,
    val status: String = "PENDING_VERIFICATION", // PENDING_VERIFICATION, APPROVED_TRANSFERRED, REJECTED_REFUNDED
    val riskStatus: String = "VERIFIED_GENUINE", // VERIFIED_GENUINE, LOW_RISK, REVIEW_REQUIRED
    val fraudCheckNotes: String = "Passed automated sanity & gameplay pattern check. No bot telemetry detected.",
    val rejectionReason: String? = null,
    val utrTransferId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val processedTimestamp: Long? = null
)

@Entity(tableName = "support_complaints")
data class SupportComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: String,
    val userId: Int = 1,
    val userName: String = "Teen Patti King",
    val category: String, // "Add Cash / Deposit", "Withdrawal Status", "Table Disconnection", "Account / Other"
    val subject: String,
    val description: String,
    val referenceNo: String? = null,
    val contactInfo: String? = null,
    val status: String = "OPEN", // OPEN, IN_REVIEW, RESOLVED, REJECTED
    val resolutionNotes: String? = null,
    val bonusCompensationChips: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val resolvedTimestamp: Long? = null
)

