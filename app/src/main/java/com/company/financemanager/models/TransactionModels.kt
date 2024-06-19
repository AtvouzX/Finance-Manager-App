package com.company.financemanager.models

data class TransactionModels(
    val id: String = "",
    val description: String = "",
    val date: String = "",
    val subcategory: String = "",
    val amount: Double = 0.0,
    val category: String = ""
)

