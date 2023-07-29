package com.example.androidapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidapp.model.FoodItem
import com.example.androidapp.model.Inventory

class InventoryViewModel : ViewModel() {

    private var nwManager = NetworkManager.getInstance()

    // list of items passed by server

    // list of items being displayed.
    private var items = mutableListOf<Item>()
    private var liveItems =  MutableLiveData<List<Item>>()

    fun getItems() : LiveData<List<Item>> {
        return liveItems;
    }

    fun setItems(inventory : Inventory) {
        items.clear()

        for ((id, item) in inventory) {
            items.add(Item(id.toInt(), item.name, item.amount.toDouble(), item.amount_unit, item.expiry_date.toString(), item.purchase_date.toString()))
        }

        liveItems.postValue(items.toList())
    }

    fun fetchItems() {
        nwManager.getInventory {inventory ->

            setItems(inventory)

        }
    }

    fun addItems(foodItems : List<FoodItem>) {

        nwManager.addItemsToInventory(foodItems) {

            fetchItems()

        }
    }

    fun addItem(name : String, quantity : Double, unit : String, purchase : String, expiry : String) {
        val foodItem = listOf(FoodItem(1, name, quantity, unit, purchase, expiry, false, false, listOf<String>()))
        addItems(foodItem)
    }

    fun addReceiptItems(receiptItems: List<Pair<String, Pair<Double, String>>>) {
        var foodItems = mutableListOf<FoodItem>();

        receiptItems.forEach {item ->
            val name = item.first
            val quantity = item.second.first
            val unit = item.second.second
            foodItems.add(FoodItem(1, name, quantity, unit, "5000-01-20", "5000-01-20", false, false, listOf<String>()))
        }

        addItems(foodItems.toList())
    }

    fun deleteItem(id : String) {

    }

    fun deleteItems(deleteItems : List<Item>) {
        val foodItems = deleteItems.map{ FoodItem(it.getId().toLong(), it.getName(), it.getQuantity(), it.getUnit(), it.getExpiryTime(), it.getPurchaseDate(), false, false, listOf<String>())  }

        nwManager.deleteItemsFromInventory(foodItems) { inventory ->

            setItems(inventory)

        }
    }

    fun updateItems() {

    }
}