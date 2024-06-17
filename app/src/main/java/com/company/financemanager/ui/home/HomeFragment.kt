package com.company.financemanager.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.adapter.HomeAdapter
import com.company.financemanager.databinding.FragmentHomeBinding
import com.company.financemanager.models.HomeHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

private lateinit var homeViewModel: HomeViewModel
private lateinit var HomeRecyclerview : RecyclerView
private lateinit var adapter : HomeAdapter
class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var balanceAmountTextView: TextView
    private lateinit var expenseAmountTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: HomeAdapter
    private lateinit var transactionList: MutableList<HomeHistory>
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi TextView
        balanceAmountTextView = view.findViewById(R.id.balance_amount)
        expenseAmountTextView = view.findViewById(R.id.expense_amount)

        // Inisialisasi RecyclerView dan adapter
        recyclerView = view.findViewById(R.id.tr_history)
        recyclerView.layoutManager = LinearLayoutManager(context)

        transactionList = mutableListOf()
        transactionAdapter = HomeAdapter()
        recyclerView.adapter = transactionAdapter

        // Inisialisasi referensi database
        database = FirebaseDatabase.getInstance().reference

        // Mengambil dan menampilkan data balance dan expense
        loadBalanceAndExpense()

        // Mengambil dan menampilkan data transaksi
        loadTransactions()

        return view
    }

    private fun loadBalanceAndExpense() {
        database.child("balance").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Mengambil nilai total_balance dan total_expense dari database
                val totalBalance = snapshot.child("total_balance").getValue(Double::class.java) ?: 0.0
                val totalExpense = snapshot.child("total_expense").getValue(Double::class.java) ?: 0.0
                val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

                // Menampilkan nilai ke dalam TextView
                balanceAmountTextView.text = "Rp ${numberFormat.format(totalBalance)}"
                expenseAmountTextView.text = "-Rp ${numberFormat.format(totalExpense)}"
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan pembatalan operasi
            }
        })
    }

    private fun loadTransactions() {
        database.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                for (dataSnapshot in snapshot.children) {
                    val transaction = dataSnapshot.getValue(HomeHistory::class.java)
                    if (transaction != null) {
                        transactionList.add(transaction)
                    }
                }
                transactionAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan pembatalan operasi
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        HomeRecyclerview = view.findViewById(R.id.tr_history)
        HomeRecyclerview.layoutManager = LinearLayoutManager(context)
        HomeRecyclerview.setHasFixedSize(true)
        adapter = HomeAdapter()
        HomeRecyclerview.adapter = adapter

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.history.observe(viewLifecycleOwner) {
            adapter.updateHomeList(it)
        }
    }


}