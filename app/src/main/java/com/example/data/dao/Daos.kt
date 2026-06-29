package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.InventoryItem
import com.example.data.models.Invoice
import com.example.data.models.LedgerParty
import com.example.data.models.LedgerTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Int): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("UPDATE inventory_items SET currentStock = currentStock + :change WHERE id = :itemId")
    suspend fun updateStock(itemId: Int, change: Int)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceById(id: Int): Flow<Invoice?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice): Long

    @Delete
    suspend fun delete(invoice: Invoice)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<LedgerParty>>

    @Query("SELECT * FROM ledger_parties WHERE partyType = :type ORDER BY name ASC")
    fun getPartiesByType(type: String): Flow<List<LedgerParty>>

    @Query("SELECT * FROM ledger_parties WHERE id = :id")
    fun getPartyById(id: Int): Flow<LedgerParty?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: LedgerParty): Long

    @Update
    suspend fun updateParty(party: LedgerParty)

    @Delete
    suspend fun deleteParty(party: LedgerParty)

    @Query("UPDATE ledger_parties SET balance = balance + :balanceChange WHERE id = :partyId")
    suspend fun updatePartyBalance(partyId: Int, balanceChange: Double)

    @Query("SELECT * FROM ledger_transactions WHERE partyId = :partyId ORDER BY date DESC")
    fun getTransactionsForParty(partyId: Int): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction): Long

    @Query("DELETE FROM ledger_transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Int)
}
