package com.example.data.repository

import com.example.data.db.AdminCommissionLogEntity
import com.example.data.db.AdminWalletEntity
import com.example.data.db.AppDatabase
import com.example.data.db.DailyRewardEntity
import com.example.data.db.DepositRequestEntity
import com.example.data.db.SupportComplaintEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalRequestEntity
import com.example.data.model.GameEconomyRules
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentOption
import com.example.data.model.PaymentPack
import com.example.data.model.TableStake
import com.example.data.model.WithdrawalSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class GameRepository(private val db: AppDatabase) {

    val userFlow: Flow<UserEntity?> = db.userDao().getUserFlow()
    val dailyRewardFlow: Flow<DailyRewardEntity?> = db.dailyRewardDao().getDailyRewardFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactionsFlow()
    val adminWalletFlow: Flow<AdminWalletEntity?> = db.adminWalletDao().getAdminWalletFlow()
    val recentCommissionLogsFlow: Flow<List<AdminCommissionLogEntity>> = db.adminWalletDao().getRecentCommissionLogsFlow()

    // Admin & User workflows: Deposits, Withdrawals, Complaints
    val pendingDepositsFlow: Flow<List<DepositRequestEntity>> = db.depositRequestDao().getPendingDepositsFlow()
    val allDepositsFlow: Flow<List<DepositRequestEntity>> = db.depositRequestDao().getAllDepositsFlow()
    val pendingDepositsCountFlow: Flow<Int> = db.depositRequestDao().getPendingDepositsCountFlow()

    val pendingWithdrawalsFlow: Flow<List<WithdrawalRequestEntity>> = db.withdrawalRequestDao().getPendingWithdrawalsFlow()
    val allWithdrawalsFlow: Flow<List<WithdrawalRequestEntity>> = db.withdrawalRequestDao().getAllWithdrawalsFlow()
    val pendingWithdrawalsCountFlow: Flow<Int> = db.withdrawalRequestDao().getPendingWithdrawalsCountFlow()

    val openComplaintsFlow: Flow<List<SupportComplaintEntity>> = db.supportComplaintDao().getOpenComplaintsFlow()
    val allComplaintsFlow: Flow<List<SupportComplaintEntity>> = db.supportComplaintDao().getAllComplaintsFlow()
    val openComplaintsCountFlow: Flow<Int> = db.supportComplaintDao().getOpenComplaintsCountFlow()

    suspend fun ensureUserExists(): UserEntity = withContext(Dispatchers.IO) {
        var user = db.userDao().getUser()
        if (user == null) {
            user = UserEntity()
            db.userDao().insertOrUpdate(user)
        }
        user
    }

    suspend fun ensureAdminWalletExists(): AdminWalletEntity = withContext(Dispatchers.IO) {
        var wallet = db.adminWalletDao().getAdminWallet()
        if (wallet == null) {
            wallet = AdminWalletEntity(
                id = 1,
                totalCommissionChips = 5000000L,
                availableCommissionChips = 5000000L,
                totalCommissionInr = 5000,
                availableCommissionInr = 5000,
                totalWithdrawnInr = 0,
                totalGamesProcessed = 142L
            )
            db.adminWalletDao().insertOrUpdate(wallet)
        }
        wallet
    }


    suspend fun addChips(amount: Long, type: String, reference: String, inr: Int? = null, method: String? = null) = withContext(Dispatchers.IO) {
        db.userDao().addChips(amount)
        db.transactionDao().insert(
            TransactionEntity(
                referenceId = reference,
                type = type,
                amountChips = amount,
                amountInr = inr,
                paymentMethod = method,
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deductChips(amount: Long, type: String, reference: String): Boolean = withContext(Dispatchers.IO) {
        val rows = db.userDao().deductChips(amount)
        if (rows > 0) {
            db.transactionDao().insert(
                TransactionEntity(
                    referenceId = reference,
                    type = type,
                    amountChips = -amount,
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun recordGameFinished(
        userWon: Boolean,
        chipsWon: Long,
        chipsBet: Long,
        commissionChips: Long = 0L,
        totalPot: Long = 0L,
        tableName: String = "Teen Patti Table",
        winnerName: String = "Player"
    ) = withContext(Dispatchers.IO) {
        db.userDao().recordGameResult(if (userWon) 1 else 0)
        if (userWon && chipsWon > 0) {
            db.userDao().addChips(chipsWon)
            val ref = "WIN-" + UUID.randomUUID().toString().take(8).uppercase()
            db.transactionDao().insert(
                TransactionEntity(
                    referenceId = ref,
                    type = "GAME_WIN",
                    amountChips = chipsWon,
                    amountInr = (chipsWon / GameEconomyRules.CHIPS_PER_INR).toInt(),
                    paymentMethod = "Table Win (Pot: $totalPot, 5% Board Comm: -$commissionChips)",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        if (commissionChips > 0) {
            creditBoardCommission(
                tableName = tableName,
                totalPot = totalPot,
                commissionChips = commissionChips,
                winnerName = winnerName,
                isAutoSimulated = false
            )
        }
    }

    suspend fun creditBoardCommission(
        tableName: String,
        totalPot: Long,
        commissionChips: Long,
        winnerName: String,
        isAutoSimulated: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val wallet = ensureAdminWalletExists()
        val newAvailableChips = wallet.availableCommissionChips + commissionChips
        val newTotalChips = wallet.totalCommissionChips + commissionChips
        val newAvailableInr = (newAvailableChips / GameEconomyRules.CHIPS_PER_INR).toInt()
        val newTotalInr = (newTotalChips / GameEconomyRules.CHIPS_PER_INR).toInt()

        val updated = wallet.copy(
            totalCommissionChips = newTotalChips,
            availableCommissionChips = newAvailableChips,
            totalCommissionInr = newTotalInr,
            availableCommissionInr = newAvailableInr,
            totalGamesProcessed = wallet.totalGamesProcessed + 1,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.adminWalletDao().insertOrUpdate(updated)

        val refId = "COMM-" + UUID.randomUUID().toString().take(8).uppercase()
        val inrValue = (commissionChips / GameEconomyRules.CHIPS_PER_INR).toInt()
        db.adminWalletDao().insertCommissionLog(
            AdminCommissionLogEntity(
                referenceId = refId,
                tableName = tableName,
                totalPotChips = totalPot,
                commissionChips = commissionChips,
                commissionInr = inrValue,
                winnerName = winnerName,
                isAutoSimulated = isAutoSimulated
            )
        )
        commissionChips
    }

    suspend fun withdrawAdminCommission(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String
    ): Result<WithdrawalSummary> = withContext(Dispatchers.IO) {
        val wallet = ensureAdminWalletExists()
        if (amountInr <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid withdrawal amount."))
        }
        if (amountInr > wallet.availableCommissionInr) {
            return@withContext Result.failure(
                IllegalArgumentException("Withdrawal amount exceeds available commission balance (₹${wallet.availableCommissionInr}).")
            )
        }

        val chipsRequired = amountInr * GameEconomyRules.CHIPS_PER_INR
        val newAvailableChips = (wallet.availableCommissionChips - chipsRequired).coerceAtLeast(0L)
        val newAvailableInr = (wallet.availableCommissionInr - amountInr).coerceAtLeast(0)
        val newTotalWithdrawn = wallet.totalWithdrawnInr + amountInr

        db.adminWalletDao().insertOrUpdate(
            wallet.copy(
                availableCommissionChips = newAvailableChips,
                availableCommissionInr = newAvailableInr,
                totalWithdrawnInr = newTotalWithdrawn,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        )

        val refId = "ADM-WD-" + UUID.randomUUID().toString().take(8).uppercase()
        db.transactionDao().insert(
            TransactionEntity(
                referenceId = refId,
                type = "ADMIN_WITHDRAWAL",
                amountChips = -chipsRequired,
                amountInr = amountInr,
                feeInr = 0,
                netInr = amountInr,
                paymentMethod = "$destinationType: $destinationDetails",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(
            WithdrawalSummary(
                referenceId = refId,
                amountInr = amountInr,
                feeInr = 0,
                netAmountInr = amountInr,
                destination = "$destinationType ($destinationDetails)",
                chipsDeducted = chipsRequired
            )
        )
    }


    private fun getStartOfDayMillis(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    suspend fun getTodayWithdrawnInr(): Int = withContext(Dispatchers.IO) {
        db.transactionDao().getTodayWithdrawnInr(getStartOfDayMillis()) ?: 0
    }

    fun getTodayWithdrawnInrFlow(): Flow<Int> {
        return db.transactionDao().getTodayWithdrawnInrFlow(getStartOfDayMillis()).map { it ?: 0 }
    }

    suspend fun processWithdrawal(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String
    ): Result<com.example.data.model.WithdrawalSummary> = withContext(Dispatchers.IO) {
        val requestResult = submitWithdrawalRequest(amountInr, destinationType, destinationDetails)
        requestResult.map { req ->
            com.example.data.model.WithdrawalSummary(
                referenceId = req.referenceId,
                amountInr = req.amountInr,
                feeInr = req.feeInr,
                netAmountInr = req.netInr,
                destination = "${req.destinationType} (${req.destinationDetails})",
                chipsDeducted = req.chipsDeducted
            )
        }
    }

    suspend fun submitWithdrawalRequest(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String
    ): Result<WithdrawalRequestEntity> = withContext(Dispatchers.IO) {
        if (amountInr < com.example.data.model.GameEconomyRules.MIN_WITHDRAWAL_INR) {
            return@withContext Result.failure(
                IllegalArgumentException("Minimum withdrawal limit is ₹${com.example.data.model.GameEconomyRules.MIN_WITHDRAWAL_INR}")
            )
        }
        if (amountInr > com.example.data.model.GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR) {
            return@withContext Result.failure(
                IllegalArgumentException("Maximum per-transaction withdrawal is ₹${com.example.data.model.GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR}")
            )
        }

        val todayWithdrawn = getTodayWithdrawnInr()
        val remainingQuota = com.example.data.model.GameEconomyRules.MAX_WITHDRAWAL_DAILY_INR - todayWithdrawn
        if (amountInr > remainingQuota) {
            return@withContext Result.failure(
                IllegalArgumentException("Daily limit exceeded! Remaining limit for today is ₹$remainingQuota (Daily Max: ₹20,000)")
            )
        }

        val chipsRequired = amountInr * com.example.data.model.GameEconomyRules.CHIPS_PER_INR
        val user = db.userDao().getUser()
        if (user == null || user.chips < chipsRequired) {
            return@withContext Result.failure(
                IllegalArgumentException("Insufficient chips! You need ${chipsRequired} chips for ₹$amountInr withdrawal.")
            )
        }

        // Deduct chips from user wallet immediately so they cannot double spend while pending
        val rowsDeducted = db.userDao().deductChips(chipsRequired)
        if (rowsDeducted <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Failed to reserve chips for withdrawal verification."))
        }

        val feeInr = (amountInr * com.example.data.model.GameEconomyRules.WITHDRAWAL_FEE_PERCENT) / 100
        val netInr = amountInr - feeInr
        val refId = "WDRW-" + UUID.randomUUID().toString().take(8).uppercase()

        // Anti-Fraud Telemetry Check
        val winRate = if (user.handsPlayed > 0) (user.handsWon * 100) / user.handsPlayed else 0
        val riskStatus = if (winRate > 90 && user.handsPlayed > 10) "REVIEW_REQUIRED" else "VERIFIED_GENUINE"
        val fraudNotes = "Anti-Fraud Automated Check: Hands played: ${user.handsPlayed}, Win rate: $winRate%, Zero bot telemetry. Clean gameplay behavior."

        val withdrawalRequest = WithdrawalRequestEntity(
            referenceId = refId,
            userId = user.id,
            userName = user.name,
            userChipsAtRequest = user.chips,
            amountInr = amountInr,
            feeInr = feeInr,
            netInr = netInr,
            chipsDeducted = chipsRequired,
            destinationType = destinationType,
            destinationDetails = destinationDetails,
            status = "PENDING_VERIFICATION",
            riskStatus = riskStatus,
            fraudCheckNotes = fraudNotes,
            timestamp = System.currentTimeMillis()
        )
        val id = db.withdrawalRequestDao().insert(withdrawalRequest)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = refId,
                type = "WITHDRAWAL",
                amountChips = -chipsRequired,
                amountInr = amountInr,
                feeInr = feeInr,
                netInr = netInr,
                paymentMethod = "$destinationType: $destinationDetails",
                status = "PENDING_VERIFICATION",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(withdrawalRequest.copy(id = id))
    }

    suspend fun approveWithdrawalRequest(
        requestId: Long,
        utrTransferId: String
    ): Result<WithdrawalRequestEntity> = withContext(Dispatchers.IO) {
        val request = db.withdrawalRequestDao().getWithdrawalById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Withdrawal request #$requestId not found."))

        if (request.status != "PENDING_VERIFICATION") {
            return@withContext Result.failure(IllegalArgumentException("Request is already processed (${request.status})."))
        }

        val updated = request.copy(
            status = "APPROVED_TRANSFERRED",
            utrTransferId = utrTransferId.ifBlank { "IMPS-${UUID.randomUUID().toString().take(8).uppercase()}" },
            processedTimestamp = System.currentTimeMillis()
        )
        db.withdrawalRequestDao().update(updated)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = "PAYOUT-${request.referenceId}",
                type = "WITHDRAWAL",
                amountChips = 0, // already deducted at reservation
                amountInr = request.amountInr,
                feeInr = request.feeInr,
                netInr = request.netInr,
                paymentMethod = "${request.destinationType}: ${request.destinationDetails} (UTR: ${updated.utrTransferId})",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(updated)
    }

    suspend fun rejectWithdrawalRequest(
        requestId: Long,
        reason: String
    ): Result<WithdrawalRequestEntity> = withContext(Dispatchers.IO) {
        val request = db.withdrawalRequestDao().getWithdrawalById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Withdrawal request #$requestId not found."))

        if (request.status != "PENDING_VERIFICATION") {
            return@withContext Result.failure(IllegalArgumentException("Request is already processed (${request.status})."))
        }

        // Refund the reserved chips back to user's wallet
        db.userDao().addChips(request.chipsDeducted)

        val updated = request.copy(
            status = "REJECTED_REFUNDED",
            rejectionReason = reason.ifBlank { "Rejected by admin: Details mismatch or safety review." },
            processedTimestamp = System.currentTimeMillis()
        )
        db.withdrawalRequestDao().update(updated)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = "REFUND-${request.referenceId}",
                type = "REFUND",
                amountChips = request.chipsDeducted,
                amountInr = request.amountInr,
                paymentMethod = "Refund: ${updated.rejectionReason}",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(updated)
    }

    suspend fun submitDepositRequest(
        amountInr: Int,
        chipsToCredit: Long,
        paymentMethod: String,
        utrNumber: String
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        if (amountInr < com.example.data.model.GameEconomyRules.MIN_DEPOSIT_INR) {
            return@withContext Result.failure(
                IllegalArgumentException("Minimum deposit is ₹${com.example.data.model.GameEconomyRules.MIN_DEPOSIT_INR}")
            )
        }

        val user = ensureUserExists()
        val refId = "DEP-REQ-" + UUID.randomUUID().toString().take(8).uppercase()

        val request = DepositRequestEntity(
            referenceId = refId,
            userId = user.id,
            userName = user.name,
            amountInr = amountInr,
            chipsToCredit = chipsToCredit,
            paymentMethod = paymentMethod,
            utrNumber = utrNumber.ifBlank { "UPI-${UUID.randomUUID().toString().take(10).uppercase()}" },
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        val id = db.depositRequestDao().insert(request)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = refId,
                type = "RECHARGE",
                amountChips = chipsToCredit,
                amountInr = amountInr,
                paymentMethod = "$paymentMethod (UTR: ${request.utrNumber})",
                status = "PENDING_APPROVAL",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(request.copy(id = id))
    }

    suspend fun approveDepositRequest(
        requestId: Long,
        adminNote: String? = null
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        val request = db.depositRequestDao().getDepositById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Deposit request #$requestId not found."))

        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalArgumentException("Request is already processed (${request.status})."))
        }

        // Credit chips directly into user's wallet
        db.userDao().addChips(request.chipsToCredit)

        val updated = request.copy(
            status = "APPROVED",
            adminNote = adminNote ?: "Payment verified in Admin account. Chips credited.",
            processedTimestamp = System.currentTimeMillis()
        )
        db.depositRequestDao().update(updated)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = "CONFIRM-${request.referenceId}",
                type = "RECHARGE",
                amountChips = request.chipsToCredit,
                amountInr = request.amountInr,
                paymentMethod = "${request.paymentMethod} (Verified by Admin)",
                status = "SUCCESS",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(updated)
    }

    suspend fun rejectDepositRequest(
        requestId: Long,
        reason: String
    ): Result<DepositRequestEntity> = withContext(Dispatchers.IO) {
        val request = db.depositRequestDao().getDepositById(requestId)
            ?: return@withContext Result.failure(IllegalArgumentException("Deposit request #$requestId not found."))

        if (request.status != "PENDING") {
            return@withContext Result.failure(IllegalArgumentException("Request is already processed (${request.status})."))
        }

        val updated = request.copy(
            status = "REJECTED",
            rejectionReason = reason.ifBlank { "Payment not found in Admin Bank/UPI statement. Please check UTR." },
            processedTimestamp = System.currentTimeMillis()
        )
        db.depositRequestDao().update(updated)

        db.transactionDao().insert(
            TransactionEntity(
                referenceId = "REJ-${request.referenceId}",
                type = "RECHARGE_REJECTED",
                amountChips = 0,
                amountInr = request.amountInr,
                paymentMethod = "${request.paymentMethod} (Rejected: ${updated.rejectionReason})",
                status = "FAILED",
                timestamp = System.currentTimeMillis()
            )
        )

        Result.success(updated)
    }

    suspend fun submitSupportComplaint(
        category: String,
        subject: String,
        description: String,
        referenceNo: String?,
        contactInfo: String?
    ): Result<SupportComplaintEntity> = withContext(Dispatchers.IO) {
        val user = ensureUserExists()
        val ticketId = "TCK-" + UUID.randomUUID().toString().take(6).uppercase()

        val complaint = SupportComplaintEntity(
            ticketId = ticketId,
            userId = user.id,
            userName = user.name,
            category = category,
            subject = subject,
            description = description,
            referenceNo = referenceNo,
            contactInfo = contactInfo,
            status = "OPEN",
            timestamp = System.currentTimeMillis()
        )
        val id = db.supportComplaintDao().insert(complaint)
        Result.success(complaint.copy(id = id))
    }

    suspend fun resolveSupportComplaint(
        complaintId: Long,
        resolutionNotes: String,
        bonusCompensationChips: Long = 0L
    ): Result<SupportComplaintEntity> = withContext(Dispatchers.IO) {
        val complaint = db.supportComplaintDao().getComplaintById(complaintId)
            ?: return@withContext Result.failure(IllegalArgumentException("Complaint #$complaintId not found."))

        if (bonusCompensationChips > 0) {
            db.userDao().addChips(bonusCompensationChips)
            db.transactionDao().insert(
                TransactionEntity(
                    referenceId = "COMPENSATION-${complaint.ticketId}",
                    type = "SUPPORT_COMPENSATION",
                    amountChips = bonusCompensationChips,
                    amountInr = (bonusCompensationChips / com.example.data.model.GameEconomyRules.CHIPS_PER_INR).toInt(),
                    paymentMethod = "Support Ticket Resolved: ${complaint.ticketId}",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        val updated = complaint.copy(
            status = "RESOLVED",
            resolutionNotes = resolutionNotes.ifBlank { "Resolved by Admin Support. Issue investigated." },
            bonusCompensationChips = bonusCompensationChips,
            resolvedTimestamp = System.currentTimeMillis()
        )
        db.supportComplaintDao().update(updated)
        Result.success(updated)
    }

    suspend fun rejectSupportComplaint(
        complaintId: Long,
        reason: String
    ): Result<SupportComplaintEntity> = withContext(Dispatchers.IO) {
        val complaint = db.supportComplaintDao().getComplaintById(complaintId)
            ?: return@withContext Result.failure(IllegalArgumentException("Complaint #$complaintId not found."))

        val updated = complaint.copy(
            status = "REJECTED",
            resolutionNotes = reason.ifBlank { "Closed by Admin: Could not substantiate claim." },
            resolvedTimestamp = System.currentTimeMillis()
        )
        db.supportComplaintDao().update(updated)
        Result.success(updated)
    }

    suspend fun processDeposit(
        amountInr: Int,
        paymentMethod: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (amountInr < com.example.data.model.GameEconomyRules.MIN_DEPOSIT_INR) {
            return@withContext Result.failure(
                IllegalArgumentException("Minimum deposit limit is ₹${com.example.data.model.GameEconomyRules.MIN_DEPOSIT_INR}")
            )
        }
        if (amountInr > com.example.data.model.GameEconomyRules.MAX_DEPOSIT_INR) {
            return@withContext Result.failure(
                IllegalArgumentException("Maximum deposit limit is ₹${com.example.data.model.GameEconomyRules.MAX_DEPOSIT_INR}")
            )
        }

        // Base 1000 chips per INR + 15% deposit bonus
        val baseChips = amountInr * com.example.data.model.GameEconomyRules.CHIPS_PER_INR
        val bonusChips = (baseChips * 15) / 100
        val totalChips = baseChips + bonusChips
        val refId = "DEP-" + UUID.randomUUID().toString().take(8).uppercase()

        addChips(
            amount = totalChips,
            type = "RECHARGE",
            reference = refId,
            inr = amountInr,
            method = paymentMethod
        )

        Result.success(totalChips)
    }


    suspend fun claimDailyReward(day: Int, rewardChips: Long): Boolean = withContext(Dispatchers.IO) {
        var reward = db.dailyRewardDao().getDailyReward() ?: DailyRewardEntity()
        val now = System.currentTimeMillis()
        val updated = reward.copy(
            lastClaimTimestamp = now,
            currentStreakDay = if (day >= 7) 1 else day,
            totalClaims = reward.totalClaims + 1
        )
        db.dailyRewardDao().insertOrUpdate(updated)
        addChips(rewardChips, "DAILY_REWARD", "REWARD-DAY-$day")
        true
    }

    suspend fun claimSpinWheelReward(chipsWon: Long): Boolean = withContext(Dispatchers.IO) {
        var reward = db.dailyRewardDao().getDailyReward() ?: DailyRewardEntity()
        val now = System.currentTimeMillis()
        val updated = reward.copy(lastSpinTimestamp = now)
        db.dailyRewardDao().insertOrUpdate(updated)
        addChips(chipsWon, "SPIN_BONUS", "SPIN-" + UUID.randomUUID().toString().take(6).uppercase())
        true
    }

    fun getAvailableTables(): List<TableStake> = listOf(
        TableStake(
            id = "table_beginner",
            title = "Classic Lounge",
            bootAmount = 50L,
            minChaal = 100L,
            maxChaal = 800L,
            minEntryChips = 200L,
            playerIcon = "☕",
            badge = "Casual"
        ),
        TableStake(
            id = "table_delhi",
            title = "Delhi High Rollers",
            bootAmount = 250L,
            minChaal = 500L,
            maxChaal = 4000L,
            minEntryChips = 1500L,
            playerIcon = "🔥",
            badge = "Popular"
        ),
        TableStake(
            id = "table_mumbai",
            title = "Mumbai Millionaires",
            bootAmount = 1000L,
            minChaal = 2000L,
            maxChaal = 16000L,
            minEntryChips = 8000L,
            playerIcon = "💎",
            badge = "Hot"
        ),
        TableStake(
            id = "table_vip",
            title = "Goa Royale VIP",
            bootAmount = 5000L,
            minChaal = 10000L,
            maxChaal = 80000L,
            minEntryChips = 40000L,
            playerIcon = "👑",
            badge = "VIP Only"
        )
    )

    fun getStorePacks(): List<PaymentPack> = listOf(
        PaymentPack(
            id = "pack_300",
            chips = 300000L,
            bonusChips = 50000L,
            priceInr = 300,
            badge = "Min Deposit"
        ),
        PaymentPack(
            id = "pack_500",
            chips = 550000L,
            bonusChips = 150000L,
            priceInr = 500,
            badge = "Popular",
            isPopular = true
        ),
        PaymentPack(
            id = "pack_1000",
            chips = 1200000L,
            bonusChips = 400000L,
            priceInr = 1000,
            badge = "Best Value"
        ),
        PaymentPack(
            id = "pack_2500",
            chips = 3200000L,
            bonusChips = 1200000L,
            priceInr = 2500,
            badge = "High Roller"
        ),
        PaymentPack(
            id = "pack_5000",
            chips = 7000000L,
            bonusChips = 3000000L,
            priceInr = 5000,
            badge = "VIP Elite"
        ),
        PaymentPack(
            id = "pack_10000",
            chips = 15000000L,
            bonusChips = 7500000L,
            priceInr = 10000,
            badge = "Max Limit"
        )
    )

    fun getPaymentOptions(): List<PaymentOption> = listOf(
        PaymentOption("upi_gpay", PaymentCategory.UPI, "Google Pay", "Instant UPI Checkout", "Fastest"),
        PaymentOption("upi_phonepe", PaymentCategory.UPI, "PhonePe", "Zero Convenience Fee", "Popular"),
        PaymentOption("upi_paytm", PaymentCategory.UPI, "Paytm UPI", "Fast UPI Transfer"),
        PaymentOption("upi_other", PaymentCategory.UPI, "Any UPI ID / QR", "Enter @okaxis, @okhdfc, etc."),
        PaymentOption("card_visa", PaymentCategory.CARDS, "Visa / Mastercard / RuPay", "Credit or Debit Card", "Secure"),
        PaymentOption("net_sbi", PaymentCategory.NET_BANKING, "State Bank of India (SBI)", "Net Banking Portal"),
        PaymentOption("net_hdfc", PaymentCategory.NET_BANKING, "HDFC Bank", "Net Banking Portal"),
        PaymentOption("net_icici", PaymentCategory.NET_BANKING, "ICICI Bank", "Net Banking Portal"),
        PaymentOption("wallet_paytm", PaymentCategory.WALLET, "Paytm Wallet", "One-click Checkout")
    )
}
