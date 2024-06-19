package com.company.financemanager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.models.TransactionModels
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class GroupedTransaction(val date: String, val transactions: List<TransactionModels>)

class GroupedTransactionAdapter(private val groupedTransactions: List<GroupedTransaction>,
                                private val onItemClicked: (TransactionModels) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeaderTextView: TextView = itemView.findViewById(R.id.dateHeaderTextView)
    }

    class ItemViewHolder(itemView: View, private val onItemClicked: (TransactionModels) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val descriptionText: TextView = itemView.findViewById(R.id.descriptiontext)
        val subcategoryText: TextView = itemView.findViewById(R.id.subcategorytext)
        val amountText: TextView = itemView.findViewById(R.id.amounttext)

        fun bind(transaction: TransactionModels) {
            descriptionText.text = transaction.description
            subcategoryText.text = transaction.subcategory
            amountText.text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(transaction.amount)}"
            itemView.setOnClickListener { onItemClicked(transaction) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var currentPosition = position
        for (group in groupedTransactions) {
            if (currentPosition == 0) {
                return VIEW_TYPE_HEADER
            }
            currentPosition--
            if (currentPosition < group.transactions.size) {
                return VIEW_TYPE_ITEM
            }
            currentPosition -= group.transactions.size
        }
        throw IndexOutOfBoundsException("Invalid position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_row, parent, false)
            ItemViewHolder(view, onItemClicked)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentPosition = position
        for (group in groupedTransactions) {
            if (currentPosition == 0 && holder is HeaderViewHolder) {
                val sdf = SimpleDateFormat("d MMMM yyyy", Locale("en", "EN"))
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(group.date)
                holder.dateHeaderTextView.text = date?.let { sdf.format(it) }
                return
            }
            currentPosition--
            if (currentPosition < group.transactions.size && holder is ItemViewHolder) {
                val transaction = group.transactions[currentPosition]
                holder.bind(transaction)
                holder.descriptionText.text = transaction.description
                holder.subcategoryText.text = transaction.subcategory
                val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
                holder.amountText.text = if (transaction.category == "Expense") {
                    holder.amountText.setTextColor(Color.parseColor("#FF6961"))
                    "-Rp ${numberFormat.format(transaction.amount)}"
                } else {
                    holder.amountText.setTextColor(Color.parseColor("#8C7BFF"))
                    "Rp ${numberFormat.format(transaction.amount)}"
                }
                return
            }
            currentPosition -= group.transactions.size
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        for (group in groupedTransactions) {
            count += 1 + group.transactions.size // 1 for header, plus number of transactions
        }
        return count
    }
}
