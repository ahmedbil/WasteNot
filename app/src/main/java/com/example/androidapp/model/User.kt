package com.example.androidapp.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val inventory: Inventory,
    val shoppingList: ShoppingList
);
