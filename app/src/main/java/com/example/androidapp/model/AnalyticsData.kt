package com.example.androidapp.model

import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable

typealias Inventory = HashMap<Long,FoodItem>
typealias ShoppingList = List<FoodItem>

@Serializable
data class AnalyticsData(
    val key: Long,
    val name: String,
    val score: AnalyticsScore,
    val computed_date: LocalDate,

    )
