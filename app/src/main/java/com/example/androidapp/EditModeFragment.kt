package com.example.androidapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import android.widget.EditText
import android.widget.ImageButton


// Initialize an array of type Ingredient with values
val ingredientsArray = arrayOf(
    Ingredient("Beef", "250gm"),
    Ingredient("Rice noodles", "200gm"),
    Ingredient("Cilantro", "10gm")
)

class IngredientAdapter(
    private val ingredients: MutableList<Ingredient>,
    private val onDeleteClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientNameEditText: EditText = itemView.findViewById(R.id.ingredientNameEditText)
        val ingredientQuantityEditText: EditText =
            itemView.findViewById(R.id.ingredientQuantityEditText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.ingredientNameEditText.setText(ingredient.name)
        holder.ingredientQuantityEditText.setText(ingredient.quantity)

        holder.deleteButton.setOnClickListener {
            onDeleteClickListener.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }
}


class EditModeFragment : Fragment() {

    private lateinit var ingredientAdapter: IngredientAdapter
    private val ingredients: MutableList<Int> = mutableListOf()
    private var flagVariable: Boolean = false


    companion object {
        private const val ARG_FLAG_VARIABLE = "arg_flag_variable"
        private const val ARG_INGREDIENTS = "arg_ingredients"

        fun newInstance(flagVariable: Boolean, ingredients: ArrayList<Int>): EditModeFragment {
            val fragment = EditModeFragment()
            val args = Bundle()
            args.putBoolean(ARG_FLAG_VARIABLE, flagVariable)
            args.putIntegerArrayList(ARG_INGREDIENTS, ingredients)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the arguments passed from the other fragment
        arguments?.let {
            flagVariable = it.getBoolean(ARG_FLAG_VARIABLE, false)
            val ingredientList = it.getIntegerArrayList(ARG_INGREDIENTS)
            ingredientList?.let {
                ingredients.addAll(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_mode, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        val addButton: MaterialButton = rootView.findViewById(R.id.addButton)
        val saveButton: MaterialButton = rootView.findViewById(R.id.saveButton)

        val ingredientMutList = addIngredientsToMutList(ingredientsArray, ingredients)


        ingredientAdapter = IngredientAdapter(ingredientMutList) { position ->
            // Remove the ingredient at the specified position
            ingredientMutList.removeAt(position)
            ingredientAdapter.notifyItemRemoved(position)
        }

        recyclerView.adapter = ingredientAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addButton.setOnClickListener {
            //saveChanges()
        }

        saveButton.setOnClickListener {
            saveChanges()
        }

        return rootView
    }

    fun addIngredientsToMutList(ingredientsArray: Array<Ingredient>, ingredients: MutableList<Int>): MutableList<Ingredient> {
        val ingredientMut: MutableList<Ingredient> = mutableListOf()

        for (index in ingredients) {
            if (index >= 0 && index < ingredientsArray.size) {
                ingredientMut.add(ingredientsArray[index])
            }
        }

        return ingredientMut
    }


    private fun saveChanges() {
        // Check the flag variable to decide whether to update the existing list
        // or add the items to the list sent from the previous fragment.
        val flagVariable = true // Replace this with the actual flag variable value
        if (flagVariable) {
            // Update the existing list
            // For example, if you have received the list from the previous fragment
            // using arguments or ViewModel, you can update it here.
        } else {
            // Add the items to the list
            // For example, if you want to add the ingredients to a new list,
            // you can do it here.
        }
    }
}
