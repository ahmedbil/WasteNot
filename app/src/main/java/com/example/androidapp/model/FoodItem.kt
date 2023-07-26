package com.example.androidapp.model

import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable

typealias Inventory = HashMap<Long,FoodItem>
typealias ShoppingList = List<FoodItem>

@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    @Contextual
    val amount: BigDecimal,
    val amountUnit: String,
    @Contextual
    val expiry_date: LocalDate,
    @Contextual
    val purchase_date: LocalDate,
    val isExpiryEstimated: Boolean,
    val softDelete: Boolean,
    val owners: List<String>
)
