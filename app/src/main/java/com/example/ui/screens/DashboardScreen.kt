package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Invoice
import com.example.ui.BusinessViewModel
import com.example.ui.theme.GreenPositive
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RedNegative
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BusinessViewModel,
    onNavigateToBilling: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reportState by viewModel.reportState.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val parties by viewModel.parties.collectAsStateWithLifecycle(emptyList())

    // Calculate Receivables & Payables dynamically from Ledger parties
    val totalReceivable = parties.filter { it.partyType == "CUSTOMER" && it.balance > 0 }.sumOf { it.balance }
    val totalPayable = parties.filter { it.partyType == "SUPPLIER" && it.balance > 0 }.sumOf { it.balance }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pristine Brand Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            BrandHeaderSection()
        }

        // Hero Statement Card (Total Sales + You'll Receive / You'll Pay)
        item {
            HeroStatementCard(
                totalSales = reportState.totalSales,
                totalReceivable = totalReceivable,
                totalPayable = totalPayable
            )
        }

        // Low Stock Alert Banner (Minimalist styled matching Design HTML)
        if (lowStockItems.isNotEmpty()) {
            item {
                MinimalistLowStockAlert(
                    lowStockCount = lowStockItems.size,
                    onClick = onNavigateToInventory
                )
            }
        }

        // Quick Actions Dashboard Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MinimalistQuickActionButton(
                        label = "Sale",
                        icon = Icons.Default.Add,
                        iconColor = GreenPositive,
                        bgColor = GreenPositive.copy(alpha = 0.08f),
                        onClick = onNavigateToBilling,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_create_bill")
                    )
                    MinimalistQuickActionButton(
                        label = "Purchase",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = RedNegative,
                        bgColor = RedNegative.copy(alpha = 0.08f),
                        onClick = onNavigateToLedger,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_party")
                    )
                    MinimalistQuickActionButton(
                        label = "Stock",
                        icon = Icons.Default.Inventory,
                        iconColor = OrangeWarning,
                        bgColor = OrangeWarning.copy(alpha = 0.08f),
                        onClick = onNavigateToInventory,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_product")
                    )
                }
            }
        }

        // Recent Invoices List Wrapper (Enclosed in a sleek card container)
        item {
            RecentInvoicesCardContainer(
                invoices = invoices,
                onViewAllClick = onNavigateToBilling
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BrandHeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Business",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenPositive)
                )
                Text(
                    text = "Online Sync Active",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun HeroStatementCard(
    totalSales: Double,
    totalReceivable: Double,
    totalPayable: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Background decorative blurred circle decoration
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.25f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.1f)
                )
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Total Sales (August)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${String.format(Locale.getDefault(), "%,.2f", totalSales)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Receivable / Payable Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "YOU'LL RECEIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${String.format(Locale.getDefault(), "%,.0f", totalReceivable)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "YOU'LL PAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rs. ${String.format(Locale.getDefault(), "%,.0f", totalPayable)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalistLowStockAlert(
    lowStockCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Amber 50
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(16.dp)) // Amber 100
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Low Stock Alert",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F) // Amber 900
                )
                Text(
                    text = "$lowStockCount products are below the stock limit threshold.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB45309) // Amber 700
                )
            }
        }
    }
}

@Composable
fun MinimalistQuickActionButton(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(24.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155) // Slate 700
            )
        }
    }
}

@Composable
fun RecentInvoicesCardContainer(
    invoices: List<Invoice>,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(32.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // Slate 800
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (invoices.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No statements logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    invoices.take(5).forEach { invoice ->
                        PristineTransactionRow(invoice = invoice)
                    }
                }
            }
        }
    }
}

@Composable
fun PristineTransactionRow(
    invoice: Invoice,
    modifier: Modifier = Modifier
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(invoice.date))
    val isPaid = invoice.paymentMode != "Unpaid"
    val firstChar = invoice.customerName.firstOrNull()?.toString()?.uppercase(Locale.getDefault()) ?: "C"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)), // slate-100
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstChar,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B), // slate-500
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // Slate 800
                )
                Text(
                    text = "Invoice #${invoice.invoiceNumber} • $dateString",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8), // Slate 400
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isPaid) "+" else "-"}Rs. ${String.format(Locale.getDefault(), "%.2f", invoice.grandTotal)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) GreenPositive else RedNegative
            )
        }
    }
}
