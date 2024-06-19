package com.company.financemanager.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*
import com.company.financemanager.R
import com.company.financemanager.adapter.GroupedTransaction
import com.company.financemanager.adapter.GroupedTransactionAdapter
import com.company.financemanager.models.TransactionModels

class TransactionFragment : Fragment() {

    private lateinit var textViewBalanceAmount: TextView
    private lateinit var textViewIncomeAmount: TextView
    private lateinit var textViewExpenseAmount: TextView
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var groupedTransactionAdapter: GroupedTransactionAdapter
    private val groupedTransactionList = mutableListOf<GroupedTransaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        textViewBalanceAmount = view.findViewById(R.id.textViewBalanceAmount)
        textViewIncomeAmount = view.findViewById(R.id.textViewIncomeAmount)
        textViewExpenseAmount = view.findViewById(R.id.textViewExpenseAmount)
        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView)

        transactionRecyclerView.layoutManager = LinearLayoutManager(context)
        groupedTransactionAdapter = GroupedTransactionAdapter(groupedTransactionList)
        transactionRecyclerView.adapter = groupedTransactionAdapter

        database = FirebaseDatabase.getInstance().reference

        loadBalanceData()
        loadTransactionData()

        return view
    }

    private fun loadBalanceData() {
        database.child("balance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalIncome = snapshot.child("total_income").getValue(Double::class.java) ?: 0.0
                val totalExpense = snapshot.child("total_expense").getValue(Double::class.java) ?: 0.0
                val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
                val totalBalance = totalIncome - totalExpense

                //update totalBalance di firebase
                database.child("balance").child("total_balance").setValue(totalBalance)


                textViewBalanceAmount.text = "Rp ${numberFormat.format(totalBalance)}"
                textViewIncomeAmount.text = "Rp ${numberFormat.format(totalIncome)}"
                textViewExpenseAmount.text = "Rp ${numberFormat.format(totalExpense)}"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun loadTransactionData() {
        database.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupedTransactionList.clear()
                val transactions = mutableListOf<TransactionModels>()
                for (dataSnapshot in snapshot.children) {
                    val transaction = dataSnapshot.getValue(TransactionModels::class.java)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }
                groupedTransactionList.clear()
                val groupedByDate = transactions.groupBy { it.date }
                for ((date, transactionsOnDate) in groupedByDate) {
                    groupedTransactionList.add(GroupedTransaction(date.toString(), transactionsOnDate))
                }
                groupedTransactionList.sortByDescending { it.date }
                groupedTransactionAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }
}
