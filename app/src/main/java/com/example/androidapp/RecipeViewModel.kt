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

    fun addRecipe(name : String, ingredients : List<String>, estimatedTime: String, imageURL: String) {
        val recipe = Recipe(name, ingredients, estimatedTime, imageURL);
        recipes.add(recipe);
        liveRecipes.value = recipes.toList();
    }

    fun deleteRecipes() {
        recipes =  mutableListOf<Recipe>();
    }

    fun queryRecipes(query: String) {
        liveRecipes.value = recipes.filter { it.getName() == query }
            .toList()
            .takeIf { query != "" }
            ?: recipes.toList()
    }
}