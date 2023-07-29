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

    fun addItem(name : String, quantity : Double, unit : String, purchase : String, expiry : String) {
        val foodItem = FoodItem(1, name, quantity, unit, purchase, expiry, false, false, listOf<String>())

        nwManager.addItemToInventory(foodItem) {inventory ->

            setItems(inventory)

        }
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