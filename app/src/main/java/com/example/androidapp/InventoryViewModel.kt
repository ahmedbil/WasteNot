package com.example.androidapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {

    private var nwManager = NetworkManager.getInstance()

    // list of items passed by server

    // list of items being displayed.
    private var liveItems =  MutableLiveData<List<Item>>()

    fun getItems() : LiveData<List<Item>> {
        return liveItems;
    }

    fun fetchItems() {
        nwManager.getInventory {inventory ->

            var items =  mutableListOf<Item>();

            for ((id, item) in inventory) {
                Log.i("hheeloo", "helloo")
                items.add(Item(id.toInt(), item.name, item.amount.toDouble(), item.amount_unit, item.expiry_date.toString(), item.purchase_date.toString()))
            }

            liveItems.postValue(items.toList())
        }
    }

    fun addItems() {
    }

    /*fun deleteItem(id : String) {
        nwManager.deleteItemFromInventory(id) {

        }

    }*/

    fun deleteItems() {
    }

    fun updateItems() {

    }
}