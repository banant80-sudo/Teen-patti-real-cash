package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AdminCommissionLogEntity
import com.example.data.db.AdminWalletEntity
import com.example.data.db.AppDatabase
import com.example.data.db.DailyRewardEntity
import com.example.data.db.DepositRequestEntity
import com.example.data.db.SupportComplaintEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UserEntity
import com.example.data.db.WithdrawalRequestEntity
import com.example.data.model.PaymentPack
import com.example.data.model.TableStake
import com.example.data.model.WithdrawalSummary
import com.example.data.repository.CommunityTableEvent
import com.example.data.repository.CommunityTableSimulationEngine
import com.example.data.repository.GameRepository
import com.example.data.repository.TeenPattiTableEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen {
    object Lobby : Screen()
    data class Table(val stake: TableStake) : Screen()
    object Store : Screen()
    object History : Screen()
    object HandRankings : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = GameRepository(db)
    val simulationEngine = CommunityTableSimulationEngine(repository, viewModelScope)

    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyRewardState: StateFlow<DailyRewardEntity?> = repository.dailyRewardFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactionsState: StateFlow<List<TransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWithdrawnInrState: StateFlow<Int> = repository.getTodayWithdrawnInrFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val adminWalletState: StateFlow<AdminWalletEntity?> = repository.adminWalletFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentCommissionLogsState: StateFlow<List<AdminCommissionLogEntity>> = repository.recentCommissionLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin & Support workflows
    val pendingDepositsState: StateFlow<List<DepositRequestEntity>> = repository.pendingDepositsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDepositsState: StateFlow<List<DepositRequestEntity>> = repository.allDepositsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingDepositsCount: StateFlow<Int> = repository.pendingDepositsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingWithdrawalsState: StateFlow<List<WithdrawalRequestEntity>> = repository.pendingWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWithdrawalsState: StateFlow<List<WithdrawalRequestEntity>> = repository.allWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingWithdrawalsCount: StateFlow<Int> = repository.pendingWithdrawalsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val openComplaintsState: StateFlow<List<SupportComplaintEntity>> = repository.openComplaintsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allComplaintsState: StateFlow<List<SupportComplaintEntity>> = repository.allComplaintsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val openComplaintsCount: StateFlow<Int> = repository.openComplaintsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isSimulationRunning: StateFlow<Boolean> = simulationEngine.isRunning
    val latestCommunityEvent: StateFlow<CommunityTableEvent?> = simulationEngine.latestEvent

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Lobby)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeTableEngine = MutableStateFlow<TeenPattiTableEngine?>(null)
    val activeTableEngine: StateFlow<TeenPattiTableEngine?> = _activeTableEngine.asStateFlow()

    // Dialog States
    private val _showDailyRewardsDialog = MutableStateFlow(false)
    val showDailyRewardsDialog: StateFlow<Boolean> = _showDailyRewardsDialog.asStateFlow()

    private val _showAdminPortalDialog = MutableStateFlow(false)
    val showAdminPortalDialog: StateFlow<Boolean> = _showAdminPortalDialog.asStateFlow()

    private val _showSupportDialog = MutableStateFlow(false)
    val showSupportDialog: StateFlow<Boolean> = _showSupportDialog.asStateFlow()

    private val _selectedStorePackForPayment = MutableStateFlow<PaymentPack?>(null)
    val selectedStorePackForPayment: StateFlow<PaymentPack?> = _selectedStorePackForPayment.asStateFlow()


    init {
        viewModelScope.launch {
            repository.ensureUserExists()
            repository.ensureAdminWalletExists()
            simulationEngine.start()
        }
    }

    fun openAdminPortal() {
        _showAdminPortalDialog.value = true
    }

    fun closeAdminPortal() {
        _showAdminPortalDialog.value = false
    }

    fun toggleSimulation(): Boolean {
        return simulationEngine.toggleSimulation()
    }

    fun withdrawAdminCommission(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String,
        onResult: (Result<WithdrawalSummary>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.withdrawAdminCommission(amountInr, destinationType, destinationDetails)
            onResult(result)
        }
    }


    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openDailyRewardsDialog() {
        _showDailyRewardsDialog.value = true
    }

    fun closeDailyRewardsDialog() {
        _showDailyRewardsDialog.value = false
    }

    fun openPaymentForPack(pack: PaymentPack) {
        _selectedStorePackForPayment.value = pack
    }

    fun closePayment() {
        _selectedStorePackForPayment.value = null
    }

    fun joinTable(stake: TableStake) {
        val userName = userState.value?.name ?: "Teen Patti King"
        val engine = TeenPattiTableEngine(
            scope = viewModelScope,
            repository = repository,
            tableStake = stake,
            userPlayerName = userName
        )
        _activeTableEngine.value = engine
        _currentScreen.value = Screen.Table(stake)
    }

    fun leaveTable() {
        _activeTableEngine.value = null
        _currentScreen.value = Screen.Lobby
    }

    fun claimDailyReward(day: Int, amount: Long) {
        viewModelScope.launch {
            repository.claimDailyReward(day, amount)
        }
    }

    fun claimSpinReward(amount: Long) {
        viewModelScope.launch {
            repository.claimSpinWheelReward(amount)
        }
    }

    fun openSupportDialog() {
        _showSupportDialog.value = true
    }

    fun closeSupportDialog() {
        _showSupportDialog.value = false
    }

    fun submitDepositRequest(
        pack: PaymentPack,
        paymentMethod: String,
        utrNumber: String,
        onResult: (Result<DepositRequestEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val totalChips = pack.chips + pack.bonusChips
            val result = repository.submitDepositRequest(
                amountInr = pack.priceInr,
                chipsToCredit = totalChips,
                paymentMethod = paymentMethod,
                utrNumber = utrNumber
            )
            onResult(result)
        }
    }

    fun approveDeposit(
        requestId: Long,
        adminNote: String? = null,
        onResult: (Result<DepositRequestEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.approveDepositRequest(requestId, adminNote)
            onResult(result)
        }
    }

    fun rejectDeposit(
        requestId: Long,
        reason: String,
        onResult: (Result<DepositRequestEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.rejectDepositRequest(requestId, reason)
            onResult(result)
        }
    }

    fun submitWithdrawal(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String,
        onResult: (Result<WithdrawalRequestEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitWithdrawalRequest(amountInr, destinationType, destinationDetails)
            onResult(result)
        }
    }

    fun approveWithdrawal(
        requestId: Long,
        utrTransferId: String,
        onResult: (Result<WithdrawalRequestEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.approveWithdrawalRequest(requestId, utrTransferId)
            onResult(result)
        }
    }

    fun rejectWithdrawal(
        requestId: Long,
        reason: String,
        onResult: (Result<WithdrawalRequestEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.rejectWithdrawalRequest(requestId, reason)
            onResult(result)
        }
    }

    fun submitSupportComplaint(
        category: String,
        subject: String,
        description: String,
        referenceNo: String?,
        contactInfo: String?,
        onResult: (Result<SupportComplaintEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.submitSupportComplaint(
                category = category,
                subject = subject,
                description = description,
                referenceNo = referenceNo,
                contactInfo = contactInfo
            )
            onResult(result)
        }
    }

    fun resolveSupportComplaint(
        complaintId: Long,
        notes: String,
        bonusChips: Long = 0L,
        onResult: (Result<SupportComplaintEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.resolveSupportComplaint(complaintId, notes, bonusChips)
            onResult(result)
        }
    }

    fun rejectSupportComplaint(
        complaintId: Long,
        reason: String,
        onResult: (Result<SupportComplaintEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.rejectSupportComplaint(complaintId, reason)
            onResult(result)
        }
    }

    fun completePayment(pack: PaymentPack, txnId: String, method: String) {
        viewModelScope.launch {
            val totalChips = pack.chips + pack.bonusChips
            repository.submitDepositRequest(
                amountInr = pack.priceInr,
                chipsToCredit = totalChips,
                paymentMethod = method,
                utrNumber = txnId
            )
        }
    }

    fun requestWithdrawal(
        amountInr: Int,
        destinationType: String,
        destinationDetails: String,
        onResult: (Result<WithdrawalSummary>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.processWithdrawal(amountInr, destinationType, destinationDetails)
            onResult(result)
        }
    }

    fun processInGameDeposit(
        amountInr: Int,
        chipsToAdd: Long,
        method: String,
        engine: TeenPattiTableEngine
    ) {
        viewModelScope.launch {
            val depositResult = repository.processDeposit(amountInr, method)
            if (depositResult.isSuccess) {
                engine.completeInGameRecharge(chipsToAdd, amountInr)
            } else {
                engine.cancelInGameRecharge()
            }
        }
    }
}
