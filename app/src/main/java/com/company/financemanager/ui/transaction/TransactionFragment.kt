package com.company.financemanager.ui.transaction

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*
import com.company.financemanager.R
import com.company.financemanager.adapter.CategoryAdapter
import com.company.financemanager.adapter.GroupedTransaction
import com.company.financemanager.adapter.GroupedTransactionAdapter
import com.company.financemanager.models.TransactionModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class TransactionFragment : Fragment() {

    private lateinit var textViewBalanceAmount: TextView
    private lateinit var textViewIncomeAmount: TextView
    private lateinit var textViewExpenseAmount: TextView
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var groupedTransactionAdapter: GroupedTransactionAdapter
    private val groupedTransactionList = mutableListOf<GroupedTransaction>()
    private lateinit var navController: NavController
    private val expenseCategories = mutableListOf<String>()
    private val incomeCategories = mutableListOf<String>()
    private lateinit var categoryDialog: AlertDialog


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
        groupedTransactionAdapter =
            GroupedTransactionAdapter(groupedTransactionList) { transaction ->
                showEditDeleteDialog(transaction)
            }
        transactionRecyclerView.adapter = groupedTransactionAdapter

        database = FirebaseDatabase.getInstance().reference

        loadBalanceData()
        loadTransactionData()
        loadCategories()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController =
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }R.id.navigation_transaction -> {
                    navController.navigate(R.id.navigation_transaction)
                    true
                }R.id.navigation_add -> {
                    navController.navigate(R.id.navigation_add)
                    true
                }

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
            }
        })
    }

    private fun loadTransactionData() {
        database.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                    groupedTransactionList.add(GroupedTransaction(date, transactionsOnDate))
                }
                groupedTransactionList.sortByDescending { it.date }
                groupedTransactionAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadCategories() {
        database.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseCategories.clear()
                incomeCategories.clear()

                snapshot.child("Expense").children.forEach {
                    if (it.value == true) expenseCategories.add(it.key ?: "")
                }
                snapshot.child("Income").children.forEach {
                    if (it.value == true) incomeCategories.add(it.key ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateBalance(amount: Double, category: String, isDeletion: Boolean) {
        val balanceRef = database.child("balance")
        balanceRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: MutableData): com.google.firebase.database.Transaction.Result {
                val totalIncome =
                    currentData.child("total_income").getValue(Double::class.java) ?: 0.0
                val totalExpense =
                    currentData.child("total_expense").getValue(Double::class.java) ?: 0.0

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

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }
        })
    }

    private fun showEditDeleteDialog(transaction: TransactionModels) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_delete, null)
        val editTextAmount = dialogView.findViewById<EditText>(R.id.editTextAmount)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val editTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)
        val autoCompleteCategory =
            dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategory)
        val buttonDatePicker = dialogView.findViewById<ImageButton>(R.id.buttonDatePicker)
        val buttonCategoryPicker = dialogView.findViewById<ImageButton>(R.id.buttonCategoryPicker)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        val decimalFormat = DecimalFormat("#,###.##")
        editTextAmount.setText(decimalFormat.format(transaction.amount))
        editTextDescription.setText(transaction.description)
        editTextDate.setText(transaction.date)
        autoCompleteCategory.setText(transaction.subcategory)

        editTextAmount.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editTextAmount.removeTextChangedListener(this)

                    try {
                        var originalString = s.toString()

                        // Preserve cursor position
                        val cursorPosition = editTextAmount.selectionStart

                        // Remove all non-digit characters (including commas)
                        originalString = originalString.replace("[^\\d]".toRegex(), "")

                        // Parse to long for integer handling
                        val longval = originalString.toLong()
                        val formatter = DecimalFormat("#,###")
                        val formattedString = formatter.format(longval)

                        // Set formatted text and preserve cursor position
                        current = formattedString
                        editTextAmount.setText(formattedString)
                        editTextAmount.setSelection(
                            if (cursorPosition + formattedString.length - originalString.length > formattedString.length)
                                formattedString.length
                            else
                                cursorPosition + formattedString.length - originalString.length
                        )
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    }

                    editTextAmount.addTextChangedListener(this)
                }
            }
        })



        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            editTextDate.setText(sdf.format(cal.time))
        }

        val showDatePicker = {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(), dateSetListener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        editTextDate.setOnClickListener { showDatePicker() }
        buttonDatePicker.setOnClickListener { showDatePicker() }

        fun showCategoryPicker() {
            val categoryPickerView = layoutInflater.inflate(R.layout.dialog_category_picker, null)
            val recyclerView = categoryPickerView.findViewById<RecyclerView>(R.id.recyclerViewCategory)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            val categoryList = expenseCategories + incomeCategories
            val adapter = CategoryAdapter(categoryList) { selectedCategory ->
                autoCompleteCategory.setText(selectedCategory)
                Log.d("TransactionFragment", "Selected category: $selectedCategory")
                categoryDialog.dismiss()
            }
            recyclerView.adapter = adapter

            val categoryDialog = AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setView(categoryPickerView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            this.categoryDialog = categoryDialog
            categoryDialog.show()
        }


        autoCompleteCategory.setOnClickListener { showCategoryPicker() }
        buttonCategoryPicker.setOnClickListener { showCategoryPicker() }

        buttonDelete.setOnClickListener {
            updateBalance(transaction.amount, transaction.category, true)
            database.child("transactions").child(transaction.id).removeValue()
            dialog.dismiss()
        }

        buttonSave.setOnClickListener {
            try {
                // Remove commas and parse the amount
                val amountString = editTextAmount.text.toString().replace(".", "")
                val updatedAmount = amountString.toDouble()

                val updatedDescription = editTextDescription.text.toString()
                val updatedDate = editTextDate.text.toString()
                val updatedCategory = autoCompleteCategory.text.toString()

                // Calculate the difference in amount
                val difference = updatedAmount - transaction.amount

                // Determine if the category has changed from Expense to Income or vice versa
                val isOldCategoryExpense = transaction.subcategory.equals("Expense", ignoreCase = true)
                val isNewCategoryExpense = updatedCategory.equals("Expense", ignoreCase = true)

                // Update the balance based on the category change
                if (isOldCategoryExpense && !isNewCategoryExpense) {
                    // Changed from Expense to Income
                    updateBalance(transaction.amount, transaction.subcategory, true) // Revert old expense
                    updateBalance(updatedAmount, updatedCategory, false) // Apply new income
                } else if (!isOldCategoryExpense && isNewCategoryExpense) {
                    // Changed from Income to Expense
                    updateBalance(transaction.amount, transaction.subcategory, false) // Revert old income
                    updateBalance(updatedAmount, updatedCategory, true) // Apply new expense
                } else {
                    // No change in type, just update with difference
                    updateBalance(difference, updatedCategory, isNewCategoryExpense)
                }

                // Create the updated transaction object
                val updatedTransaction = transaction.copy(
                    amount = updatedAmount,
                    description = updatedDescription,
                    date = updatedDate,
                    subcategory = updatedCategory
                )

                // Update the transaction in the database
                database.child("transactions").child(transaction.id).setValue(updatedTransaction)
                dialog.dismiss() // Dismiss the dialog after saving
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                // Handle error (e.g., show a toast or error message)
                Toast.makeText(requireContext(), "Invalid input for amount", Toast.LENGTH_SHORT).show()
            }
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}
