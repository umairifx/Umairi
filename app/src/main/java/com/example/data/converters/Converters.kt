package com.example.data.converters

import androidx.room.TypeConverter
import com.example.data.models.InvoiceItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, InvoiceItem::class.java)
    private val adapter = moshi.adapter<List<InvoiceItem>>(listType)

    @TypeConverter
    fun fromInvoiceItemList(value: List<InvoiceItem>?): String {
        return value?.let { adapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toInvoiceItemList(value: String?): List<InvoiceItem> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
