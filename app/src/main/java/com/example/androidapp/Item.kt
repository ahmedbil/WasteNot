package com.example.androidapp

class Item(id : Int, name : String, quantity : Double, unit: String, expiryTime: String, purchaseDate : String) {
    private var id = id;
    private var name = name;
    private var quantity = quantity;
    private var unit = unit;
    private var expiryTime = expiryTime;
    private var purchaseDate = purchaseDate;

    fun getId() : Int { return id}

    fun getName() : String { return name }

    fun getQuantity() : Double { return quantity }

    fun getUnit() : String { return unit }

    fun getExpiryTime() : String { return expiryTime }

    fun getPurchaseDate() : String { return purchaseDate }
}