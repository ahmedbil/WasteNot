package com.example.androidapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentRecipesBinding

import com.squareup.picasso.Picasso



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

        recipeViewModel.deleteRecipes();
        createRecipes(recipeViewModel);

        recipeViewModel.getRecipes().observe(viewLifecycleOwner) { recipes ->
            binding.recipeScrollHost.removeAllViews();

            recipes.forEach{recipe ->
                val recipeOverviewCard = inflater.inflate(R.layout.recipe_overview_card, null);
                val recipeName = recipeOverviewCard.findViewById<TextView>(R.id.recipe_name);
                val recipeIngredients = recipeOverviewCard.findViewById<TextView>(R.id.recipe_ingredients);
                val recipeEstimatedTime = recipeOverviewCard.findViewById<TextView>(R.id.recipe_estimated_time);
                val recipeImage = recipeOverviewCard.findViewById<ImageView>(R.id.recipe_image);

                recipeName.text = recipe.getName();
                recipeIngredients.text = getIngredientsOverview(recipe.getIngredients());
                recipeEstimatedTime.text = recipe.getEstimatedTime();
                Picasso.get().load(recipe.getImageURL()).into(recipeImage)

                binding.recipeScrollHost.addView(recipeOverviewCard)
            }
        }

        return binding.root
    }

    fun createRecipes(recipeViewModel: RecipeViewModel) {
        recipeViewModel.addRecipe("Lamb Biryani", listOf("plain yogurt", "skinless chicken pieces",  "basmati rice", "vegetable oil" ), "2hr 15 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2022/07/27/0/YAHI_Dum-Aloo-Biryani_s4x3.jpg.rend.hgtvcom.826.620.suffix/1658954351318.jpeg");
        recipeViewModel.addRecipe("PHỞ BÒ", listOf("Beef brisket", "lb beef shank", "cooked rice noodles" ), "8hr 25 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/11/30/0/FNK_Instant-Pot-Beef-Pho-H_s4x3.jpg.rend.hgtvcom.826.620.suffix/1548176890147.jpeg");
        recipeViewModel.addRecipe("Tonkotsu ramen", listOf("chicken carcass", "pork ribs",  "dried shiitake mushrooms"), "20hr 45 mins", "https://food.fnr.sndimg.com/content/dam/images/food/fullset/2018/4/3/0/LS-Library_Kimchi-and-Bacon-Ramen_s4x3.jpg.rend.hgtvcom.826.620.suffix/1522778330680.jpeg");
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

        var recipeViewModel = ViewModelProvider(requireActivity())[RecipeViewModel::class.java]


        binding.searchView2.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    recipeViewModel.queryRecipes(newText);
                }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                recipeViewModel.queryRecipes(query);
                return false
            }

        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}