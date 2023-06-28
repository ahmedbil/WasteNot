package com.example.androidapp

import android.view.View
class Recipe(name : String, ingredients : List<String>, estimatedTime: String, imageURL: String, id: Int = View.generateViewId()) {
    private var id = id;
    private var name = name;
    private var ingredients = ingredients;
    private var estimatedTime = estimatedTime;
    private var imageURL = imageURL;
    init {
    }
    fun getEstimatedTime() : String { return estimatedTime }
    fun getName() : String { return name }
    fun getId() : Int { return id}
    fun getIngredients() : List<String> { return ingredients }

    fun getImageURL() : String { return imageURL }


}