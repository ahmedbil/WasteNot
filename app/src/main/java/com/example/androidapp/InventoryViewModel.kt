package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {
    // list of items passed by server
    private var items =  mutableListOf<Item>();

    // list of items being displayed.
    private var liveItems =  MutableLiveData<List<Recipe>>()

    fun getItems() : LiveData<List<Recipe>> {
        return liveItems;
    }

    fun addItems() {
    }

    fun deleteItems() {
    }

    fun updateItems() {

    }
}