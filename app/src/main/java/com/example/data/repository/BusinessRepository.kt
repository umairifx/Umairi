package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.LedgerDao
import com.example.data.models.InventoryItem
import com.example.data.models.Invoice
import com.example.data.models.InvoiceItem
import com.example.data.models.LedgerParty
import com.example.data.models.LedgerTransaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class BusinessRepository(
    private val inventoryDao: InventoryDao,
    private val invoiceDao: InvoiceDao,
    private val ledgerDao: LedgerDao
) {
    // Inventory
    val allInventoryItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    
    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems().map { items ->
        items.filter { it.currentStock <= it.lowStockThreshold }
    }

    suspend fun insertInventoryItem(item: InventoryItem): Long = inventoryDao.insert(item)
    
    suspend fun updateInventoryItem(item: InventoryItem) = inventoryDao.update(item)
    
    suspend fun deleteInventoryItem(item: InventoryItem) = inventoryDao.delete(item)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getInvoiceById(id: Int): Flow<Invoice?> = invoiceDao.getInvoiceById(id)

    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, InvoiceItem::class.java)
    private val adapter = moshi.adapter<List<InvoiceItem>>(listType)

    suspend fun insertInvoice(invoice: Invoice): Long {
        // 1. Save the invoice
        val invoiceId = invoiceDao.insert(invoice)

        // 2. Parse items and update inventory stock
        val items = try {
            adapter.fromJson(invoice.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        for (item in items) {
            // Deduct from stock (quantity sold is positive, so pass -quantity)
            inventoryDao.updateStock(item.itemId, -item.quantity)
        }

        // 3. If there is a pending balance (grandTotal > amountPaid), update or create Ledger
        val pendingBalance = invoice.grandTotal - invoice.amountPaid
        if (pendingBalance > 0 && invoice.customerName.isNotBlank()) {
            // Find existing party or create one
            val parties = ledgerDao.getPartiesByType("CUSTOMER").firstOrNull() ?: emptyList()
            var party = parties.find { it.name.equals(invoice.customerName, ignoreCase = true) }
            
            val partyId = if (party == null) {
                val newParty = LedgerParty(
                    name = invoice.customerName,
                    phone = invoice.customerPhone,
                    partyType = "CUSTOMER",
                    balance = pendingBalance
                )
                ledgerDao.insertParty(newParty).toInt()
            } else {
                ledgerDao.updatePartyBalance(party.id, pendingBalance)
                party.id
            }

            // Create ledger transaction for this balance due
            val transaction = LedgerTransaction(
                partyId = partyId,
                amount = pendingBalance,
                transactionType = "CREDIT_GIVEN",
                description = "Invoice #${invoice.invoiceNumber} Credit Sales"
            )
            ledgerDao.insertTransaction(transaction)
        }

        return invoiceId
    }

    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.delete(invoice)

    // Ledger Parties & Khata
    val allParties: Flow<List<LedgerParty>> = ledgerDao.getAllParties()
    val allTransactions: Flow<List<LedgerTransaction>> = ledgerDao.getAllTransactions()

    fun getPartiesByType(type: String): Flow<List<LedgerParty>> = ledgerDao.getPartiesByType(type)

    fun getPartyById(id: Int): Flow<LedgerParty?> = ledgerDao.getPartyById(id)

    suspend fun insertParty(party: LedgerParty): Long = ledgerDao.insertParty(party)

    suspend fun updateParty(party: LedgerParty) = ledgerDao.updateParty(party)

    suspend fun deleteParty(party: LedgerParty) = ledgerDao.deleteParty(party)

    fun getTransactionsForParty(partyId: Int): Flow<List<LedgerTransaction>> = ledgerDao.getTransactionsForParty(partyId)

    suspend fun insertLedgerTransaction(transaction: LedgerTransaction): Long {
        // Save the transaction
        val id = ledgerDao.insertTransaction(transaction)

        // Adjust party's outstanding balance
        // Customer balance: positive means we receive money. Credit Given (+), Payment Received (-)
        // Supplier balance: positive means we pay money. Credit Taken (+), Payment Made (-)
        val balanceChange = when (transaction.transactionType) {
            "CREDIT_GIVEN" -> transaction.amount
            "PAYMENT_RECEIVED" -> -transaction.amount
            "CREDIT_TAKEN" -> transaction.amount
            "PAYMENT_MADE" -> -transaction.amount
            else -> 0.0
        }
        
        if (balanceChange != 0.0) {
            ledgerDao.updatePartyBalance(transaction.partyId, balanceChange)
        }

        return id
    }

    suspend fun deleteLedgerTransaction(transactionId: Int, partyId: Int, amount: Double, type: String) {
        // To delete a transaction, we first reverse its effect on the party balance
        val balanceReversal = when (type) {
            "CREDIT_GIVEN" -> -amount
            "PAYMENT_RECEIVED" -> amount
            "CREDIT_TAKEN" -> -amount
            "PAYMENT_MADE" -> amount
            else -> 0.0
        }
        
        if (balanceReversal != 0.0) {
            ledgerDao.updatePartyBalance(partyId, balanceReversal)
        }
        
        ledgerDao.deleteTransaction(transactionId)
    }
}
