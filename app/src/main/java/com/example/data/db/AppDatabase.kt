package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        DailyRewardEntity::class,
        AdminWalletEntity::class,
        AdminCommissionLogEntity::class,
        DepositRequestEntity::class,
        WithdrawalRequestEntity::class,
        SupportComplaintEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun dailyRewardDao(): DailyRewardDao
    abstract fun adminWalletDao(): AdminWalletDao
    abstract fun depositRequestDao(): DepositRequestDao
    abstract fun withdrawalRequestDao(): WithdrawalRequestDao
    abstract fun supportComplaintDao(): SupportComplaintDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teen_patti_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.userDao().insertOrUpdate(UserEntity())
                                database.dailyRewardDao().insertOrUpdate(DailyRewardEntity())
                                database.adminWalletDao().insertOrUpdate(
                                    AdminWalletEntity(
                                        id = 1,
                                        totalCommissionChips = 5000000L,
                                        availableCommissionChips = 5000000L,
                                        totalCommissionInr = 5000,
                                        availableCommissionInr = 5000,
                                        totalWithdrawnInr = 0,
                                        totalGamesProcessed = 142L
                                    )
                                )
                                database.transactionDao().insert(
                                    TransactionEntity(
                                        referenceId = "WELCOME-BONUS-01",
                                        type = "WELCOME_BONUS",
                                        amountChips = 50000L,
                                        status = "SUCCESS",
                                        timestamp = System.currentTimeMillis()
                                    )
                                )

                                // Seed sample pending deposit for Admin approval demo
                                database.depositRequestDao().insert(
                                    DepositRequestEntity(
                                        referenceId = "DEP-REQ-882194",
                                        userId = 1,
                                        userName = "Rajesh Kumar (VIP)",
                                        amountInr = 500,
                                        chipsToCredit = 575000L,
                                        paymentMethod = "UPI: PhonePe (to admin.patti@okhdfcbank)",
                                        utrNumber = "423891002341",
                                        status = "PENDING",
                                        timestamp = System.currentTimeMillis() - 15 * 60 * 1000L
                                    )
                                )

                                // Seed sample pending withdrawal for Admin verification demo
                                database.withdrawalRequestDao().insert(
                                    WithdrawalRequestEntity(
                                        referenceId = "WDRW-REQ-449102",
                                        userId = 1,
                                        userName = "Sunil Verma",
                                        userChipsAtRequest = 1200000L,
                                        amountInr = 1000,
                                        feeInr = 50,
                                        netInr = 950,
                                        chipsDeducted = 1000000L,
                                        destinationType = "UPI",
                                        destinationDetails = "sunil.v@okaxis",
                                        status = "PENDING_VERIFICATION",
                                        riskStatus = "VERIFIED_GENUINE",
                                        fraudCheckNotes = "Passed automated sanity check: 38 table hands, zero rapid dumping, matched IP locale. Safe to approve transfer.",
                                        timestamp = System.currentTimeMillis() - 8 * 60 * 1000L
                                    )
                                )

                                // Seed sample complaint for Admin resolution demo
                                database.supportComplaintDao().insert(
                                    SupportComplaintEntity(
                                        ticketId = "TCK-9921",
                                        userId = 1,
                                        userName = "Amit Patel",
                                        category = "Add Cash / Deposit",
                                        subject = "Paid ₹200 via Google Pay, waiting for chips credit",
                                        description = "I transferred ₹200 to admin UPI ID. UTR is 418293746192. Please verify and credit my 230,000 chips.",
                                        referenceNo = "UTR-418293746192",
                                        contactInfo = "+91 9876543210",
                                        status = "OPEN",
                                        timestamp = System.currentTimeMillis() - 25 * 60 * 1000L
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

