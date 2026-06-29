package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val lowStockThreshold: Int = 5,
    val unit: String = "pcs",
    val category: String = "General",
    val itemCode: String = ""
)

@JsonClass(generateAdapter = true)
data class InvoiceItem(
    val itemId: Int,
    val name: String,
    val quantity: Int,
    val price: Double,
    val discountPercent: Double = 0.0,
    val taxPercent: Double = 0.0,
    val total: Double
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val date: Long = System.currentTimeMillis(),
    val itemsJson: String, // JSON representation of List<InvoiceItem>
    val subTotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double,
    val amountPaid: Double,
    val paymentMode: String // "Cash", "Online", "Cheque", "Unpaid" (Credit)
)

@Entity(tableName = "ledger_parties")
data class LedgerParty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val partyType: String, // "CUSTOMER" or "SUPPLIER"
    val balance: Double = 0.0 // For Customer: >0 means Receivable (due from customer), For Supplier: >0 means Payable (due to supplier)
)

@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partyId: Int,
    val date: Long = System.currentTimeMillis(),
    val amount: Double,
    val transactionType: String, // "PAYMENT_RECEIVED", "CREDIT_GIVEN" (Receivable), "PAYMENT_MADE", "CREDIT_TAKEN" (Payable)
    val description: String = ""
)
