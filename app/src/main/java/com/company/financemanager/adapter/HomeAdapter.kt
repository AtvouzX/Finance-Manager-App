package com.company.financemanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.models.HomeHistory

import java.util.ArrayList

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {
    private val homeList = ArrayList<HomeHistory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.singlerow, parent, false)
        return HomeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return homeList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = homeList[position]
        holder.amount.text = currentItem.amount.toString()
        holder.date.text = currentItem.date
        holder.description.text = currentItem.description
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

    }
}