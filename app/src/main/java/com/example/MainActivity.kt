package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AdminCommissionPortalDialog
import com.example.ui.components.DailyRewardsDialog
import com.example.ui.components.PaymentGatewaySheet
import com.example.ui.components.UserSupportComplaintDialog
import com.example.ui.screens.GameTableScreen
import com.example.ui.screens.HandRankingsScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    TeenPattiApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeenPattiApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val user by viewModel.userState.collectAsStateWithLifecycle()
    val dailyReward by viewModel.dailyRewardState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val todayWithdrawnInr by viewModel.todayWithdrawnInrState.collectAsStateWithLifecycle()
    val activeTableEngine by viewModel.activeTableEngine.collectAsStateWithLifecycle()

    val adminWallet by viewModel.adminWalletState.collectAsStateWithLifecycle()
    val recentCommissionLogs by viewModel.recentCommissionLogsState.collectAsStateWithLifecycle()
    val isSimulationRunning by viewModel.isSimulationRunning.collectAsStateWithLifecycle()
    val latestCommunityEvent by viewModel.latestCommunityEvent.collectAsStateWithLifecycle()
    val showAdminPortal by viewModel.showAdminPortalDialog.collectAsStateWithLifecycle()

    val pendingDeposits by viewModel.pendingDepositsState.collectAsStateWithLifecycle()
    val allDeposits by viewModel.allDepositsState.collectAsStateWithLifecycle()
    val pendingWithdrawals by viewModel.pendingWithdrawalsState.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawalsState.collectAsStateWithLifecycle()
    val openComplaints by viewModel.openComplaintsState.collectAsStateWithLifecycle()
    val allComplaints by viewModel.allComplaintsState.collectAsStateWithLifecycle()
    val showSupportDialog by viewModel.showSupportDialog.collectAsStateWithLifecycle()

    val pendingAdminCount = pendingDeposits.size + pendingWithdrawals.size + openComplaints.size

    val showDailyRewards by viewModel.showDailyRewardsDialog.collectAsStateWithLifecycle()
    val selectedPaymentPack by viewModel.selectedStorePackForPayment.collectAsStateWithLifecycle()
    val paymentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Lobby -> {
                    LobbyScreen(
                        user = user,
                        tables = viewModel.repository.getAvailableTables(),
                        adminWallet = adminWallet,
                        latestCommunityEvent = latestCommunityEvent,
                        onJoinTable = { viewModel.joinTable(it) },
                        onOpenDailyRewards = { viewModel.openDailyRewardsDialog() },
                        onOpenStore = { viewModel.navigateTo(Screen.Store) },
                        onOpenHistory = { viewModel.navigateTo(Screen.History) },
                        onOpenHandRankings = { viewModel.navigateTo(Screen.HandRankings) },
                        onOpenAdminPortal = { viewModel.openAdminPortal() },
                        pendingAdminCount = pendingAdminCount,
                        onOpenSupport = { viewModel.openSupportDialog() }
                    )
                }

                is Screen.Table -> {
                    if (activeTableEngine != null) {
                        GameTableScreen(
                            engine = activeTableEngine!!,
                            onLeaveTable = { viewModel.leaveTable() },
                            onOpenStore = { viewModel.navigateTo(Screen.Store) },
                            onInGameDeposit = { amountInr, chips, method ->
                                viewModel.processInGameDeposit(amountInr, chips, method, activeTableEngine!!)
                            }
                        )
                    } else {
                        // Fallback to lobby
                        viewModel.navigateTo(Screen.Lobby)
                    }
                }

                is Screen.Store -> {
                    StoreScreen(
                        user = user,
                        packs = viewModel.repository.getStorePacks(),
                        todayWithdrawnInr = todayWithdrawnInr,
                        onSelectPack = { viewModel.openPaymentForPack(it) },
                        onRequestWithdrawal = { inr, type, dest, onResult ->
                            viewModel.requestWithdrawal(inr, type, dest, onResult)
                        },
                        onBack = { viewModel.navigateTo(Screen.Lobby) }
                    )
                }

                is Screen.History -> {
                    TransactionHistoryScreen(
                        transactions = transactions,
                        onBack = { viewModel.navigateTo(Screen.Lobby) }
                    )
                }

                is Screen.HandRankings -> {
                    HandRankingsScreen(
                        onBack = { viewModel.navigateTo(Screen.Lobby) }
                    )
                }
            }
        }

        // Daily Rewards & Lucky Wheel Dialog
        if (showDailyRewards) {
            DailyRewardsDialog(
                rewardEntity = dailyReward,
                onClaimStreak = { day, amount ->
                    viewModel.claimDailyReward(day, amount)
                },
                onSpinWheel = { amount ->
                    viewModel.claimSpinReward(amount)
                },
                onDismiss = { viewModel.closeDailyRewardsDialog() }
            )
        }

        // Secure Payment Gateway Bottom Sheet
        if (selectedPaymentPack != null) {
            PaymentGatewaySheet(
                sheetState = paymentSheetState,
                pack = selectedPaymentPack!!,
                paymentOptions = viewModel.repository.getPaymentOptions(),
                onPaymentCompleted = { pack, txnId, method ->
                    viewModel.completePayment(pack, txnId, method)
                },
                onDismiss = { viewModel.closePayment() }
            )
        }

        // Admin Commission & Request Management Dialog
        if (showAdminPortal) {
            AdminCommissionPortalDialog(
                adminWallet = adminWallet,
                recentLogs = recentCommissionLogs,
                latestEvent = latestCommunityEvent,
                isSimulationRunning = isSimulationRunning,
                pendingDeposits = pendingDeposits,
                allDeposits = allDeposits,
                pendingWithdrawals = pendingWithdrawals,
                allWithdrawals = allWithdrawals,
                openComplaints = openComplaints,
                allComplaints = allComplaints,
                onToggleSimulation = { viewModel.toggleSimulation() },
                onWithdrawCommission = { amountInr, destType, destDetails, onResult ->
                    viewModel.withdrawAdminCommission(amountInr, destType, destDetails, onResult)
                },
                onApproveDeposit = { reqId, note -> viewModel.approveDeposit(reqId, note) },
                onRejectDeposit = { reqId, reason -> viewModel.rejectDeposit(reqId, reason) },
                onApproveWithdrawal = { reqId, utr -> viewModel.approveWithdrawal(reqId, utr) },
                onRejectWithdrawal = { reqId, reason -> viewModel.rejectWithdrawal(reqId, reason) },
                onResolveComplaint = { compId, notes, bonus -> viewModel.resolveSupportComplaint(compId, notes, bonus) },
                onRejectComplaint = { compId, reason -> viewModel.rejectSupportComplaint(compId, reason) },
                onDismiss = { viewModel.closeAdminPortal() }
            )
        }

        // Support & Complaints Dialog for Players
        if (showSupportDialog) {
            UserSupportComplaintDialog(
                complaints = allComplaints,
                onSubmitComplaint = { cat, sub, desc, ref, contact, onResult ->
                    viewModel.submitSupportComplaint(cat, sub, desc, ref, contact, onResult)
                },
                onDismiss = { viewModel.closeSupportDialog() }
            )
        }
    }
}

