package com.example.androidapp.model

import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable

typealias Inventory = HashMap<Long,FoodItem>
typealias ShoppingList = List<FoodItem>

@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    val amount: BigDecimal,
    val amountUnit: String,
    val expiry_date: LocalDate,
    val purchase_date: LocalDate,
    val isExpiryEstimated: Boolean,
    val softDelete: Boolean,
    val owners: List<String>
)
