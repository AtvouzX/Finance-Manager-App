package com.company.financemanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var subcategories: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.SubcategoryViewHolder>() {
    class SubcategoryViewHolder(itemView: View, val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(subcategory: String) {
            textView.text = subcategory
            itemView.setOnClickListener { onItemClick(subcategory) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return SubcategoryViewHolder(view, onItemClick)
    }

    override fun getItemCount(): Int = subcategories.size

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        holder.bind(subcategories[position])
    }

    fun updateList(newList: List<String>) {
        subcategories = newList
        notifyDataSetChanged()
    }
}
