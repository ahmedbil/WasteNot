package com.example.androidapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidapp.model.FoodItem

class InventoryViewModel : ViewModel() {

    private var nwManager = NetworkManager.getInstance()

    // list of items passed by server

    // list of items being displayed.
    private var items = mutableListOf<Item>()
    private var liveItems =  MutableLiveData<List<Item>>()

    fun getItems() : LiveData<List<Item>> {
        return liveItems;
    }

    fun fetchItems() {
        nwManager.getInventory {inventory ->

            items.clear()

            for ((id, item) in inventory) {
                items.add(Item(id.toInt(), item.name, item.amount.toDouble(), item.amount_unit, item.expiry_date.toString(), item.purchase_date.toString()))
            }

            liveItems.postValue(items.toList())
        }
    }

    fun addItem(name : String, quantity : Double, unit : String, purchase : String, expiry : String) {
        val foodItem = FoodItem(1, name, quantity, unit, purchase, expiry, false, false, listOf<String>())

        nwManager.addItemToInventory(foodItem) {
            fetchItems()
        }
    }

    fun deleteItem(id : String) {

    }

    fun deleteItems(deleteItems : List<Item>) {
        deleteItems.forEach { item ->
            val id = item.getId().toString()
            nwManager.deleteItemFromInventory(id) {inventory ->
                for ((id, item) in inventory) {
                    Log.i(id.toString(), item.toString())
                }
                    fetchItems()
            }
        }
    }

    fun updateItems() {

    }
}