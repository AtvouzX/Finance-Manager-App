package com.company.financemanager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.models.HomeHistory
import java.text.NumberFormat
import java.text.SimpleDateFormat

import java.util.ArrayList
import java.util.Locale

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {
    private val homeList = ArrayList<HomeHistory>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.singlerow, parent, false)
        return HomeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(homeList.size > 5) {
            return 5
        } else {
            return homeList.size
        }
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = homeList[position]
        holder.date.text = currentItem.date
        holder.description.text = currentItem.description
        holder.subcategory.text = currentItem.subcategory

        val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

        val formatAmount = when (currentItem.category) {
            "Income" -> "+Rp ${numberFormat.format(currentItem.amount)}"
            "Expense" -> "-Rp ${numberFormat.format(currentItem.amount)}"
            else -> "Rp ${numberFormat.format(currentItem.amount)}"
        }

        holder.amount.text = formatAmount

        val color = when (currentItem.category) {
            "Income" -> Color.parseColor("#8C7BFF") // Purple
            "Expense" -> Color.parseColor("#FF6961") // Red
            else -> Color.BLACK // Default color
        }
        holder.amount.setTextColor(color)

        // Format date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(currentItem.date)
        val formattedDate = date?.let { outputFormat.format(it) } ?: currentItem.date
        holder.date.text = formattedDate

        }



    fun updateHomeList(homeList: List<HomeHistory>?) {
        this.homeList.clear()
        this.homeList.addAll(homeList!!)
        notifyDataSetChanged()

    }

    class HomeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val amount : TextView = itemView.findViewById(R.id.amounttext)
        val date : TextView = itemView.findViewById(R.id.datetext)
        val description : TextView = itemView.findViewById(R.id.descriptiontext)
        val subcategory : TextView = itemView.findViewById(R.id.subcategorytext)

    }
}