package com.company.financemanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.models.TransactionModels
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<TransactionModels>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionText: TextView = itemView.findViewById(R.id.descriptiontext)
        val dateText: TextView = itemView.findViewById(R.id.datetext)
        val subcategoryText: TextView = itemView.findViewById(R.id.subcategorytext)
        val amountText: TextView = itemView.findViewById(R.id.amounttext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.singlerow, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.descriptionText.text = transaction.description
        holder.dateText.text = transaction.date
        holder.subcategoryText.text = transaction.subcategory
        holder.amountText.text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(transaction.amount)}"
    }

    override fun getItemCount(): Int = transactions.size
}
