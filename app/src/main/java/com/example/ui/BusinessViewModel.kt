package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.InventoryItem
import com.example.data.models.Invoice
import com.example.data.models.InvoiceItem
import com.example.data.models.LedgerParty
import com.example.data.models.LedgerTransaction
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod {
    DAILY, WEEKLY, MONTHLY
}

data class BusinessReportState(
    val period: ReportPeriod = ReportPeriod.MONTHLY,
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalPurchaseCost: Double = 0.0,
    val totalStockValue: Double = 0.0,
    val salesCount: Int = 0,
    val periodLabel: String = "This Month"
)

// Represents the state of the active invoice being drafted in the UI
data class InvoiceDraftState(
    val customerName: String = "",
    val customerPhone: String = "",
    val selectedItems: List<InvoiceItem> = emptyList(),
    val discountAmount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val paymentMode: String = "Cash", // "Cash", "Online", "Cheque", "Unpaid"
    val amountPaid: Double = 0.0
) {
    val subTotal: Double get() = selectedItems.sumOf { it.total }
    val taxAmount: Double get() = (subTotal - discountAmount) * (taxPercent / 100.0)
    val grandTotal: Double get() = subTotal - discountAmount + taxAmount
}

class BusinessViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BusinessRepository(
        inventoryDao = database.inventoryDao(),
        invoiceDao = database.invoiceDao(),
        ledgerDao = database.ledgerDao()
    )

    // Flow of all database records
    val inventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItem>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parties: StateFlow<List<LedgerParty>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<LedgerTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Invoice drafting state
    private val _invoiceDraft = MutableStateFlow(InvoiceDraftState())
    val invoiceDraft: StateFlow<InvoiceDraftState> = _invoiceDraft

    // Selected period for reports
    private val _reportPeriod = MutableStateFlow(ReportPeriod.MONTHLY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod

    // Report/Analytics calculation flow
    val reportState: StateFlow<BusinessReportState> = combine(
        invoices,
        inventoryItems,
        _reportPeriod
    ) { invoiceList, itemList, period ->
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        val startTime = when (period) {
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

        val periodLabel = when (period) {
            ReportPeriod.DAILY -> "Today"
            ReportPeriod.WEEKLY -> "Last 7 Days"
            ReportPeriod.MONTHLY -> "Last 30 Days"
        }

        val filteredInvoices = invoiceList.filter { it.date >= startTime }
        
        var totalSales = 0.0
        var totalProfit = 0.0
        var totalPurchaseCost = 0.0

        val itemPriceMap = itemList.associateBy { it.id }

        // Deserialize item lists and compute margins
        val converters = com.example.data.converters.Converters()
        for (inv in filteredInvoices) {
            totalSales += inv.grandTotal
            val items = converters.toInvoiceItemList(inv.itemsJson)
            
            var invoicePurchaseCost = 0.0
            for (item in items) {
                val dbItem = itemPriceMap[item.itemId]
                val purchasePrice = dbItem?.purchasePrice ?: (item.price * 0.7) // Fallback to 70% if item was deleted
                invoicePurchaseCost += purchasePrice * item.quantity
            }

            totalPurchaseCost += invoicePurchaseCost
            
            // Profit is revenue minus cost of goods sold, also factoring in discounts and taxes
            // To be precise: grandTotal is the revenue, and invoicePurchaseCost is the cost.
            val profit = inv.grandTotal - invoicePurchaseCost
            totalProfit += profit
        }

        // Stock value
        val stockValue = itemList.sumOf { it.purchasePrice * it.currentStock }

        BusinessReportState(
            period = period,
            totalSales = totalSales,
            totalProfit = totalProfit,
            totalPurchaseCost = totalPurchaseCost,
            totalStockValue = stockValue,
            salesCount = filteredInvoices.size,
            periodLabel = periodLabel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessReportState())


    // --- Inventory Operations ---
    fun addInventoryItem(name: String, purchasePrice: Double, sellingPrice: Double, currentStock: Int, lowStockThreshold: Int, unit: String, category: String, itemCode: String) {
        viewModelScope.launch {
            val item = InventoryItem(
                name = name,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                currentStock = currentStock,
                lowStockThreshold = lowStockThreshold,
                unit = unit,
                category = category,
                itemCode = itemCode
            )
            repository.insertInventoryItem(item)
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // --- Invoicing Drafting Operations ---
    fun updateDraftCustomer(name: String, phone: String) {
        _invoiceDraft.update { it.copy(customerName = name, customerPhone = phone) }
    }

    fun addDraftItem(item: InventoryItem, quantity: Int) {
        _invoiceDraft.update { draft ->
            val existing = draft.selectedItems.find { it.itemId == item.id }
            val updatedList = if (existing != null) {
                draft.selectedItems.map {
                    if (it.itemId == item.id) {
                        val newQty = it.quantity + quantity
                        it.copy(quantity = newQty, total = newQty * it.price)
                    } else it
                }
            } else {
                draft.selectedItems + InvoiceItem(
                    itemId = item.id,
                    name = item.name,
                    quantity = quantity,
                    price = item.sellingPrice,
                    total = quantity * item.sellingPrice
                )
            }
            draft.copy(selectedItems = updatedList)
        }
        updateDraftAmountPaid()
    }

    fun removeDraftItem(itemId: Int) {
        _invoiceDraft.update { draft ->
            draft.copy(selectedItems = draft.selectedItems.filter { it.itemId != itemId })
        }
        updateDraftAmountPaid()
    }

    fun updateDraftItemQuantity(itemId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeDraftItem(itemId)
            return
        }
        _invoiceDraft.update { draft ->
            val updated = draft.selectedItems.map {
                if (it.itemId == itemId) {
                    it.copy(quantity = newQuantity, total = newQuantity * it.price)
                } else it
            }
            draft.copy(selectedItems = updated)
        }
        updateDraftAmountPaid()
    }

    fun updateDraftDiscountsAndTaxes(discountAmount: Double, taxPercent: Double) {
        _invoiceDraft.update { draft ->
            draft.copy(discountAmount = discountAmount, taxPercent = taxPercent)
        }
        updateDraftAmountPaid()
    }

    fun updateDraftPaymentMode(paymentMode: String) {
        _invoiceDraft.update { draft ->
            draft.copy(
                paymentMode = paymentMode,
                amountPaid = if (paymentMode == "Unpaid") 0.0 else draft.grandTotal
            )
        }
    }

    fun updateDraftAmountPaid(amount: Double) {
        _invoiceDraft.update { it.copy(amountPaid = amount) }
    }

    private fun updateDraftAmountPaid() {
        _invoiceDraft.update { draft ->
            draft.copy(
                amountPaid = if (draft.paymentMode == "Unpaid") 0.0 else draft.grandTotal
            )
        }
    }

    fun resetInvoiceDraft() {
        _invoiceDraft.value = InvoiceDraftState()
    }

    fun saveInvoice(onSuccess: (Invoice) -> Unit) {
        viewModelScope.launch {
            val draft = _invoiceDraft.value
            val invoiceNo = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
            val converters = com.example.data.converters.Converters()
            
            val invoice = Invoice(
                invoiceNumber = invoiceNo,
                customerName = draft.customerName.ifBlank { "Walk-in Customer" },
                customerPhone = draft.customerPhone,
                itemsJson = converters.fromInvoiceItemList(draft.selectedItems),
                subTotal = draft.subTotal,
                discountAmount = draft.discountAmount,
                taxAmount = draft.taxAmount,
                grandTotal = draft.grandTotal,
                amountPaid = draft.amountPaid,
                paymentMode = draft.paymentMode
            )
            
            repository.insertInvoice(invoice)
            resetInvoiceDraft()
            onSuccess(invoice)
        }
    }

    // --- Reports Operations ---
    fun selectReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    // --- Ledger / Khata Operations ---
    fun addLedgerParty(name: String, phone: String, email: String, type: String, initialBalance: Double) {
        viewModelScope.launch {
            val party = LedgerParty(
                name = name,
                phone = phone,
                email = email,
                partyType = type,
                balance = initialBalance
            )
            val partyId = repository.insertParty(party).toInt()
            
            // Record initial balance as transaction if not zero
            if (initialBalance != 0.0) {
                val txType = if (type == "CUSTOMER") {
                    if (initialBalance > 0) "CREDIT_GIVEN" else "PAYMENT_RECEIVED"
                } else {
                    if (initialBalance > 0) "CREDIT_TAKEN" else "PAYMENT_MADE"
                }
                
                val transaction = LedgerTransaction(
                    partyId = partyId,
                    amount = Math.abs(initialBalance),
                    transactionType = txType,
                    description = "Opening Balance"
                )
                // We directly insert this into database since party is already created with the correct balance
                database.ledgerDao().insertTransaction(transaction)
            }
        }
    }

    fun updateLedgerParty(party: LedgerParty) {
        viewModelScope.launch {
            repository.updateParty(party)
        }
    }

    fun deleteLedgerParty(party: LedgerParty) {
        viewModelScope.launch {
            repository.deleteParty(party)
        }
    }

    fun addLedgerTransaction(partyId: Int, amount: Double, type: String, description: String) {
        viewModelScope.launch {
            val tx = LedgerTransaction(
                partyId = partyId,
                amount = amount,
                transactionType = type,
                description = description
            )
            repository.insertLedgerTransaction(tx)
        }
    }

    fun deleteLedgerTransaction(tx: LedgerTransaction, party: LedgerParty) {
        viewModelScope.launch {
            repository.deleteLedgerTransaction(
                transactionId = tx.id,
                partyId = party.id,
                amount = tx.amount,
                type = tx.transactionType
            )
        }
    }

    fun getTransactionsForParty(partyId: Int): Flow<List<LedgerTransaction>> {
        return repository.getTransactionsForParty(partyId)
    }

    fun getPartiesByType(type: String): Flow<List<LedgerParty>> {
        return repository.getPartiesByType(type)
    }

    // Friendly balance reminder sharing text generator
    fun generateReminderText(party: LedgerParty): String {
        val appName = "My Business"
        val phoneSuffix = if (party.phone.isNotBlank()) " on ${party.phone}" else ""
        return if (party.partyType == "CUSTOMER") {
            "Dear ${party.name}, this is a gentle reminder that an outstanding payment of Rs. ${String.format(Locale.getDefault(), "%.2f", party.balance)} is pending. Please settle it at your earliest convenience. Thank you for doing business with us! - Sent via $appName"
        } else {
            "Hi ${party.name}, checking on our outstanding payable balance of Rs. ${String.format(Locale.getDefault(), "%.2f", party.balance)}. Please provide payment details or let me know when it will be processed. Thank you. - Sent via $appName"
        }
    }
}
