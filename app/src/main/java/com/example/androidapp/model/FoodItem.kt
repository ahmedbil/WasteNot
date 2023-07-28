package com.example.androidapp.model

import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable

import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializer(forClass = LocalDate::class)
object DateSerializer  {
    fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

typealias Inventory = @Serializable HashMap<Long,FoodItem>
typealias ShoppingList = @Serializable List<FoodItem>

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
