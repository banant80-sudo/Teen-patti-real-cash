package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.SupportComplaintEntity
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkError
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserSupportComplaintDialog(
    complaints: List<SupportComplaintEntity>,
    onSubmitComplaint: (category: String, subject: String, description: String, refNo: String?, contact: String?, onResult: (Result<SupportComplaintEntity>) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Submit Ticket, 1: My Tickets

    // Form inputs
    val categories = listOf(
        "Add Cash / Deposit",
        "Withdrawal Status",
        "Table Disconnection",
        "Account / KYC",
        "Other Issue"
    )
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var subjectText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var referenceNoText by remember { mutableStateOf("") }
    var contactText by remember { mutableStateOf("+91 ") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccessMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, ElegantDarkBorder, RoundedCornerShape(24.dp))
                .testTag("user_support_complaint_dialog"),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2538)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = CasinoGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Helpdesk & User Complaints",
                                color = ElegantDarkTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Direct resolution by Game Admin & Support",
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSurfaceElevated)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ElegantDarkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElegantDarkSurfaceInset)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) ElegantDarkPrimary else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✍️ Raise Complaint",
                            color = if (selectedTab == 0) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) ElegantDarkPrimary else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📂 My Tickets (${complaints.size})",
                            color = if (selectedTab == 1) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tab 0: Submit Complaint Form
                if (selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Select Issue Category",
                                color = ElegantDarkTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(categories) { cat ->
                                    val isSelected = cat == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) ElegantDarkPrimary else ElegantDarkSurface)
                                            .border(
                                                1.dp,
                                                if (isSelected) CasinoGold else ElegantDarkBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) ElegantDarkOnPrimary else ElegantDarkTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = subjectText,
                                onValueChange = { subjectText = it; errorMessage = null },
                                label = { Text("Subject / Brief Summary") },
                                placeholder = { Text("e.g. Added ₹500 via UPI, chips not received") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantDarkPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantDarkTextPrimary,
                                    unfocusedTextColor = ElegantDarkTextPrimary,
                                    focusedContainerColor = ElegantDarkSurface,
                                    unfocusedContainerColor = ElegantDarkSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("complaint_subject_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = referenceNoText,
                                onValueChange = { referenceNoText = it },
                                label = { Text("Transaction ID / UTR (Optional)") },
                                placeholder = { Text("e.g. 423891002341 or WDRW-123") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantDarkPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantDarkTextPrimary,
                                    unfocusedTextColor = ElegantDarkTextPrimary,
                                    focusedContainerColor = ElegantDarkSurface,
                                    unfocusedContainerColor = ElegantDarkSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("complaint_ref_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = contactText,
                                onValueChange = { contactText = it },
                                label = { Text("Contact Phone / UPI ID") },
                                placeholder = { Text("+91 9876543210 or user@upi") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantDarkPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantDarkTextPrimary,
                                    unfocusedTextColor = ElegantDarkTextPrimary,
                                    focusedContainerColor = ElegantDarkSurface,
                                    unfocusedContainerColor = ElegantDarkSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = descriptionText,
                                onValueChange = { descriptionText = it; errorMessage = null },
                                label = { Text("Detailed Explanation") },
                                placeholder = { Text("Describe what happened, table name, amount, or any details to help admin resolve quickly...") },
                                minLines = 3,
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantDarkPrimary,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantDarkTextPrimary,
                                    unfocusedTextColor = ElegantDarkTextPrimary,
                                    focusedContainerColor = ElegantDarkSurface,
                                    unfocusedContainerColor = ElegantDarkSurface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("complaint_description_input")
                            )
                        }

                        if (errorMessage != null) {
                            item {
                                Text(
                                    text = errorMessage ?: "",
                                    color = ElegantDarkError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (submissionSuccessMessage != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B3820))
                                        .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = submissionSuccessMessage ?: "",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (subjectText.isBlank()) {
                                errorMessage = "Please enter a subject for the complaint."
                                return@Button
                            }
                            if (descriptionText.isBlank()) {
                                errorMessage = "Please describe your issue in detail."
                                return@Button
                            }
                            isSubmitting = true
                            onSubmitComplaint(
                                selectedCategory,
                                subjectText,
                                descriptionText,
                                referenceNoText.ifBlank { null },
                                contactText.ifBlank { null }
                            ) { result ->
                                isSubmitting = false
                                result.onSuccess { ticket ->
                                    submissionSuccessMessage = "Ticket #${ticket.ticketId} submitted to Admin! Admin will investigate and resolve."
                                    subjectText = ""
                                    descriptionText = ""
                                    referenceNoText = ""
                                    errorMessage = null
                                }.onFailure { err ->
                                    errorMessage = err.message ?: "Failed to submit ticket."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_complaint_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = ElegantDarkOnPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Submit Complaint to Admin",
                                color = ElegantDarkOnPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Tab 1: My Tickets List
                    if (complaints.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = ElegantDarkTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No complaints submitted yet",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Have an issue with Add Cash or Withdrawal? Raise a ticket.",
                                    color = ElegantDarkTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(complaints) { ticket ->
                                ComplaintTicketCard(ticket = ticket)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplaintTicketCard(ticket: SupportComplaintEntity) {
    val statusColor = when (ticket.status) {
        "RESOLVED" -> Color(0xFF4CAF50)
        "IN_REVIEW" -> Color(0xFF29B6F6)
        "REJECTED" -> Color(0xFFEF5350)
        else -> CasinoGold
    }
    val statusLabel = when (ticket.status) {
        "RESOLVED" -> "RESOLVED BY ADMIN"
        "IN_REVIEW" -> "IN REVIEW"
        "REJECTED" -> "CLOSED"
        else -> "PENDING ADMIN REVIEW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${ticket.ticketId}",
                        color = ElegantDarkPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${ticket.category}",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = ticket.subject,
                color = ElegantDarkTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = ticket.description,
                color = ElegantDarkTextSecondary,
                fontSize = 12.sp
            )

            if (!ticket.referenceNo.isNullOrBlank()) {
                Text(
                    text = "Ref / UTR: ${ticket.referenceNo}",
                    color = CasinoGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Admin Resolution Response Box
            if (!ticket.resolutionNotes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF221F2D))
                        .border(1.dp, ElegantDarkPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "👑", fontSize = 12.sp)
                            Text(
                                text = "Admin Resolution Reply:",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = ticket.resolutionNotes ?: "",
                            color = ElegantDarkTextPrimary,
                            fontSize = 12.sp
                        )

                        if (ticket.bonusCompensationChips > 0L) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🎁 Admin Compensation Credited: +${formatChips(ticket.bonusCompensationChips)} Chips to your wallet",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ticket.timestamp)),
                    color = ElegantDarkTextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = if (ticket.status == "RESOLVED") "Status: Resolved ✅" else "Awaiting Admin Action",
                    color = if (ticket.status == "RESOLVED") Color(0xFF4CAF50) else CasinoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
