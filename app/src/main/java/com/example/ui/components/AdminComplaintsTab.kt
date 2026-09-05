package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkError
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceInset
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminComplaintsTab(
    complaints: List<SupportComplaintEntity>,
    onResolveComplaint: (complaintId: Long, notes: String, bonusChips: Long) -> Unit,
    onRejectComplaint: (complaintId: Long, reason: String) -> Unit
) {
    var filterOpenOnly by remember { mutableStateOf(false) }
    var selectedForResolve by remember { mutableStateOf<SupportComplaintEntity?>(null) }
    var selectedForReject by remember { mutableStateOf<SupportComplaintEntity?>(null) }
    var resolutionNotesText by remember { mutableStateOf("Investigated and resolved. Transaction verified.") }
    var bonusChipsText by remember { mutableStateOf("0") }
    var rejectReasonText by remember { mutableStateOf("") }

    val openComplaints = complaints.filter { it.status == "OPEN" || it.status == "IN_REVIEW" }
    val displayedComplaints = if (filterOpenOnly) openComplaints else complaints

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_complaints_tab"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Explanatory Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2438)),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A148C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color(0xFFE1BEE7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "User Complaints & Helpdesk Resolution",
                        color = Color(0xFFE1BEE7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review tickets filed by players regarding payment, gameplay, or account issues. Admin can directly resolve, reply to user, and provide goodwill chips compensation.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Header and Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tickets (${displayedComplaints.size})",
                color = ElegantDarkTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!filterOpenOnly) ElegantDarkPrimary else ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .clickable { filterOpenOnly = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "All (${complaints.size})",
                        color = if (!filterOpenOnly) ElegantDarkOnPrimary else ElegantDarkTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (filterOpenOnly) CasinoGold else ElegantDarkSurface)
                        .border(1.dp, if (filterOpenOnly) CasinoGold else ElegantDarkBorder, RoundedCornerShape(8.dp))
                        .clickable { filterOpenOnly = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Open (${openComplaints.size})",
                        color = if (filterOpenOnly) Color.Black else CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Complaints List
        if (displayedComplaints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = ElegantDarkTextSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (filterOpenOnly) "No open complaints" else "No complaints registered",
                        color = ElegantDarkTextSecondary,
                        fontSize = 13.sp
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
                items(displayedComplaints) { ticket ->
                    AdminComplaintItemCard(
                        ticket = ticket,
                        onResolveClick = {
                            resolutionNotesText = "Investigated and resolved by Admin. Issue sorted out."
                            bonusChipsText = "0"
                            selectedForResolve = ticket
                        },
                        onRejectClick = { selectedForReject = ticket }
                    )
                }
            }
        }
    }

    // Resolve Complaint Dialog
    if (selectedForResolve != null) {
        val ticket = selectedForResolve!!
        Dialog(onDismissRequest = { selectedForResolve = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.RateReview,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Resolve Ticket #${ticket.ticketId}",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ticket Info snippet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElegantDarkSurfaceInset)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "Player: ${ticket.userName} (${ticket.category})",
                                color = ElegantDarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Issue: ${ticket.subject}",
                                color = CasinoGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ticket.description,
                                color = ElegantDarkTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }

                    OutlinedTextField(
                        value = resolutionNotesText,
                        onValueChange = { resolutionNotesText = it },
                        label = { Text("Resolution Message (Sent to User)") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bonusChipsText,
                        onValueChange = { bonusChipsText = it.filter { char -> char.isDigit() } },
                        label = { Text("Compensation / Bonus Chips (Optional)") },
                        placeholder = { Text("e.g. 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedForResolve = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                val bonus = bonusChipsText.toLongOrNull() ?: 0L
                                onResolveComplaint(ticket.id, resolutionNotesText, bonus)
                                selectedForResolve = null
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Mark Resolved",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Reject Complaint Dialog
    if (selectedForReject != null) {
        val ticket = selectedForReject!!
        Dialog(onDismissRequest = { selectedForReject = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, ElegantDarkError, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = ElegantDarkError,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Close Ticket #${ticket.ticketId}",
                            color = ElegantDarkTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Provide reason for closing or rejecting ticket without resolution.",
                        color = ElegantDarkTextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Reason for Rejection / Closing") },
                        placeholder = { Text("e.g. Could not verify transaction reference / Invalid claim") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkError,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedForReject = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = ElegantDarkTextSecondary)
                        }

                        Button(
                            onClick = {
                                onRejectComplaint(
                                    ticket.id,
                                    rejectReasonText.ifBlank { "Closed by Admin: Could not substantiate claim." }
                                )
                                selectedForReject = null
                                rejectReasonText = ""
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkError),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Close Ticket",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminComplaintItemCard(
    ticket: SupportComplaintEntity,
    onResolveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val isOpen = ticket.status == "OPEN" || ticket.status == "IN_REVIEW"

    val statusColor = when (ticket.status) {
        "RESOLVED" -> Color(0xFF4CAF50)
        "REJECTED" -> Color(0xFFEF5350)
        else -> CasinoGold
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isOpen) CasinoGold.copy(alpha = 0.6f) else ElegantDarkBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpen) Color(0xFF241F2B) else ElegantDarkSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
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
                        color = Color(0xFF81D4FA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
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
                        text = when (ticket.status) {
                            "RESOLVED" -> "RESOLVED ✅"
                            "REJECTED" -> "CLOSED ❌"
                            else -> "OPEN / PENDING"
                        },
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

            // User Info & Reference Details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElegantDarkSurfaceInset)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Player: ${ticket.userName} (ID: ${ticket.userId})",
                            color = ElegantDarkTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!ticket.referenceNo.isNullOrBlank()) {
                            Text(
                                text = "Ref/UTR: ${ticket.referenceNo}",
                                color = CasinoGold,
                                fontSize = 11.sp
                            )
                        }
                        if (!ticket.contactInfo.isNullOrBlank()) {
                            Text(
                                text = "Contact: ${ticket.contactInfo}",
                                color = ElegantDarkTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ticket.timestamp)),
                        color = ElegantDarkTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Resolution note if present
            if (!ticket.resolutionNotes.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E281F))
                        .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Admin Resolution Reply:",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ticket.resolutionNotes ?: "",
                            color = ElegantDarkTextPrimary,
                            fontSize = 11.sp
                        )
                        if (ticket.bonusCompensationChips > 0L) {
                            Text(
                                text = "🎁 Compensation Credited: +${formatChips(ticket.bonusCompensationChips)} Chips to player",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action Buttons for Open Tickets
            if (isOpen) {
                HorizontalDivider(color = ElegantDarkBorder, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkError.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close / Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onResolveClick,
                        modifier = Modifier.weight(1.5f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Resolve Ticket", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
