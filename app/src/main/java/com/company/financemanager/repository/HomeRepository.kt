package com.company.financemanager.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.company.financemanager.models.HomeHistory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class HomeRepository {
    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("transactions")
    @Volatile private var INSTANCE : HomeRepository ?= null

    fun getInstance() : HomeRepository {
        return INSTANCE ?: synchronized(this) {
            val instance = HomeRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadTransactions(transactionsList : MutableLiveData<List<HomeHistory>>) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    val _transactionsList : List<HomeHistory> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(HomeHistory::class.java)!!
                    }
                    transactionsList.postValue(_transactionsList)

                } catch (e: Exception) {
                    Log.e("loadTransactions", "Error parsing data", e)
                    // You can also post an empty list or null to indicate an error
                    transactionsList.postValue(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("loadTransactions", "Database error: ${error.message}")
                // Optionally, you can also post an empty list or null to indicate a cancelled operation
                transactionsList.postValue(emptyList())
            }
        })
    }
}