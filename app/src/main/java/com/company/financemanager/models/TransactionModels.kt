package com.company.financemanager.models

data class TransactionModels(var id : String ?=null,
                             var amount : Double ?=null,
                             var date : String ?=null,
                             var category : String ?=null,
                             var subcategory : String ?=null,
                             var description : String ?=null)
