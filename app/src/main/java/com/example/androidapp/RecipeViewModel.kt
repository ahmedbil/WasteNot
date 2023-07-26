package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidapp.model.Recipe
import com.example.androidapp.model.RecipeRequest

class RecipeViewModel : ViewModel() {
    private var nwManager = NetworkManager.getInstance()

    // list of recipes being displayed.
    private var liveRecipes =  MutableLiveData<List<Recipe>>()

    fun getRecipes() : LiveData<List<Recipe>> {
        return liveRecipes;
    }

    // fetch recipes whose name matches the query.
    fun queryRecipes(query: String, includedIngredients: List<String>, excludedIngredients: List<String>, dietaryRestrictions: List<String>) {
        val queriedRecipes = nwManager.searchRecipesByName(RecipeRequest(query, 50, includedIngredients, excludedIngredients)) { it ->
            val recipesMap = HashMap<String, Recipe>()
            it.forEach {
                recipesMap[it.metadata.source_url] = it
            }
            liveRecipes.postValue(recipesMap.toList().map { it.second })
        }
    }
}