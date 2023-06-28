package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentRecipesBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FragmentRecipes : Fragment() {

    private var _binding: FragmentRecipesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRecipesBinding.inflate(inflater, container, false)

        var recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]

        createRecipes(recipeViewModel);

        recipeViewModel.getRecipes().observe(viewLifecycleOwner) { recipes ->
            binding.recipeScrollHost.removeAllViews();

            recipes.forEach{recipe ->
                val recipeOverviewCard = inflater.inflate(R.layout.recipe_overview_card, null);
                val recipeName = recipeOverviewCard.findViewById<TextView>(R.id.recipe_name);
                val recipeIngredients = recipeOverviewCard.findViewById<TextView>(R.id.recipe_ingredients);
                val recipeEstimatedTime = recipeOverviewCard.findViewById<TextView>(R.id.recipe_estimated_time);

                recipeName.text = recipe.getName();
                recipeIngredients.text = getIngredientsOverview(recipe.getIngredients());
                recipeEstimatedTime.text = recipe.getEstimatedTime();
                binding.recipeScrollHost.addView(recipeOverviewCard)

            }
        }

        return binding.root
    }

    fun createRecipes(recipeViewModel: RecipeViewModel) {
        recipeViewModel.addRecipe("Lamb Biryani", listOf("plain yogurt", "skinless chicken pieces",  "basmati rice", "vegetable oil" ), "2hr 15 mins");
        recipeViewModel.addRecipe("PHỞ BÒ", listOf("Beef brisket", "lb beef shank", "cooked rice noodles" ), "8hr 25 mins");
        recipeViewModel.addRecipe("Tonkotsu ramen", listOf("chicken carcass", "pork ribs",  "dried shiitake mushrooms"), "20hr 45 mins");
    }

    fun getIngredientsOverview(ingredients: List<String>) : String {
        var ingredientsDisplay = ""
        val numOfIngredients = ingredients.size;

        var ingredientDisplaySize = 0;

        ingredientDisplaySize = if (numOfIngredients >= 3) {
            3
        } else {
            numOfIngredients
        }

        val ingredientDisplayList = ingredients.take(ingredientDisplaySize)

        var isFirstIngredient = true;

        for (ingredient in ingredientDisplayList) {
            if (isFirstIngredient) {
                ingredientsDisplay = ingredient;
                isFirstIngredient = !isFirstIngredient;
            } else {
                ingredientsDisplay += ", $ingredient"
            }
        }

        return ingredientsDisplay
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}