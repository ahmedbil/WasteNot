package com.example.androidapp.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object DateSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

typealias Inventory = @Serializable HashMap<Long,FoodItem>
typealias ShoppingList = @Serializable List<FoodItem>

@Serializable
data class FoodItem(
    val id: Long,
    val name: String,
    val amount: Double,
    val amount_unit: String,
    val expiry_date: String,
    val purchase_date: String,
    val is_expiry_estimated: Boolean,
    val soft_delete: Boolean,
    val owners: List<String>
)
