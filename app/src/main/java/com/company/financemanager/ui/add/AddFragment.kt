package com.company.financemanager.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.adapter.CategoryAdapter
import com.company.financemanager.databinding.FragmentAddBinding
import com.company.financemanager.models.TransactionModels

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

class AddFragment : Fragment() {

    private lateinit var editTextDate: EditText
    private lateinit var buttonCalendar: ImageButton
    private lateinit var autoCompleteCategory: AutoCompleteTextView
    private lateinit var buttonCategory: ImageButton
    private lateinit var recyclerViewSubcategory: RecyclerView
    private lateinit var editTextAmount: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var saveButton: Button
    private val calendar = Calendar.getInstance()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var subcategoryList: MutableList<String>
    private lateinit var database: DatabaseReference
    private val expenseCategories = mutableSetOf<String>()
    private val incomeCategories = mutableSetOf<String>()


    private var _binding: FragmentAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonCalendar = view.findViewById(R.id.buttonCalendar)
        autoCompleteCategory = view.findViewById(R.id.autoCompleteCategory)
        buttonCategory = view.findViewById(R.id.buttonSubcategory)
        editTextAmount = view.findViewById(R.id.editTextAmount)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        saveButton = view.findViewById(R.id.saveButton)
        recyclerViewSubcategory = view.findViewById(R.id.recyclerViewSubcategory)


        recyclerViewSubcategory.layoutManager = LinearLayoutManager(context)
        subcategoryList = mutableListOf()
        categoryAdapter = CategoryAdapter(subcategoryList) { selectedSubcategory ->
            autoCompleteCategory.setText(selectedSubcategory)
            recyclerViewSubcategory.visibility = View.GONE
        }
        recyclerViewSubcategory.adapter = categoryAdapter

        database = FirebaseDatabase.getInstance().reference

        loadCategories()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        val dateClickListener = View.OnClickListener {
            DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        editTextDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        saveButton.setOnClickListener {
            saveTransaction()
        }

        buttonCalendar.setOnClickListener(dateClickListener)

        val subcategoryClickListener = View.OnClickListener {
            recyclerViewSubcategory.visibility = if (recyclerViewSubcategory.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        autoCompleteCategory.setOnClickListener(subcategoryClickListener)
        buttonCategory.setOnClickListener(subcategoryClickListener)

        /*editTextSubcategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSubcategories(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })*/

        return view
    }

    private fun updateLabel() {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        editTextDate.setText(sdf.format(calendar.time))
    }

    private fun loadCategories() {
        database.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.child("Expense").children.forEach {
                    if (it.value == true) expenseCategories.add(it.key ?: "")
                }
                snapshot.child("Income").children.forEach {
                    if (it.value == true) incomeCategories.add(it.key ?: "")
                }
                val allCategories = expenseCategories + incomeCategories
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allCategories.toList())
                autoCompleteCategory.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun saveTransaction() {
        val date = editTextDate.text.toString()
        val category = autoCompleteCategory.text.toString()
        val amount = editTextAmount.text.toString()
        val description = editTextDescription.text.toString()

        if (date.isEmpty() || category.isEmpty() || amount.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amountValue = amount.toDouble()
        val transactionId = database.child("transactions").push().key ?: return
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).parse(date)!!)

        val transactionCategory = when {
            expenseCategories.contains(category) -> "Expense"
            incomeCategories.contains(category) -> "Income"
            else -> ""
        }

        if (transactionCategory.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = TransactionModels(
            id = transactionId,
            description = description,
            date = formattedDate,
            subcategory = category,
            amount = amountValue,
            category = transactionCategory
        )

        database.child("transactions").child(transactionId).setValue(transaction).addOnCompleteListener {
            if (it.isSuccessful) {
                updateBalance(amountValue, transaction.category)
                Toast.makeText(requireContext(), "Transaction saved", Toast.LENGTH_SHORT).show()
                clearFields()
            } else {
                Toast.makeText(requireContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBalance(amount: Double, category: String) {
        val balanceRef = database.child("balance")
        balanceRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                val totalBalance = currentData.child("total_balance").getValue(Double::class.java) ?: 0.0
                val totalIncome = currentData.child("total_income").getValue(Double::class.java) ?: 0.0
                val totalExpense = currentData.child("total_expense").getValue(Double::class.java) ?: 0.0

                if (category == "Income") {
                    currentData.child("total_balance").value = totalBalance + amount
                    currentData.child("total_income").value = totalIncome + amount
                } else {
                    currentData.child("total_balance").value = totalBalance - amount
                    currentData.child("total_expense").value = totalExpense + amount
                }
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Handle transaction completion
            }
        })
    }

    private fun clearFields() {
        editTextDate.text.clear()
        autoCompleteCategory.text.clear()
        editTextAmount.text.clear()
        editTextDescription.text.clear()
    }

//    private fun loadSubcategories() {
//        val expenseRef = database.child("categories").child("Expense")
//        val incomeRef = database.child("categories").child("Income")
//
//        val subcategoryListener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (dataSnapshot in snapshot.children) {
//                    val subcategory = dataSnapshot.key ?: continue
//                    if (!subcategoryList.contains(subcategory)) {
//                        subcategoryList.add(subcategory)
//                    }
//                }
//                categoryAdapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        }
//
//        expenseRef.addValueEventListener(subcategoryListener)
//        incomeRef.addValueEventListener(subcategoryListener)
//    }

    /*private fun filterSubcategories(query: String) {
        val filteredList = subcategoryList.filter { it.contains(query, ignoreCase = true) }
        subcategoryAdapter.updateList(filteredList)
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




