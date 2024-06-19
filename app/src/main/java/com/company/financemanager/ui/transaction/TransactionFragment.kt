package com.company.financemanager.ui.transaction

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*
import com.company.financemanager.R
import com.company.financemanager.adapter.GroupedTransaction
import com.company.financemanager.adapter.GroupedTransactionAdapter
import com.company.financemanager.models.TransactionModels
import com.google.android.material.bottomnavigation.BottomNavigationView

class TransactionFragment : Fragment() {

    private lateinit var textViewBalanceAmount: TextView
    private lateinit var textViewIncomeAmount: TextView
    private lateinit var textViewExpenseAmount: TextView
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var groupedTransactionAdapter: GroupedTransactionAdapter
    private val groupedTransactionList = mutableListOf<GroupedTransaction>()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)
        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView)
        textViewBalanceAmount = view.findViewById(R.id.textViewBalanceAmount)
        textViewIncomeAmount = view.findViewById(R.id.textViewIncomeAmount)
        textViewExpenseAmount = view.findViewById(R.id.textViewExpenseAmount)
        transactionRecyclerView = view.findViewById(R.id.transactionRecyclerView)

        transactionRecyclerView.layoutManager = LinearLayoutManager(context)
        groupedTransactionAdapter = GroupedTransactionAdapter(groupedTransactionList) { transaction ->
            showEditDeleteDialog(transaction)
        }
        transactionRecyclerView.adapter = groupedTransactionAdapter

        database = FirebaseDatabase.getInstance().reference

        loadBalanceData()
        loadTransactionData()


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inisialisasi NavController
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)

        // Dapatkan referensi ke BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        // Tambahkan listener untuk menangani klik pada item navbar
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Pindah ke HomeFragment
                    navController.navigate(R.id.navigation_home)
                    true
                }R.id.navigation_transaction -> {
                    navController.navigate(R.id.navigation_transaction)
                true
            }
                R.id.navigation_add -> {
                    // Pindah ke ProfileFragment
                    navController.navigate(R.id.navigation_add)
                    true
                }
                // Tambahkan kasus lain untuk item navbar lainnya jika ada
                else -> false
            }
        }
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

    private fun showEditDeleteDialog(transaction: TransactionModels) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_delete, null)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)

        editTextAmount.setText(transaction.amount.toString())
        editTextDescription.setText(transaction.description)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit/Delete Transaction")
            .setView(dialogView)
            .create()

        buttonDelete.setOnClickListener {
            updateBalance(transaction.amount, transaction.category, true)
            database.child("transactions").child(transaction.id).removeValue()
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            val updatedAmount = editTextAmount.text.toString().toDouble()
            val updatedDescription = editTextDescription.text.toString()

            // Update balance with the difference between old and new amount
            val difference = updatedAmount - transaction.amount
            updateBalance(difference, transaction.category, false)

            database.child("transactions").child(transaction.id).setValue(
                transaction.copy(amount = updatedAmount, description = updatedDescription)
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateBalance(amount: Double, category: String, isDeletion: Boolean) {
        val balanceRef = database.child("balance")
        balanceRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                val totalIncome = currentData.child("total_income").getValue(Double::class.java) ?: 0.0
                val totalExpense = currentData.child("total_expense").getValue(Double::class.java) ?: 0.0

                if (isDeletion) {
                    if (category == "Income") {
                        currentData.child("total_income").value = totalIncome - amount
                    } else {
                        currentData.child("total_expense").value = totalExpense - amount
                    }
                } else {
                    if (category == "Income") {
                        currentData.child("total_income").value = totalIncome + amount
                    } else {
                        currentData.child("total_expense").value = totalExpense + amount
                    }
                }

                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Handle transaction completion
            }
        })
    }
}
