package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecipeViewModel : ViewModel() {
    private var recipes =  mutableListOf<Recipe>();
    private var liveRecipes =  MutableLiveData<List<Recipe>>()

    init {

    }

    fun getRecipes() : LiveData<List<Recipe>> {
        return liveRecipes;
    }


    fun addRecipe(name : String, ingredients : List<String>, estimatedTime: String) {
        val recipe = Recipe(name, ingredients, estimatedTime);
        recipes.add(recipe);
        liveRecipes.value = recipes.toList();
    }
}