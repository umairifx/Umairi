package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.LedgerParty
import com.example.data.models.LedgerTransaction
import com.example.ui.BusinessViewModel
import com.example.ui.theme.GreenPositive
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RedNegative
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedgerScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 for Customers, 1 for Suppliers
    val partyType = if (selectedTab == 0) "CUSTOMER" else "SUPPLIER"

    val parties by viewModel.getPartiesByType(partyType).collectAsStateWithLifecycle(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var selectedPartyForDetails by remember { mutableStateOf<LedgerParty?>(null) }

    val filteredParties = parties.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    if (selectedPartyForDetails != null) {
        // Render detailed Ledger transaction statement screen
        LedgerPartyDetailsScreen(
            party = selectedPartyForDetails!!,
            viewModel = viewModel,
            onBack = { selectedPartyForDetails = null }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddPartyDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_party_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Party")
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab Row for Customers / Suppliers
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Customers (Receivable)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("customer_tab")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Suppliers (Payable)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("supplier_tab")
                    )
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${if (selectedTab == 0) "customer" else "supplier"} name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_search")
                )

                if (filteredParties.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No ${if (selectedTab == 0) "customers" else "suppliers"} registered yet!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Click the '+' button to add contacts & record balances.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredParties) { party ->
                            PartyRow(
                                party = party,
                                onClick = { selectedPartyForDetails = party },
                                onSendReminder = {
                                    val textMessage = viewModel.generateReminderText(party)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, textMessage)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder")
                                    context.startActivity(shareIntent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Customer / Supplier Dialog
    if (showAddPartyDialog) {
        AddPartyDialog(
            partyType = partyType,
            onDismiss = { showAddPartyDialog = false },
            onConfirm = { name, phone, email, balance ->
                viewModel.addLedgerParty(name, phone, email, partyType, balance)
                showAddPartyDialog = false
            }
        )
    }
}

@Composable
fun PartyRow(
    party: LedgerParty,
    onClick: () -> Unit,
    onSendReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReceivable = party.partyType == "CUSTOMER"
    val balanceText = "Rs. ${String.format(Locale.getDefault(), "%.2f", Math.abs(party.balance))}"
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (party.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = party.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = balanceText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (party.balance > 0) {
                        if (isReceivable) RedNegative else RedNegative
                    } else if (party.balance < 0) {
                        GreenPositive
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Text(
                    text = if (party.balance > 0) {
                        if (isReceivable) "Receivable (Due)" else "Payable (We owe)"
                    } else if (party.balance < 0) {
                        "Advance Credit"
                    } else {
                        "Settled"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (party.balance > 0) {
                    Button(
                        onClick = { onSendReminder() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPositive.copy(alpha = 0.1f), contentColor = GreenPositive),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("send_reminder_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remind", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerPartyDetailsScreen(
    party: LedgerParty,
    viewModel: BusinessViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.getTransactionsForParty(party.id).collectAsStateWithLifecycle(emptyList())
    var showAddTxDialog by remember { mutableStateOf<String?>(null) } // "PAYMENT" or "CREDIT"

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (party.partyType == "CUSTOMER") "Customer Ledger Sheet" else "Supplier Ledger Sheet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        },
        bottomBar = {
            // Give/Receive actions inside the ledger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (party.partyType == "CUSTOMER") {
                    Button(
                        onClick = { showAddTxDialog = "CREDIT" },
                        colors = ButtonDefaults.buttonColors(containerColor = RedNegative),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("give_credit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Give Credit (Udhari)", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAddTxDialog = "PAYMENT" },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPositive),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("receive_payment_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Receive Payment", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showAddTxDialog = "CREDIT" },
                        colors = ButtonDefaults.buttonColors(containerColor = RedNegative),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Take Credit", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAddTxDialog = "PAYMENT" },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPositive),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pay Cash", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business card summary of the contact
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Contact Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (party.phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(party.phone, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (party.email.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(party.email, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Circular running balance display
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Rs. ${String.format(Locale.getDefault(), "%.2f", Math.abs(party.balance))}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (party.balance > 0) RedNegative else GreenPositive
                            )
                            Text(
                                text = if (party.balance > 0) "Total Due" else "Advance Credit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (party.balance > 0) {
                        Button(
                            onClick = {
                                val textMessage = viewModel.generateReminderText(party)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, textMessage)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPositive),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share WhatsApp/Email Payment Reminder")
                        }
                    }
                }
            }

            // Ledger statement listing
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transaction Statement history",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No statements logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(transactions) { tx ->
                        LedgerTransactionRow(
                            tx = tx,
                            onDelete = { viewModel.deleteLedgerTransaction(tx, party) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue to record new CREDIT or PAYMENT transaction manually
    if (showAddTxDialog != null) {
        val type = showAddTxDialog!!
        AddLedgerTransactionDialog(
            isCredit = type == "CREDIT",
            partyType = party.partyType,
            onDismiss = { showAddTxDialog = null },
            onConfirm = { amount, desc ->
                val txType = if (party.partyType == "CUSTOMER") {
                    if (type == "CREDIT") "CREDIT_GIVEN" else "PAYMENT_RECEIVED"
                } else {
                    if (type == "CREDIT") "CREDIT_TAKEN" else "PAYMENT_MADE"
                }
                viewModel.addLedgerTransaction(party.id, amount, txType, desc)
                showAddTxDialog = null
            }
        )
    }
}

@Composable
fun LedgerTransactionRow(
    tx: LedgerTransaction,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.date))
    val isCredit = tx.transactionType == "CREDIT_GIVEN" || tx.transactionType == "CREDIT_TAKEN"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (tx.description.isBlank()) {
                        if (isCredit) "Credit Recorded" else "Payment Logged"
                    } else tx.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${if (isCredit) "+" else "-"} Rs. ${String.format(Locale.getDefault(), "%.2f", tx.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) RedNegative else GreenPositive
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Statement Log",
                        tint = RedNegative.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddLedgerTransactionDialog(
    isCredit: Boolean,
    partyType: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val heading = if (partyType == "CUSTOMER") {
        if (isCredit) "Give Credit (Udhari)" else "Receive Payment"
    } else {
        if (isCredit) "Take Credit" else "Record Cash Payment"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) RedNegative else GreenPositive
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_tx_amount")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Remarks / Remarks Details") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_tx_desc")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) onConfirm(amt, description)
                        },
                        enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCredit) RedNegative else GreenPositive
                        ),
                        modifier = Modifier.testTag("dialog_tx_save")
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun AddPartyDialog(
    partyType: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }

    val heading = if (partyType == "CUSTOMER") "Add Customer Contact" else "Add Supplier / vendor"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_party_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_party_phone")
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_party_email")
                    )
                }

                item {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Opening Due balance (Rs., optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_party_balance")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val bal = initialBalance.toDoubleOrNull() ?: 0.0
                                onConfirm(name, phone, email, bal)
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.testTag("dialog_party_save")
                        ) {
                            Text("Save Contact")
                        }
                    }
                }
            }
        }
    }
}
