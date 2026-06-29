package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontFamily
import android.content.Intent
import com.example.data.models.Invoice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import com.example.ui.BusinessViewModel
import com.example.ui.ReportPeriod
import com.example.ui.theme.GreenPositive
import com.example.ui.theme.RedNegative
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reportState by viewModel.reportState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.reportPeriod.collectAsStateWithLifecycle()
    val allInvoices by viewModel.invoices.collectAsStateWithLifecycle()

    var showInvoicesDialog by remember { mutableStateOf(false) }
    var selectedInvoiceDetail by remember { mutableStateOf<Invoice?>(null) }

    val filteredInvoices = remember(allInvoices, selectedPeriod) {
        val calendar = Calendar.getInstance()
        val startTime = when (selectedPeriod) {
            ReportPeriod.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            ReportPeriod.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            ReportPeriod.MONTHLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.timeInMillis
            }
        }
        allInvoices.filter { it.date >= startTime }.sortedByDescending { it.date }
    }

    val periods = listOf(ReportPeriod.DAILY, ReportPeriod.WEEKLY, ReportPeriod.MONTHLY)
    val selectedIndex = periods.indexOf(selectedPeriod)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Selector Tab
        item {
            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                periods.forEachIndexed { index, period ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.selectReportPeriod(period) },
                        text = {
                            Text(
                                text = when (period) {
                                    ReportPeriod.DAILY -> "Today"
                                    ReportPeriod.WEEKLY -> "7 Days"
                                    ReportPeriod.MONTHLY -> "30 Days"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("report_tab_${period.name}")
                    )
                }
            }
        }

        // Period Indicator Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Viewing report summary for ${reportState.periodLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Profit / Loss Card (Most Important)
        item {
            val isProfit = reportState.totalProfit >= 0
            val profitMargin = if (reportState.totalSales > 0) (reportState.totalProfit / reportState.totalSales) else 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "P&L Statement",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isProfit) "Net Profit" else "Net Loss",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) GreenPositive else RedNegative
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isProfit) GreenPositive.copy(alpha = 0.1f) else RedNegative.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isProfit) GreenPositive else RedNegative,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        text = "Rs. ${String.format(Locale.getDefault(), "%.2f", Math.abs(reportState.totalProfit))}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) GreenPositive else RedNegative
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Profit Margin Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Profit Margin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", profitMargin * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) GreenPositive else RedNegative
                            )
                        }
                        LinearProgressIndicator(
                            progress = { profitMargin.coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isProfit) GreenPositive else RedNegative,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Detailed sales stats grid items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportStatMiniCard(
                    title = "Total Sales",
                    value = "Rs. ${String.format(Locale.getDefault(), "%.2f", reportState.totalSales)}",
                    icon = Icons.Default.MonetizationOn,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ReportStatMiniCard(
                    title = "Cost of Goods",
                    value = "Rs. ${String.format(Locale.getDefault(), "%.2f", reportState.totalPurchaseCost)}",
                    icon = Icons.Default.ShoppingBag,
                    color = RedNegative,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportStatMiniCard(
                    title = "Billed Invoices",
                    value = "${reportState.salesCount} Bills",
                    icon = Icons.Default.ReceiptLong,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = { showInvoicesDialog = true }
                )
                ReportStatMiniCard(
                    title = "Stock Assets",
                    value = "Rs. ${String.format(Locale.getDefault(), "%.2f", reportState.totalStockValue)}",
                    icon = Icons.Default.BarChart,
                    color = GreenPositive,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showInvoicesDialog) {
        BilledInvoicesListDialog(
            invoices = filteredInvoices,
            periodLabel = reportState.periodLabel,
            onDismiss = { showInvoicesDialog = false },
            onInvoiceClick = { invoice ->
                selectedInvoiceDetail = invoice
            }
        )
    }

    if (selectedInvoiceDetail != null) {
        InvoiceBillDetailDialog(
            invoice = selectedInvoiceDetail!!,
            onDismiss = { selectedInvoiceDetail = null },
            onShare = { textReceipt ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textReceipt)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share Invoice via")
                context.startActivity(shareIntent)
            }
        )
    }
}

@Composable
fun ReportStatMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = color
                    )
                }
                if (onClick != null) {
                    Text(
                        text = "View All →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BilledInvoicesListDialog(
    invoices: List<Invoice>,
    periodLabel: String,
    onDismiss: () -> Unit,
    onInvoiceClick: (Invoice) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Billed Invoices",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Period: $periodLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ) {
                    if (invoices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No bills generated in this period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(invoices) { invoice ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onInvoiceClick(invoice) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "#${invoice.invoiceNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Payment Mode Badge
                                            val badgeColor = when (invoice.paymentMode.lowercase()) {
                                                "cash" -> GreenPositive
                                                "online" -> MaterialTheme.colorScheme.primary
                                                else -> RedNegative
                                            }
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (invoice.paymentMode == "Unpaid") "Credit" else invoice.paymentMode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = badgeColor,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = invoice.customerName.ifBlank { "Walk-in Customer" },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text(
                                                text = dateFormat.format(Date(invoice.date)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Rs. ${String.format(Locale.getDefault(), "%.2f", invoice.grandTotal)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceBillDetailDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    val converters = remember { com.example.data.converters.Converters() }
    val items = remember(invoice) { converters.toInvoiceItemList(invoice.itemsJson) }

    val billText = remember(invoice, items) {
        StringBuilder().apply {
            appendLine("============ RECEIPT ============")
            appendLine("Invoice No: #${invoice.invoiceNumber}")
            appendLine("Customer: ${invoice.customerName}")
            if (invoice.customerPhone.isNotBlank()) {
                appendLine("Contact: ${invoice.customerPhone}")
            }
            appendLine("---------------------------------")
            appendLine(String.format("%-18s %3s %8s", "Item Name", "Qty", "Total"))
            appendLine("---------------------------------")
            items.forEach { item ->
                val itemNameLimited = if (item.name.length > 18) item.name.take(15) + "..." else item.name
                appendLine(String.format("%-18s %3d Rs. %7.2f", itemNameLimited, item.quantity, item.total))
            }
            appendLine("---------------------------------")
            appendLine(String.format("Sub Total:               Rs. %8.2f", invoice.subTotal))
            if (invoice.discountAmount > 0) {
                appendLine(String.format("Discount:               -Rs. %8.2f", invoice.discountAmount))
            }
            if (invoice.taxAmount > 0) {
                appendLine(String.format("Tax Added:              +Rs. %8.2f", invoice.taxAmount))
            }
            appendLine("---------------------------------")
            appendLine(String.format("GRAND TOTAL:             Rs. %8.2f", invoice.grandTotal))
            appendLine(String.format("Amount Paid:             Rs. %8.2f", invoice.amountPaid))
            appendLine("Payment Mode:            ${invoice.paymentMode}")
            appendLine("=================================")
            appendLine("Thank you for shopping with us!")
        }.toString()
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Invoice Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        item {
                            Text(
                                text = billText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onShare(billText) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPositive),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Bill")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
