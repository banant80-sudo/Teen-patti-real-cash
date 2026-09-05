package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE user_profile SET chips = chips + :amount WHERE id = 1")
    suspend fun addChips(amount: Long)

    @Query("UPDATE user_profile SET chips = chips - :amount WHERE id = 1 AND chips >= :amount")
    suspend fun deductChips(amount: Long): Int

    @Query("UPDATE user_profile SET handsPlayed = handsPlayed + 1, handsWon = handsWon + :won, winStreak = CASE WHEN :won = 1 THEN winStreak + 1 ELSE 0 END WHERE id = 1")
    suspend fun recordGameResult(won: Int)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amountInr) FROM transactions WHERE type = 'WITHDRAWAL' AND timestamp >= :startOfDay")
    suspend fun getTodayWithdrawnInr(startOfDay: Long): Int?

    @Query("SELECT SUM(amountInr) FROM transactions WHERE type = 'WITHDRAWAL' AND timestamp >= :startOfDay")
    fun getTodayWithdrawnInrFlow(startOfDay: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
}

@Dao
interface DailyRewardDao {
    @Query("SELECT * FROM daily_rewards WHERE id = 1")
    fun getDailyRewardFlow(): Flow<DailyRewardEntity?>

    @Query("SELECT * FROM daily_rewards WHERE id = 1")
    suspend fun getDailyReward(): DailyRewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(reward: DailyRewardEntity)
}

@Dao
interface AdminWalletDao {
    @Query("SELECT * FROM admin_wallet WHERE id = 1")
    fun getAdminWalletFlow(): Flow<AdminWalletEntity?>

    @Query("SELECT * FROM admin_wallet WHERE id = 1")
    suspend fun getAdminWallet(): AdminWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(wallet: AdminWalletEntity)

    @Query("SELECT * FROM admin_commission_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommissionLogsFlow(limit: Int = 30): Flow<List<AdminCommissionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommissionLog(log: AdminCommissionLogEntity): Long
}

@Dao
interface DepositRequestDao {
    @Query("SELECT * FROM deposit_requests ORDER BY timestamp DESC")
    fun getAllDepositsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingDepositsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT COUNT(*) FROM deposit_requests WHERE status = 'PENDING'")
    fun getPendingDepositsCountFlow(): Flow<Int>

    @Query("SELECT * FROM deposit_requests WHERE id = :id")
    suspend fun getDepositById(id: Long): DepositRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deposit: DepositRequestEntity): Long

    @Update
    suspend fun update(deposit: DepositRequestEntity)
}

@Dao
interface WithdrawalRequestDao {
    @Query("SELECT * FROM withdrawal_requests ORDER BY timestamp DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE status = 'PENDING_VERIFICATION' ORDER BY timestamp DESC")
    fun getPendingWithdrawalsFlow(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT COUNT(*) FROM withdrawal_requests WHERE status = 'PENDING_VERIFICATION'")
    fun getPendingWithdrawalsCountFlow(): Flow<Int>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id")
    suspend fun getWithdrawalById(id: Long): WithdrawalRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(withdrawal: WithdrawalRequestEntity): Long

    @Update
    suspend fun update(withdrawal: WithdrawalRequestEntity)
}

@Dao
interface SupportComplaintDao {
    @Query("SELECT * FROM support_complaints ORDER BY timestamp DESC")
    fun getAllComplaintsFlow(): Flow<List<SupportComplaintEntity>>

    @Query("SELECT * FROM support_complaints WHERE status = 'OPEN' OR status = 'IN_REVIEW' ORDER BY timestamp DESC")
    fun getOpenComplaintsFlow(): Flow<List<SupportComplaintEntity>>

    @Query("SELECT COUNT(*) FROM support_complaints WHERE status = 'OPEN' OR status = 'IN_REVIEW'")
    fun getOpenComplaintsCountFlow(): Flow<Int>

    @Query("SELECT * FROM support_complaints WHERE id = :id")
    suspend fun getComplaintById(id: Long): SupportComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(complaint: SupportComplaintEntity): Long

    @Update
    suspend fun update(complaint: SupportComplaintEntity)
}

