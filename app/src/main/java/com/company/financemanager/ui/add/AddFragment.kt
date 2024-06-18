package com.company.financemanager.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.financemanager.R
import com.company.financemanager.adapter.SubcategoryAdapter
import com.company.financemanager.databinding.FragmentAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar.YEAR
import java.util.Calendar.MONTH
import java.util.Calendar.DAY_OF_MONTH
import java.util.Locale
import java.util.*

class AddFragment : Fragment() {

    private lateinit var editTextDate: EditText
    private lateinit var buttonCalendar: ImageButton
    private lateinit var editTextSubcategory: EditText
    private lateinit var buttonSubcategory: ImageButton
    private lateinit var recyclerViewSubcategory: RecyclerView
    private val calendar = Calendar.getInstance()

    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var subcategoryList: MutableList<String>
    private lateinit var database: DatabaseReference

    private var _binding: FragmentAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        editTextDate = view.findViewById(R.id.editTextDate)
        buttonCalendar = view.findViewById(R.id.buttonCalendar)
        editTextSubcategory = view.findViewById(R.id.autoCompleteCategory)
        buttonSubcategory = view.findViewById(R.id.buttonSubcategory)
        recyclerViewSubcategory = view.findViewById(R.id.recyclerViewSubcategory)

        recyclerViewSubcategory.layoutManager = LinearLayoutManager(context)
        subcategoryList = mutableListOf()
        subcategoryAdapter = SubcategoryAdapter(subcategoryList) { selectedSubcategory ->
            editTextSubcategory.setText(selectedSubcategory)
            recyclerViewSubcategory.visibility = View.GONE
        }
        recyclerViewSubcategory.adapter = subcategoryAdapter

        database = FirebaseDatabase.getInstance().reference

        loadSubcategories()

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

        editTextDate.setOnClickListener(dateClickListener)
        buttonCalendar.setOnClickListener(dateClickListener)

        val subcategoryClickListener = View.OnClickListener {
            recyclerViewSubcategory.visibility = if (recyclerViewSubcategory.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        editTextSubcategory.setOnClickListener(subcategoryClickListener)
        buttonSubcategory.setOnClickListener(subcategoryClickListener)

        editTextSubcategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSubcategories(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun updateLabel() {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        editTextDate.setText(sdf.format(calendar.time))
    }

    private fun loadSubcategories() {
        val expenseRef = database.child("categories").child("Expense")
        val incomeRef = database.child("categories").child("Income")

        val subcategoryListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val subcategory = dataSnapshot.key ?: continue
                    if (!subcategoryList.contains(subcategory)) {
                        subcategoryList.add(subcategory)
                    }
                }
                subcategoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        expenseRef.addValueEventListener(subcategoryListener)
        incomeRef.addValueEventListener(subcategoryListener)
    }

    private fun filterSubcategories(query: String) {
        val filteredList = subcategoryList.filter { it.contains(query, ignoreCase = true) }
        subcategoryAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}