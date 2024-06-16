package com.company.financemanager.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.company.financemanager.models.HomeHistory
import com.company.financemanager.repository.HomeRepository

class HomeViewModel : ViewModel() {

    private val repository : HomeRepository
    private val _history = MutableLiveData<List<HomeHistory>>()
    val history : LiveData<List<HomeHistory>> = _history

    init {
        repository = HomeRepository().getInstance()
        repository.loadTransactions(_history)
    }

}