package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentOption
import com.example.data.model.PaymentPack
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOnPrimaryContainer
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class PaymentStep {
    object SelectMethod : PaymentStep()
    data class EnterDetails(val option: PaymentOption) : PaymentStep()
    data class Processing(val statusText: String) : PaymentStep()
    data class Success(val txnId: String, val amountChips: Long, val inr: Int, val method: String) : PaymentStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewaySheet(
    sheetState: SheetState,
    pack: PaymentPack,
    paymentOptions: List<PaymentOption>,
    onPaymentCompleted: (pack: PaymentPack, txnId: String, method: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf<PaymentStep>(PaymentStep.SelectMethod) }
    var selectedCategory by remember { mutableStateOf(PaymentCategory.UPI) }
    var selectedOption by remember { mutableStateOf(paymentOptions.first()) }
    var upiIdInput by remember { mutableStateOf("user@okhdfcbank") }
    var cardNumberInput by remember { mutableStateOf("4532 •••• •••• 8821") }
    var expiryInput by remember { mutableStateOf("09/28") }
    var cvvInput by remember { mutableStateOf("724") }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ElegantDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ElegantDarkBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Security Lock: Elegant Dark styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Security",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Secure Payment Gateway",
                            color = ElegantDarkTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "256-Bit SSL Encrypted • Real-time Instant Settlement",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ElegantDarkTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pack Summary Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ElegantDarkSurfaceInset)
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Order Summary",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🪙", fontSize = 18.sp)
                            Text(
                                text = "${formatChips(pack.chips + pack.bonusChips)} Chips",
                                color = ElegantDarkPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        if (pack.bonusChips > 0) {
                            Text(
                                text = "Includes +${formatChips(pack.bonusChips)} Free Bonus!",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Amount to Pay",
                            color = ElegantDarkTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "₹${pack.priceInr}",
                            color = ElegantDarkTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PaymentStepTransition"
            ) { step ->
                when (step) {
                    is PaymentStep.SelectMethod -> {
                        Column {
                            Text(
                                text = "Select Payment Mode",
                                color = ElegantDarkTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Category tabs: Pills styled in Elegant Dark
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PaymentCategory.values().forEach { cat ->
                                    val isSelected = cat == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) ElegantDarkPrimary else ElegantDarkSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isSelected) ElegantDarkPrimary else ElegantDarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                selectedCategory = cat
                                                selectedOption = paymentOptions.first { it.category == cat }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (cat) {
                                                PaymentCategory.UPI -> "UPI"
                                                PaymentCategory.CARDS -> "Cards"
                                                PaymentCategory.NET_BANKING -> "Banks"
                                                PaymentCategory.WALLET -> "Wallets"
                                            },
                                            color = if (isSelected) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Options in selected category
                            val categoryOptions = paymentOptions.filter { it.category == selectedCategory }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categoryOptions.forEach { opt ->
                                    val isChosen = opt.id == selectedOption.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isChosen) ElegantDarkPrimaryContainer.copy(alpha = 0.4f) else ElegantDarkSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isChosen) ElegantDarkPrimary else ElegantDarkBorder,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedOption = opt }
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                val icon = when (opt.category) {
                                                    PaymentCategory.UPI -> Icons.Default.QrCodeScanner
                                                    PaymentCategory.CARDS -> Icons.Default.CreditCard
                                                    PaymentCategory.NET_BANKING -> Icons.Default.AccountBalance
                                                    PaymentCategory.WALLET -> Icons.Default.Wallet
                                                }
                                                Icon(
                                                    icon,
                                                    contentDescription = null,
                                                    tint = if (isChosen) ElegantDarkPrimary else ElegantDarkTextSecondary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = opt.name,
                                                        color = ElegantDarkTextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = opt.subtitle,
                                                        color = ElegantDarkTextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            if (opt.badge != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(ElegantDarkPrimaryContainer)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = opt.badge,
                                                        color = ElegantDarkOnPrimaryContainer,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { currentStep = PaymentStep.EnterDetails(selectedOption) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("continue_to_payment_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "Continue with ${selectedOption.name}",
                                    color = ElegantDarkOnPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    is PaymentStep.EnterDetails -> {
                        val opt = step.option
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Confirm ${opt.name}",
                                    color = ElegantDarkTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Change",
                                    color = ElegantDarkPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { currentStep = PaymentStep.SelectMethod }
                                )
                            }

                            if (opt.category == PaymentCategory.UPI) {
                                OutlinedTextField(
                                    value = upiIdInput,
                                    onValueChange = { upiIdInput = it },
                                    label = { Text("Virtual Payment Address (UPI ID)") },
                                    placeholder = { Text("username@upi") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantDarkPrimary,
                                        unfocusedBorderColor = ElegantDarkBorder,
                                        focusedTextColor = ElegantDarkTextPrimary,
                                        unfocusedTextColor = ElegantDarkTextPrimary,
                                        focusedLabelColor = ElegantDarkPrimary,
                                        unfocusedLabelColor = ElegantDarkTextSecondary
                                    )
                                )
                                Text(
                                    text = "A real-time payment notification will be sent to your UPI App.",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 11.sp
                                )
                            } else if (opt.category == PaymentCategory.CARDS) {
                                OutlinedTextField(
                                    value = cardNumberInput,
                                    onValueChange = { cardNumberInput = it },
                                    label = { Text("Card Number") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElegantDarkPrimary,
                                        unfocusedBorderColor = ElegantDarkBorder,
                                        focusedTextColor = ElegantDarkTextPrimary,
                                        unfocusedTextColor = ElegantDarkTextPrimary
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = expiryInput,
                                        onValueChange = { expiryInput = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElegantDarkPrimary,
                                            unfocusedBorderColor = ElegantDarkBorder,
                                            focusedTextColor = ElegantDarkTextPrimary,
                                            unfocusedTextColor = ElegantDarkTextPrimary
                                        )
                                    )
                                    OutlinedTextField(
                                        value = cvvInput,
                                        onValueChange = { cvvInput = it },
                                        label = { Text("CVV") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElegantDarkPrimary,
                                            unfocusedBorderColor = ElegantDarkBorder,
                                            focusedTextColor = ElegantDarkTextPrimary,
                                            unfocusedTextColor = ElegantDarkTextPrimary
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = "You will be securely redirected to ${opt.name} portal to verify and authorize ₹${pack.priceInr}.",
                                    color = ElegantDarkTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        currentStep = PaymentStep.Processing("Connecting to Payment Gateway...")
                                        delay(1000)
                                        currentStep = PaymentStep.Processing("Authenticating with ${opt.name}...")
                                        delay(1200)
                                        currentStep = PaymentStep.Processing("Verifying transaction signature...")
                                        delay(1000)
                                        val generatedTxnId = "TXN-" + UUID.randomUUID().toString().take(10).uppercase()
                                        currentStep = PaymentStep.Success(
                                            txnId = generatedTxnId,
                                            amountChips = pack.chips + pack.bonusChips,
                                            inr = pack.priceInr,
                                            method = opt.name
                                        )
                                        onPaymentCompleted(pack, generatedTxnId, opt.name)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("pay_securely_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = ElegantDarkOnPrimary
                                    )
                                    Text(
                                        text = "Pay ₹${pack.priceInr} Securely",
                                        color = ElegantDarkOnPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    is PaymentStep.Processing -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ElegantDarkPrimary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = step.statusText,
                                color = ElegantDarkTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Please do not press back or close the application.",
                                color = ElegantDarkTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    is PaymentStep.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )

                            Text(
                                text = "Payment Sent to Admin Account!",
                                color = ElegantDarkTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "+${formatChips(step.amountChips)} Chips (Pending Verification)",
                                color = ElegantDarkPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Money transferred to Admin. Admin verifies the UTR ref and confirms payment before chips are added to your wallet.",
                                color = ElegantDarkTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Digital Receipt: bg-[#211F26] rounded-2xl
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ElegantDarkSurfaceInset)
                                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ReceiptRow("UTR / Ref No", step.txnId)
                                    ReceiptRow("Payment Mode", step.method)
                                    ReceiptRow("Amount Paid", "₹${step.inr}")
                                    ReceiptRow("Status", "PENDING ADMIN APPROVAL")
                                    ReceiptRow(
                                        "Timestamp",
                                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("done_payment_receipt_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    text = "Awesome! Return to Game",
                                    color = ElegantDarkOnPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = ElegantDarkTextSecondary, fontSize = 11.sp)
        Text(text = value, color = ElegantDarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
