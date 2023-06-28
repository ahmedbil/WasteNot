package com.example.androidapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecipeViewModel : ViewModel() {
    private var nwManager: NetworkManager = NetworkManager("pixa.cubetex.net:8080")

    // list of recipes passed by server
    private var recipes =  mutableListOf<Recipe>();

    // list of recipes being displayed.
    private var liveRecipes =  MutableLiveData<List<Recipe>>()

    fun getRecipes() : LiveData<List<Recipe>> {
        return liveRecipes;
    }

    // create new recipe and update the current recipes being displayed.
    fun addRecipe(name : String, ingredients : List<String>, estimatedTime: String, imageURL: String) {
        val recipe = Recipe(name, ingredients, estimatedTime, imageURL);
        recipes.add(recipe);
        liveRecipes.value = recipes.toList();
    }

    // remove all recipes
    fun deleteRecipes() {
        recipes =  mutableListOf<Recipe>();
    }

    // fetch recipes whose name matches the query.
    fun queryRecipes(query: String) {
        val queriedRecipes = nwManager.getRecipes(query)

        recipes.clear();

        queriedRecipes.forEach { recipe ->

            var ingredients = mutableListOf<String>()

            recipe.ingredientsList.forEach { ingredient ->
                ingredients.add(ingredient.name)
            }

            val recipeTime = recipe.metadata.minutesToCook.toString() + " mins";

            recipes.add(Recipe(recipe.name, ingredients, recipeTime, recipe.metadata.imageUrl))
        }
        liveRecipes.value = recipes.toList();
    }
}