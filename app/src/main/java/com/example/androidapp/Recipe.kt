package com.example.androidapp

import android.view.View
class Recipe(name : String, ingredients : List<String>, estimatedTime: String, id: Int = View.generateViewId()) {
    private var id = id;
    private var name = name;
    private var ingredients = ingredients;
    private var estimatedTime = estimatedTime
    init {
    }
    fun getEstimatedTime() : String { return estimatedTime }
    fun getName() : String { return name }
    fun getId() : Int { return id}
    fun getIngredients() : List<String> { return ingredients }

}